package com.monkey.ultimatebot.nms;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface INMSBridge {

    Set<PlatformCapability> capabilities();

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

    default void broadcastBotPosition(Player bot) {}

    default void setBotRotation(Player bot, float yaw, float pitch) {
        Objects.requireNonNull(bot, "bot");
        if (MinecraftVersionAccess.isAtLeast(1, 17)) {
            bot.setRotation(yaw, pitch);
            return;
        }
        com.monkey.ultimatebot.access.entity.CraftEntityLookAccess.setYawPitch(bot, yaw, pitch);
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
            Player bot, ItemStack stack, Block clicked, BlockFace face, Location hitLocation, EquipmentSlotKind hand);

    void throwEnderpearl(Player bot, Vector targetPos);

    void sendTabListAdd(Player viewer, ITrainingBot bot);

    default void sendTabListRemove(Player viewer, ITrainingBot bot) {}

    void sendSpawnAndMeta(Player viewer, ITrainingBot bot);

    void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlotKind, ItemStack> equipment);

    void broadcastEquipment(ITrainingBot bot, Map<EquipmentSlotKind, ItemStack> equipment);

    void broadcastMetadata(ITrainingBot bot);

    default void setBotSwimming(ITrainingBot bot, boolean swimming) {
        Player player = Objects.requireNonNull(bot, "bot").asBukkitPlayer();
        player.setSprinting(swimming);
        try {
            player.setSwimming(swimming);
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {

        }
    }

    ItemStack getBotItem(ITrainingBot bot, EquipmentSlotKind slot);

    void setBotItem(ITrainingBot bot, EquipmentSlotKind slot, @Nullable ItemStack stack);

    void clearBotInventory(ITrainingBot bot);

    void beginUsingBotItem(ITrainingBot bot, EquipmentSlotKind hand);

    void stopUsingBotItem(ITrainingBot bot);

    boolean isBotOnGround(ITrainingBot bot);

    void setBotOnGround(ITrainingBot bot, boolean onGround);

    Vector getBotVelocity(ITrainingBot bot);

    void setBotVelocity(ITrainingBot bot, Vector velocity);

    float getBotFallDistance(ITrainingBot bot);

    void setBotFallDistance(ITrainingBot bot, float fallDistance);

    void attackTarget(ITrainingBot bot, LivingEntity target);

    default void attackEntity(ITrainingBot bot, org.bukkit.entity.Entity target) {
        Objects.requireNonNull(bot, "bot");
        Objects.requireNonNull(target, "target");
        if (target instanceof LivingEntity) {
            attackTarget(bot, (LivingEntity) target);
            return;
        }
        com.monkey.ultimatebot.access.player.PlayerAttackAccess.attack(bot.asBukkitPlayer(), target);
    }

    default Entity spawnExplosiveMinecart(Location location) {
        Objects.requireNonNull(location, "location");
        org.bukkit.World world = Objects.requireNonNull(location.getWorld(), "location world");
        return world.spawn(location, org.bukkit.entity.minecart.ExplosiveMinecart.class);
    }

    default void setExplosiveMinecartFuseTicks(Entity minecart, int ticks) {
        if (minecart == null || ticks < 0) {
            return;
        }
        org.bukkit.Location location = minecart.getLocation();
        minecart.remove();

        explode(location, null, 4.0F, true);
    }

    default void openBotGui(Player player, UltimateBot plugin, BotType botType) {
        if (MinecraftVersionAccess.isAtLeast(1, 14) && com.monkey.ultimatebot.access.runtime.JavaRuntimeAccess.isAtLeast(11)) {
            try {
                Class<?> launcher = Class.forName("com.monkey.ultimatebot.gui.NewBotGuiLauncher");
                launcher.getMethod("open", Player.class, UltimateBot.class, BotType.class)
                        .invoke(null, player, plugin, botType);
                return;
            } catch (ReflectiveOperationException | LinkageError error) {
                Throwable root = error;
                if (error instanceof java.lang.reflect.InvocationTargetException) {
                    Throwable target = ((java.lang.reflect.InvocationTargetException) error).getTargetException();
                    if (target != null) {
                        root = target;
                    }
                }
                while (root.getCause() != null && root.getCause() != root) {
                    root = root.getCause();
                }
                String detail = root.getMessage();
                if (detail == null || detail.isEmpty()) {
                    detail = root.toString();
                }
                plugin.getLogger()
                        .warning("InvUI GUI unavailable ("
                                + root.getClass().getSimpleName()
                                + ": "
                                + detail
                                + "); falling back to legacy kit GUI.");
            }
        }
        new LegacyBotGui(player, plugin, botType).open();
    }
}
