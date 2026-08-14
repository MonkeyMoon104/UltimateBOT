package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.compat.ItemStackAccess;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import com.monkey.ultimatebot.compat.AttributeAccess;
import com.monkey.ultimatebot.compat.PlayerAttackAccess;
import com.monkey.ultimatebot.compat.PlayerAttackCooldownAccess;
import com.monkey.ultimatebot.compat.PlayerSwingAccess;
import com.monkey.ultimatebot.compat.VelocityAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Version-neutral bot handle used by core AI. Native {@code net.minecraft} types stay inside NMS
 * modules; core talks only through this facade and {@link com.monkey.ultimatebot.nms.INMSBridge}.
 *
 * <p>Most accessors default to the Bukkit {@link Player} view. Version modules only need to supply
 * {@link #asBukkitPlayer()} plus the small set of AI state / lifecycle hooks.
 */
public interface ITrainingBot {

    boolean isCombat();

    void setCombat(boolean combat);

    boolean isFollow();

    void setFollow(boolean follow);

    int getTotemCount();

    void setTotemCount(int count);

    org.bukkit.entity.@Nullable Player getTargetPlayer();

    BotBrainController getBrainController();

    BotAI getBotAI();

    TotemTrackerService getTotemTracker();

    UltimateBot getPlugin();

    /** Bukkit view of this fake player (Craft adapter owned by the active NMS module). */
    Player asBukkitPlayer();

    /** Called after the bridge has applied native damage and produced a Bukkit damage event. */
    void onDamaged(EntityDamageEvent event);

    /** Called after the native entity has died. */
    void onDeath(@Nullable LivingEntity killer);

    default UUID getUniqueId() {
        return asBukkitPlayer().getUniqueId();
    }

    default int getEntityId() {
        return asBukkitPlayer().getEntityId();
    }

    default World getWorld() {
        return Objects.requireNonNull(asBukkitPlayer().getWorld(), "bot world");
    }

    default Location getLocation() {
        return Objects.requireNonNull(asBukkitPlayer().getLocation(), "bot location");
    }

    default Vector bukkitPosition() {
        Location location = getLocation();
        return new Vector(location.getX(), location.getY(), location.getZ());
    }

    default Vector bukkitEyePosition() {
        return asBukkitPlayer().getEyeLocation().toVector();
    }

    default Vector bukkitVelocity() {
        return VelocityAccess.finite(NMSBridgeManager.get().getBotVelocity(this));
    }

    default void setBukkitVelocity(Vector velocity) {
        NMSBridgeManager.get()
                .setBotVelocity(this, VelocityAccess.finite(Objects.requireNonNull(velocity, "velocity")));
    }

    default boolean isOnGround() {
        return NMSBridgeManager.get().isBotOnGround(this);
    }

    default void setOnGround(boolean onGround) {
        NMSBridgeManager.get().setBotOnGround(this, onGround);
    }

    default float getYaw() {
        return getLocation().getYaw();
    }

    default float getPitch() {
        return getLocation().getPitch();
    }

    default float getHeadYaw() {
        return getLocation().getYaw();
    }

    default void setRotation(float yaw, float pitch) {
        com.monkey.ultimatebot.compat.PlayerRotationAccess.set(asBukkitPlayer(), yaw, pitch);
    }

    default Vector getLookDirection() {
        return getLocation().getDirection();
    }

    default BoundingBox bukkitBoundingBox() {
        return asBukkitPlayer().getBoundingBox();
    }

    default boolean isAlive() {
        return !asBukkitPlayer().isDead() && asBukkitPlayer().getHealth() > 0.0D;
    }

    default boolean isRemoved() {
        return !asBukkitPlayer().isValid();
    }

    /** Bukkit health accessor — named to avoid clashing with NMS {@code LivingEntity#getHealth()}. */
    default double healthValue() {
        return asBukkitPlayer().getHealth();
    }

    /** Bukkit max-health accessor — named to avoid clashing with NMS {@code LivingEntity#getMaxHealth()}. */
    default double maxHealthValue() {
        return AttributeAccess.maxHealthValue(asBukkitPlayer());
    }

    default void setHealthValue(double health) {
        asBukkitPlayer().setHealth(Math.max(0.0D, Math.min(health, maxHealthValue())));
    }

    default double distanceTo(Entity entity) {
        return getLocation().distance(Objects.requireNonNull(entity.getLocation(), "entity location"));
    }

    default boolean hasLineOfSight(Entity entity) {
        return asBukkitPlayer().hasLineOfSight(entity);
    }

    /**
     * Forces a full attack-strength charge. Implemented by NMS {@code TrainingBot} subclasses
     * (they can touch the protected ticker; do not put helpers under {@code net.minecraft.*}).
     */
    default void prepareFullAttackStrength() {}

    default void attackEntity(Entity target) {
        Entity checked = Objects.requireNonNull(target, "target");
        prepareFullAttackStrength();
        if (checked instanceof LivingEntity) { LivingEntity living = (LivingEntity) checked;
            NMSBridgeManager.get().attackTarget(this, living);
            return;
        }
        PlayerAttackAccess.attack(asBukkitPlayer(), checked);
    }

    default float fallDistanceValue() {
        return NMSBridgeManager.get().getBotFallDistance(this);
    }

    default void setFallDistanceValue(float fallDistance) {
        NMSBridgeManager.get().setBotFallDistance(this, fallDistance);
    }

    default void swingMainHand() {
        PlayerSwingAccess.swingMainHand(asBukkitPlayer());
    }

    default void swingOffHand() {
        PlayerSwingAccess.swingOffHand(asBukkitPlayer());
    }

    default float getAttackStrengthScale(float advance) {
        return PlayerAttackCooldownAccess.get(asBukkitPlayer(), advance);
    }

    default double getAttackDamageAttribute() {
        return AttributeAccess.attackDamageValue(asBukkitPlayer());
    }

    default boolean isBlocking() {
        return asBukkitPlayer().isBlocking();
    }

    default boolean isFireImmune() {
        return false;
    }

    default int getRemainingFireTicks() {
        return asBukkitPlayer().getFireTicks();
    }

    default void setRemainingFireTicks(int ticks) {
        asBukkitPlayer().setFireTicks(ticks);
    }

    default ItemStack getItem(EquipmentSlot slot) {
        return NMSBridgeManager.get().getBotItem(this, Objects.requireNonNull(slot, "slot"));
    }

    default void setItem(EquipmentSlot slot, @Nullable ItemStack stack) {
        NMSBridgeManager.get().setBotItem(this, Objects.requireNonNull(slot, "slot"), stack);
    }

    default ItemStack getItemInMainHand() {
        return getItem(EquipmentSlot.HAND);
    }

    default ItemStack getItemInOffHand() {
        return getItem(EquipmentSlot.OFF_HAND);
    }

    default void setItemInMainHand(@Nullable ItemStack stack) {
        setItem(EquipmentSlot.HAND, stack);
    }

    default void setItemInOffHand(@Nullable ItemStack stack) {
        setItem(EquipmentSlot.OFF_HAND, stack);
    }

    default boolean isUsingItem() {
        return asBukkitPlayer().isHandRaised();
    }

    default ItemStack activeItemStack() {
        return isUsingItem() ? getItemInMainHand() : ItemStackAccess.empty();
    }

    default void beginUsingItem(EquipmentSlot hand) {
        NMSBridgeManager.get().beginUsingBotItem(this, Objects.requireNonNull(hand, "hand"));
    }

    default void stopUsingItem() {
        NMSBridgeManager.get().stopUsingBotItem(this);
    }

    default void clearInventory() {
        NMSBridgeManager.get().clearBotInventory(this);
    }

    default boolean isSneaking() {
        return asBukkitPlayer().isSneaking();
    }

    default void setSneaking(boolean sneaking) {
        asBukkitPlayer().setSneaking(sneaking);
    }

    default boolean isSprinting() {
        return asBukkitPlayer().isSprinting();
    }

    default void setSprinting(boolean sprinting) {
        asBukkitPlayer().setSprinting(sprinting);
    }

    default void lookAt(Location location) {
        Location target = Objects.requireNonNull(location, "location");
        Player player = asBukkitPlayer();
        Location current = player.getEyeLocation();
        Vector direction = target.toVector().subtract(current.toVector());
        if (direction.lengthSquared() > 0.0D) {
            Location rotated = Objects.requireNonNull(player.getLocation(), "bot location");
            rotated.setDirection(direction);
            setRotation(rotated.getYaw(), rotated.getPitch());
        }
    }

    default void lookAt(Entity entity) {
        lookAt(Objects.requireNonNull(entity, "entity").getLocation());
    }
}
