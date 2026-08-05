package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.brain.BotBrainSession;
import com.monkey.ultimatebot.api.extension.brain.BrainTick;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.ModeInventorySession;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.extension.registry.CoreExtensionRegistry;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.logging.Level;
import java.util.random.RandomGenerator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class CustomBrainRuntime implements AutoCloseable {
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    private final UltimateBot plugin;
    private final Player bot;
    private final BotOptions options;
    private final CoreExtensionRegistry extensions;
    private final CoreBotControl control;
    private final CoreNativeBotAccess nativeAccess;
    private final RandomGenerator random;
    private final ModeInventorySession kitSession;
    private final CustomBrainSignalDispatcher signals;

    private @Nullable BrainKey activeKey;
    private @Nullable CombatMode activeMode;
    private @Nullable BotBrainSession session;
    private long sequence;
    private int consecutiveFailures;
    private boolean disabled;

    public CustomBrainRuntime(
            UltimateBot plugin,
            Player bot,
            BotOptions options,
            CoreBotControl control,
            CoreNativeBotAccess nativeAccess,
            BotInventoryController inventory) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.bot = Objects.requireNonNull(bot, "bot");
        this.options = Objects.requireNonNull(options, "options");
        this.extensions = plugin.getExtensionRegistry();
        this.control = Objects.requireNonNull(control, "control");
        this.nativeAccess = Objects.requireNonNull(nativeAccess, "nativeAccess");
        this.kitSession = new ModeInventorySession(Objects.requireNonNull(inventory, "inventory"));
        this.random = new SplittableRandom(
                bot.getUUID().getMostSignificantBits() ^ ~bot.getUUID().getLeastSignificantBits());
        this.signals = new CustomBrainSignalDispatcher(() -> disabled ? null : session, this::handleSignalFailure);
    }

    public boolean tick(@Nullable LivingEntity target, boolean follow, boolean combat) {
        BrainKey selected = selectedBrain();
        CombatMode selectedMode = options.getCombatMode();
        boolean providerAvailable =
                selected == null || extensions.brain(selected).isPresent();
        if (!providerAvailable) {
            if (session != null) {
                closeSession();
            }
            activeKey = selected;
            activeMode = selectedMode;
            disabled = true;
            consecutiveFailures = 0;
            return false;
        }
        if (disabled && session == null && consecutiveFailures == 0 && Objects.equals(activeKey, selected)) {
            transition(selected, selectedMode);
        }
        if (!Objects.equals(activeKey, selected) || !Objects.equals(activeMode, selectedMode)) {
            transition(selected, selectedMode);
        }
        BotBrainSession current = session;
        if (current == null || disabled) {
            return false;
        }
        nativeAccess.target(target);
        org.bukkit.entity.LivingEntity bukkitTarget =
                target == null ? null : (org.bukkit.entity.LivingEntity) target.getBukkitEntity();
        try {
            current.tick(new BrainTick(sequence++, follow, combat, bukkitTarget));
            consecutiveFailures = 0;
            return true;
        } catch (RuntimeException | LinkageError error) {
            consecutiveFailures++;
            plugin.getLogger()
                    .log(
                            Level.WARNING,
                            "Custom brain " + activeKey + " failed for bot " + bot.getUUID() + " ("
                                    + consecutiveFailures + '/' + MAX_CONSECUTIVE_FAILURES + ')',
                            error);
            if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                disabled = true;
                closeSession();
            }
            return false;
        }
    }

    public boolean isConfigured() {
        BrainKey selected = selectedBrain();
        return selected != null && extensions.brain(selected).isPresent();
    }

    public void targetChanged(
            org.bukkit.entity.@Nullable LivingEntity previous, org.bukkit.entity.@Nullable LivingEntity current) {
        signals.targetChanged(previous, current);
    }

    public void damaged(org.bukkit.event.entity.EntityDamageEvent event) {
        signals.damaged(event);
    }

    public void refreshRegistration() {
        BrainKey selected = selectedBrain();
        CombatMode selectedMode = options.getCombatMode();
        if (!Objects.equals(activeKey, selected) || !Objects.equals(activeMode, selectedMode)) {
            transition(selected, selectedMode);
            return;
        }
        if (selected != null && extensions.brain(selected).isEmpty()) {
            closeSession();
            kitSession.close();
            disabled = true;
            consecutiveFailures = 0;
        }
    }

    private void handleSignalFailure(Throwable error, String label) {
        consecutiveFailures++;
        plugin.getLogger().log(Level.WARNING, "Custom brain " + activeKey + " failed on " + label, error);
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            disabled = true;
            closeSession();
        }
    }

    private @Nullable BrainKey selectedBrain() {
        BrainKey selected = options.getBrainKey();
        return selected != null
                ? selected
                : extensions
                        .combatMode(options.getCombatMode())
                        .flatMap(provider -> provider.descriptor().brainKey())
                        .orElse(null);
    }

    private void transition(@Nullable BrainKey selected, CombatMode selectedMode) {
        closeSession();
        kitSession.close();
        activeKey = selected;
        activeMode = Objects.requireNonNull(selectedMode, "selectedMode");
        disabled = false;
        consecutiveFailures = 0;
        if (selected == null) {
            return;
        }
        BotBrainProvider provider = extensions.brain(selected).orElse(null);
        if (provider == null) {
            plugin.getLogger().warning("Custom brain is unavailable: " + selected);
            disabled = true;
            return;
        }
        try {
            extensions
                    .combatMode(selectedMode)
                    .ifPresent(modeProvider -> kitSession.apply(
                            ModeKitAdapter.toInternal(modeProvider.descriptor().kit()), options));
            BotBrainSession created = Objects.requireNonNull(
                    provider.create(new CoreBotBrainContext(bot, control, nativeAccess, random)),
                    "brain provider returned null session");
            session = created;
            created.onStart();
        } catch (RuntimeException | LinkageError error) {
            disabled = true;
            closeSession();
            plugin.getLogger().log(Level.WARNING, "Unable to start custom brain " + selected, error);
        }
    }

    private void closeSession() {
        BotBrainSession current = session;
        session = null;
        if (current == null) {
            return;
        }
        try {
            current.close();
        } catch (Exception | LinkageError error) {
            plugin.getLogger().log(Level.WARNING, "Unable to close custom brain " + activeKey, error);
        }
    }

    @Override
    public void close() {
        closeSession();
        kitSession.close();
        activeKey = null;
        activeMode = null;
        disabled = true;
        nativeAccess.target(null);
    }
}
