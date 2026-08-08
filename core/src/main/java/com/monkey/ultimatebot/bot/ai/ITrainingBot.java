package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
        return asBukkitPlayer().getVelocity();
    }

    default void setBukkitVelocity(Vector velocity) {
        asBukkitPlayer().setVelocity(Objects.requireNonNull(velocity, "velocity"));
    }

    default boolean isOnGround() {
        return asBukkitPlayer().getVelocity().getY() == 0.0D;
    }

    default void setOnGround(boolean onGround) {
        // Paper exposes setOnGround on CraftEntity; fall back is a no-op if unavailable.
        asBukkitPlayer().setGravity(!onGround || asBukkitPlayer().hasGravity());
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
        Player player = asBukkitPlayer();
        Location location = Objects.requireNonNull(player.getLocation(), "bot location");
        location.setYaw(yaw);
        location.setPitch(pitch);
        player.setRotation(yaw, pitch);
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
        AttributeInstance attribute = asBukkitPlayer().getAttribute(Attribute.MAX_HEALTH);
        return attribute != null ? attribute.getValue() : asBukkitPlayer().getHealth();
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

    default void attackEntity(Entity target) {
        asBukkitPlayer().attack(Objects.requireNonNull(target, "target"));
    }

    default void swingMainHand() {
        asBukkitPlayer().swingMainHand();
    }

    default void swingOffHand() {
        asBukkitPlayer().swingOffHand();
    }

    default float getAttackStrengthScale(float advance) {
        return asBukkitPlayer().getAttackCooldown();
    }

    default double getAttackDamageAttribute() {
        AttributeInstance attribute = asBukkitPlayer().getAttribute(Attribute.ATTACK_DAMAGE);
        return attribute != null ? attribute.getValue() : 1.0D;
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
        PlayerInventory inventory = asBukkitPlayer().getInventory();
        return switch (Objects.requireNonNull(slot, "slot")) {
            case HAND -> inventory.getItemInMainHand();
            case OFF_HAND -> inventory.getItemInOffHand();
            case FEET -> nullToAir(inventory.getBoots());
            case LEGS -> nullToAir(inventory.getLeggings());
            case CHEST -> nullToAir(inventory.getChestplate());
            case HEAD -> nullToAir(inventory.getHelmet());
            default -> {
                EntityEquipment equipment = asBukkitPlayer().getEquipment();
                yield equipment != null ? nullToAir(equipment.getItem(slot)) : ItemStack.empty();
            }
        };
    }

    default void setItem(EquipmentSlot slot, @Nullable ItemStack stack) {
        PlayerInventory inventory = asBukkitPlayer().getInventory();
        ItemStack value = stack == null ? ItemStack.empty() : stack;
        switch (Objects.requireNonNull(slot, "slot")) {
            case HAND -> inventory.setItemInMainHand(value);
            case OFF_HAND -> inventory.setItemInOffHand(value);
            case FEET -> inventory.setBoots(value);
            case LEGS -> inventory.setLeggings(value);
            case CHEST -> inventory.setChestplate(value);
            case HEAD -> inventory.setHelmet(value);
            default -> {
                EntityEquipment equipment = asBukkitPlayer().getEquipment();
                if (equipment != null) {
                    equipment.setItem(slot, value);
                }
            }
        }
    }

    default ItemStack getItemInMainHand() {
        return asBukkitPlayer().getInventory().getItemInMainHand();
    }

    default ItemStack getItemInOffHand() {
        return asBukkitPlayer().getInventory().getItemInOffHand();
    }

    default void setItemInMainHand(@Nullable ItemStack stack) {
        asBukkitPlayer().getInventory().setItemInMainHand(stack == null ? ItemStack.empty() : stack);
    }

    default void setItemInOffHand(@Nullable ItemStack stack) {
        asBukkitPlayer().getInventory().setItemInOffHand(stack == null ? ItemStack.empty() : stack);
    }

    default boolean isUsingItem() {
        return asBukkitPlayer().isHandRaised();
    }

    default ItemStack activeItemStack() {
        return isUsingItem() ? getItemInMainHand() : ItemStack.empty();
    }

    default void beginUsingItem(EquipmentSlot hand) {
        asBukkitPlayer()
                .startUsingItem(Objects.requireNonNull(hand, "hand") == EquipmentSlot.OFF_HAND
                        ? EquipmentSlot.OFF_HAND
                        : EquipmentSlot.HAND);
    }

    default void stopUsingItem() {
        asBukkitPlayer().clearActiveItem();
    }

    default void clearInventory() {
        asBukkitPlayer().getInventory().clear();
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
            player.setRotation(rotated.getYaw(), rotated.getPitch());
        }
    }

    default void lookAt(Entity entity) {
        lookAt(Objects.requireNonNull(entity, "entity").getLocation());
    }

    private static ItemStack nullToAir(@Nullable ItemStack stack) {
        return stack == null ? ItemStack.empty() : stack;
    }
}
