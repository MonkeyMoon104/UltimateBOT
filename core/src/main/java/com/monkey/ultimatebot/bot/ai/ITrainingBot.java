package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.access.entity.AttributeAccess;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.access.player.PlayerAttackCooldownAccess;
import com.monkey.ultimatebot.access.player.PlayerHandRaisedAccess;
import com.monkey.ultimatebot.access.player.PlayerSwingAccess;
import com.monkey.ultimatebot.access.entity.VelocityAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

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

    Player asBukkitPlayer();

    void onDamaged(EntityDamageEvent event);

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
        com.monkey.ultimatebot.access.player.PlayerRotationAccess.set(asBukkitPlayer(), yaw, pitch);
    }

    default Vector getLookDirection() {
        return getLocation().getDirection();
    }

    default com.monkey.ultimatebot.access.entity.EntityBoundsAccess.Box bukkitBoundingBox() {
        return com.monkey.ultimatebot.access.entity.EntityBoundsAccess.of(asBukkitPlayer());
    }

    default boolean isAlive() {
        return !asBukkitPlayer().isDead() && asBukkitPlayer().getHealth() > 0.0D;
    }

    default boolean isRemoved() {
        return !asBukkitPlayer().isValid();
    }

    default double healthValue() {
        return asBukkitPlayer().getHealth();
    }

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

    default void prepareFullAttackStrength() {}

    default void attackEntity(Entity target) {
        Entity checked = Objects.requireNonNull(target, "target");
        prepareFullAttackStrength();
        com.monkey.ultimatebot.access.combat.AttackStrengthAccess.chargeFull(asBukkitPlayer());
        if (checked instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) checked;
            NMSBridgeManager.get().attackTarget(this, living);
            return;
        }

        NMSBridgeManager.get().attackEntity(this, checked);
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

    default ItemStack getItem(EquipmentSlotKind slot) {
        return NMSBridgeManager.get().getBotItem(this, Objects.requireNonNull(slot, "slot"));
    }

    default void setItem(EquipmentSlotKind slot, @Nullable ItemStack stack) {
        NMSBridgeManager.get().setBotItem(this, Objects.requireNonNull(slot, "slot"), stack);
    }

    default ItemStack getItemInMainHand() {
        return getItem(EquipmentSlotKind.HAND);
    }

    default ItemStack getItemInOffHand() {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        return offHand == null ? ItemStackAccess.empty() : getItem(offHand);
    }

    default void setItemInMainHand(@Nullable ItemStack stack) {
        setItem(EquipmentSlotKind.HAND, stack);
    }

    default void setItemInOffHand(@Nullable ItemStack stack) {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand != null) {
            setItem(offHand, stack);
        }
    }

    default boolean isUsingItem() {
        return PlayerHandRaisedAccess.isRaised(asBukkitPlayer());
    }

    default ItemStack activeItemStack() {
        return isUsingItem() ? getItemInMainHand() : ItemStackAccess.empty();
    }

    default void beginUsingItem(EquipmentSlotKind hand) {
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
