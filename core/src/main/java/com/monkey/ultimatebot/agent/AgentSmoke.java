package com.monkey.ultimatebot.agent;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import com.monkey.ultimatebot.compat.WorldAccess;
import com.monkey.ultimatebot.nms.INMSBridge;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Headless spawn smoke for agent/CI matrix runs (no GUI, no online player required).
 *
 * <p>Writes {@code plugins/UltimateBot/agent-smoke-result.txt} with STATUS=PASS|FAIL.
 */
public final class AgentSmoke {

    public static final String RESULT_FILE = "agent-smoke-result.txt";

    private AgentSmoke() {}

    public static boolean envEnabled() {
        if (envFlagTrue("ULTIMATEBOT_AGENT_SMOKE") || envFlagTrue("ULTIMATEBOT_AGENT_LICENSE_BYPASS")) {
            return true;
        }
        // File trigger is more reliable than env with some Windows Start-Process setups.
        try {
            java.io.File trigger = new java.io.File("plugins/UltimateBot/agent-smoke.trigger");
            return trigger.isFile();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean envFlagTrue(String name) {
        String value = System.getenv(name);
        return value != null && (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("true"));
    }

    public static SmokeReport run(UltimateBot plugin) {
        String version = MinecraftVersionAccess.minecraftVersion();
        INMSBridge bridge = NMSBridgeManager.get();
        String bridgeName = bridge.getClass().getName();
        StringBuilder detail = new StringBuilder();
        detail.append("mc=").append(version).append('\n');
        detail.append("bridge=").append(bridgeName).append('\n');
        detail.append("botRuntimeSupported=").append(bridge.isBotRuntimeSupported()).append('\n');

        if (!bridge.isBotRuntimeSupported()) {
            return fail(plugin, version, bridgeName, "bot runtime not supported on this bridge", detail);
        }

        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (world == null) {
            return fail(plugin, version, bridgeName, "no worlds loaded", detail);
        }

        Location spawn = world.getSpawnLocation().clone().add(0.5, 0, 0.5);
        detail.append("spawn=").append(WorldAccess.name(spawn.getWorld()))
                .append(' ')
                .append(spawn.getX()).append(',')
                .append(spawn.getY()).append(',')
                .append(spawn.getZ())
                .append('\n');

        UUID botUuid = UUID.nameUUIDFromBytes(("ultimatebot-agent-smoke-" + version).getBytes(StandardCharsets.UTF_8));
        BotProfileData profile = new BotProfileData(botUuid, "AgentSmoke", Collections.emptyList());

        ITrainingBot bot = null;
        try {
            BotOptions options = new BotOptions(plugin, new java.util.HashMap<>());
            options.setCombat(false);
            options.setFollow(false);
            options.setEnderPearls(false);

            bot = bridge.createTrainingBot(
                    spawn,
                    profile,
                    null,
                    false,
                    plugin,
                    "agent-smoke-dead",
                    "agent-smoke-event-dead",
                    options);
            detail.append("created=").append(bot.getClass().getName()).append('\n');

            bridge.registerBotEntity(bot);
            detail.append("registered=true\n");

            Player bukkit = bot.asBukkitPlayer();
            Location loc = bukkit.getLocation();
            if (loc == null || loc.getWorld() == null) {
                return fail(plugin, version, bridgeName, "bot location null after register", detail);
            }
            detail.append("bukkitLoc=")
                    .append(WorldAccess.name(loc.getWorld()))
                    .append(' ')
                    .append(loc.getX()).append(',')
                    .append(loc.getY()).append(',')
                    .append(loc.getZ())
                    .append('\n');
            detail.append("entityId=").append(bukkit.getEntityId()).append('\n');
            detail.append("uuid=").append(bot.getUniqueId()).append('\n');

            // One AI tick path without requiring a real combat target.
            bot.getBotAI();
            detail.append("botAi=ok\n");

            // Exercise AttributeAccess so MAX_HEALTH / ATTACK_DAMAGE renames fail CI.
            double maxHealth = bot.maxHealthValue();
            double attackDamage = bot.getAttackDamageAttribute();
            detail.append("maxHealth=").append(maxHealth).append('\n');
            detail.append("attackDamage=").append(attackDamage).append('\n');
            if (!(maxHealth > 0.0D) || Double.isNaN(maxHealth) || Double.isInfinite(maxHealth)) {
                return fail(plugin, version, bridgeName, "maxHealthValue invalid: " + maxHealth, detail);
            }
            if (!(attackDamage > 0.0D) || Double.isNaN(attackDamage) || Double.isInfinite(attackDamage)) {
                return fail(plugin, version, bridgeName, "getAttackDamageAttribute invalid: " + attackDamage, detail);
            }

            return pass(plugin, version, bridgeName, detail);
        } catch (Throwable t) {
            detail.append("exception=").append(t.getClass().getName()).append(": ").append(t.getMessage()).append('\n');
            plugin.getLogger().log(Level.SEVERE, "[agent-smoke] failed", t);
            return fail(plugin, version, bridgeName, t.getClass().getSimpleName() + ": " + t.getMessage(), detail);
        } finally {
            if (bot != null) {
                try {
                    bridge.removeFromProfileCache(bot.getUniqueId());
                } catch (Throwable ignored) {
                    // best-effort cleanup
                }
                try {
                    bot.asBukkitPlayer().remove();
                } catch (Throwable ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    private static SmokeReport pass(UltimateBot plugin, String version, String bridge, StringBuilder detail) {
        SmokeReport report = new SmokeReport(true, version, bridge, null, detail.toString());
        write(plugin, report);
        plugin.getLogger().info("[agent-smoke] PASS bridge=" + bridge);
        return report;
    }

    private static SmokeReport fail(
            UltimateBot plugin, String version, String bridge, String error, StringBuilder detail) {
        SmokeReport report = new SmokeReport(false, version, bridge, error, detail.toString());
        write(plugin, report);
        plugin.getLogger().severe("[agent-smoke] FAIL: " + error);
        return report;
    }

    private static void write(UltimateBot plugin, SmokeReport report) {
        Path path = plugin.getDataFolder().toPath().resolve(RESULT_FILE);
        String body = "STATUS="
                + (report.passed() ? "PASS" : "FAIL")
                + "\nVERSION="
                + nullToEmpty(report.minecraftVersion())
                + "\nBRIDGE="
                + nullToEmpty(report.bridgeClass())
                + "\nERROR="
                + nullToEmpty(report.error())
                + "\n---\n"
                + nullToEmpty(report.detail())
                + "\n";
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, body.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "[agent-smoke] could not write " + path, e);
        }
    }

    private static String nullToEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }

    public static final class SmokeReport {
        private final boolean passed;
        private final String minecraftVersion;
        private final String bridgeClass;
        private final @Nullable String error;
        private final String detail;

        public SmokeReport(
                boolean passed,
                String minecraftVersion,
                String bridgeClass,
                @Nullable String error,
                String detail) {
            this.passed = passed;
            this.minecraftVersion = minecraftVersion;
            this.bridgeClass = bridgeClass;
            this.error = error;
            this.detail = detail;
        }

        public boolean passed() {
            return passed;
        }

        public String minecraftVersion() {
            return minecraftVersion;
        }

        public String bridgeClass() {
            return bridgeClass;
        }

        public @Nullable String error() {
            return error;
        }

        public String detail() {
            return detail;
        }
    }
}
