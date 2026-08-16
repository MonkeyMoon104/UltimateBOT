package com.monkey.ultimatebot.nms;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import com.monkey.ultimatebot.gui.LegacyBotGui;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Version-specific native bridge. Signatures are Bukkit-only so {@code core} can stay free of
 * {@code net.minecraft} types while NMS modules keep the real implementations.
 */
public interface INMSBridge {

    Set<PlatformCapability> capabilities();

    /**
     * Whether this bridge can spawn and drive fake-player bots. Stub bridges for unmapped
     * pre-1.17 revisions return {@code false}; {@code v1_8_R3}, {@code v1_16_R3}, and 1.17.1+
     * modules return {@code true} (legacy modules are minimal — see their package-info).
     */
    default boolean isBotRuntimeSupported() {
        return true;
    }

    default boolean supports(PlatformCapability capability) {
        return capabilities().contains(capability);
    }

    default boolean supportsCombatMode(CombatMode mode) {
        return Objects.requireNonNull(mode, "mode").supportedBy(capabilities());
    }

    ITrainingBot createTrainingBot(
            Location spawn,
            BotProfileData profile,
            Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions);

    void registerBotEntity(ITrainingBot bot);

    void addToProfileCache(ITrainingBot bot);

    void removeFromProfileCache(UUID botUUID);

    void moveBot(Player bot, double x, double y, double z);

    /**
     * Pushes the bot's current server position/rotation to nearby viewers. Needed on packet-spawned
     * fake players (especially pre-1.17) whose entity tracker does not send move packets.
     */
    default void broadcastBotPosition(Player bot) {}

    /**
     * Updates look direction without teleporting, and broadcasts head/body look to nearby viewers
     * when the bridge can send packets.
     *
     * <p>Paper &lt;1.17 cannot use {@code CraftPlayer#setRotation}; teleporting to apply yaw/pitch
     * cancels velocity and makes follow stutter. Default sets NMS fields only; legacy bot bridges
     * override to also send look packets.
     */
    default void setBotRotation(Player bot, float yaw, float pitch) {
        Objects.requireNonNull(bot, "bot");
        if (MinecraftVersionAccess.isAtLeast(1, 17)) {
            bot.setRotation(yaw, pitch);
            return;
        }
        com.monkey.ultimatebot.compat.CraftEntityLookAccess.setYawPitch(bot, yaw, pitch);
    }

    BotProfileData copyProfileWithTextures(Player viewer, UUID botUUID, String botName);

    BotProfileData createProfileWithTexture(
            UUID botUUID, String botName, String textureValue, @Nullable String textureSignature);

    String getProfileName(BotProfileData profile);

    boolean actuallyHurt(Player bot, float amount, EntityDamageEvent event);

    void hurt(LivingEntity target, @Nullable Player attacker, float amount, DamageKind kind);

    void explode(Location location, @Nullable Player cause, float power, boolean blockDamage);

    void playSound(Location location, String soundKey, String source, float volume, float pitch);

    void playSoundOnPlayer(Player player, String soundKey, float volume, float pitch);

    boolean useItemOnBlock(
            Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            EquipmentSlot hand);

    void throwEnderpearl(Player bot, Vector targetPos);

    void sendTabListAdd(Player viewer, ITrainingBot bot);

    /**
     * Clears packet-injected player-info for this bot on {@code viewer}. Default no-op for bridges
     * that do not inject PlayerInfo outside the entity tracker.
     */
    default void sendTabListRemove(Player viewer, ITrainingBot bot) {}

    void sendSpawnAndMeta(Player viewer, ITrainingBot bot);

    void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment);

    void broadcastEquipment(ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment);

    void broadcastMetadata(ITrainingBot bot);

    /**
     * Sprint + swim flag for Water/Trident PvP.
     *
     * <p>Default uses Bukkit APIs (enough on 1.17+ where the entity tracker syncs pose). Packet-spawned
     * 1.16 bots must also force {@code EntityPose.SWIMMING} and {@link #broadcastMetadata} or viewers
     * never see the swim animation.
     */
    default void setBotSwimming(ITrainingBot bot, boolean swimming) {
        Player player = Objects.requireNonNull(bot, "bot").asBukkitPlayer();
        player.setSprinting(swimming);
        try {
            player.setSwimming(swimming);
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
            // Pre-1.13 swim API
        }
    }

    /**
     * Reads equipment from the native fake-player inventory.
     *
     * <p>Must not go through {@code CraftInventoryPlayer}: {@code BotCraftPlayer} is not a
     * {@code CraftPlayer} and Bukkit inventory setters cast to that type.
     */
    ItemStack getBotItem(ITrainingBot bot, EquipmentSlot slot);

    void setBotItem(ITrainingBot bot, EquipmentSlot slot, @Nullable ItemStack stack);

    void clearBotInventory(ITrainingBot bot);

    void beginUsingBotItem(ITrainingBot bot, EquipmentSlot hand);

    void stopUsingBotItem(ITrainingBot bot);

    /** Native on-ground flag. Do not infer this from Bukkit velocity. */
    boolean isBotOnGround(ITrainingBot bot);

    /** Sets the native on-ground flag (needed before mace smash checks). */
    void setBotOnGround(ITrainingBot bot, boolean onGround);

    Vector getBotVelocity(ITrainingBot bot);

    /** Sets native delta-movement and marks the entity for velocity sync ({@code hurtMarked}). */
    void setBotVelocity(ITrainingBot bot, Vector velocity);

    float getBotFallDistance(ITrainingBot bot);

    void setBotFallDistance(ITrainingBot bot, float fallDistance);

    /** Native {@code Player#attack} so mace smash and item attributes apply. */
    void attackTarget(ITrainingBot bot, LivingEntity target);

    /**
     * Melee-hit any entity (including end crystals). Prefer this over Bukkit {@code Player#attack}
     * on pre-1.15 Paper where that API is missing.
     */
    default void attackEntity(ITrainingBot bot, org.bukkit.entity.Entity target) {
        Objects.requireNonNull(bot, "bot");
        Objects.requireNonNull(target, "target");
        if (target instanceof LivingEntity) {
            attackTarget(bot, (LivingEntity) target);
            return;
        }
        com.monkey.ultimatebot.compat.PlayerAttackAccess.attack(bot.asBukkitPlayer(), target);
    }

    /**
     * Spawns an inert TNT minecart.
     *
     * <p>Default is Bukkit {@code World#spawn} (correct on 1.17+). Pre-1.17 bridges override because
     * Paper 1.16 often cancels {@code VehicleCreateEvent} for {@code World#spawn}.
     */
    default Entity spawnExplosiveMinecart(Location location) {
        Objects.requireNonNull(location, "location");
        org.bukkit.World world = Objects.requireNonNull(location.getWorld(), "location world");
        return world.spawn(location, org.bukkit.entity.minecart.ExplosiveMinecart.class);
    }

    /**
     * Sets TNT-minecart fuse ticks. {@code -1} = inert; {@code 1} = detonate next tick.
     *
     * <p>Default: Bukkit explosion fallback when arming, because older Paper lacks {@code
     * ExplosiveMinecart#setFuseTicks} and Spigot-mapped NMS field names are not reflectable.
     */
    default void setExplosiveMinecartFuseTicks(Entity minecart, int ticks) {
        if (minecart == null || ticks < 0) {
            return;
        }
        org.bukkit.Location location = minecart.getLocation();
        minecart.remove();
        // Prefer NMS explode — Bukkit World#createExplosion(Location,F,ZZ) is missing on 1.12.
        explode(location, null, 4.0F, true);
    }

    /**
     * Opens the bot configuration UI.
     *
     * <p>InvUI 1.49 supports MC 1.14–1.21.11 but is compiled for <strong>Java 11+</strong>. On Java
     * 8 (common for Paper 1.16.5) we must not even class-load InvUI — use {@link LegacyBotGui}
     * instead. {@code NewBotGuiLauncher} is loaded reflectively so verification never pulls InvUI
     * on Java 8.
     */
    default void openBotGui(Player player, UltimateBot plugin, BotType botType) {
        if (MinecraftVersionAccess.isAtLeast(1, 14)
                && com.monkey.ultimatebot.compat.JavaRuntimeAccess.isAtLeast(11)) {
            try {
                Class<?> launcher = Class.forName("com.monkey.ultimatebot.gui.NewBotGuiLauncher");
                launcher
                        .getMethod("open", Player.class, UltimateBot.class, BotType.class)
                        .invoke(null, player, plugin, botType);
                return;
            } catch (ReflectiveOperationException | LinkageError error) {
                plugin.getLogger()
                        .warning(
                                "InvUI GUI unavailable ("
                                        + error.getClass().getSimpleName()
                                        + ": "
                                        + error.getMessage()
                                        + "); falling back to legacy kit GUI.");
            }
        }
        new LegacyBotGui(player, plugin, botType).open();
    }
}
