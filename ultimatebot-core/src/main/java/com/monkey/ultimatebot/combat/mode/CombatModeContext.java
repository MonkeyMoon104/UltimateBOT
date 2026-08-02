package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

final class CombatModeContext implements AutoCloseable {
    private final Player bot;
    private final org.bukkit.entity.Player bukkitBot;
    private final BotOptions options;
    private final BotMovementController movement;
    private final BotRotationController rotation;
    private final BotAttackController attack;
    private final BotInventoryController inventory;
    private final BotCPVPController crystal;
    private final BotRAPVPController anchor;
    private final ICombatStrategyExecutor legacyCombat;
    private final ModeInventorySession inventorySession;
    private final ModeEntityTracker entities;
    private final ModeProjectileService projectiles;
    private final RandomGenerator random;

    CombatModeContext(
            Player bot,
            BotOptions options,
            BotMovementController movement,
            BotRotationController rotation,
            BotAttackController attack,
            BotInventoryController inventory,
            BotCPVPController crystal,
            BotRAPVPController anchor,
            ICombatStrategyExecutor legacyCombat) {
        this.bot = Objects.requireNonNull(bot, "bot");
        if (!(bot.getBukkitEntity() instanceof org.bukkit.entity.Player player)) {
            throw new IllegalArgumentException("Combat bot must expose a Bukkit player entity");
        }
        this.bukkitBot = player;
        this.options = Objects.requireNonNull(options, "options");
        this.movement = Objects.requireNonNull(movement, "movement");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.attack = Objects.requireNonNull(attack, "attack");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.crystal = Objects.requireNonNull(crystal, "crystal");
        this.anchor = Objects.requireNonNull(anchor, "anchor");
        this.legacyCombat = Objects.requireNonNull(legacyCombat, "legacyCombat");
        this.inventorySession = new ModeInventorySession(inventory);
        this.entities = new ModeEntityTracker();
        this.random = new SplittableRandom(
                bot.getUUID().getMostSignificantBits() ^ bot.getUUID().getLeastSignificantBits());
        this.projectiles = new ModeProjectileService(bukkitBot, entities, random);
    }

    CombatTuning tuning() {
        return options.getCombatTuning();
    }

    BotOptions options() {
        return options;
    }

    Player bot() {
        return bot;
    }

    org.bukkit.entity.Player bukkitBot() {
        return bukkitBot;
    }

    BotInventoryController inventory() {
        return inventory;
    }

    BotMovementController movement() {
        return movement;
    }

    BotCPVPController crystal() {
        return crystal;
    }

    BotRAPVPController anchor() {
        return anchor;
    }

    ModeProjectileService projectiles() {
        return projectiles;
    }

    ModeEntityTracker entities() {
        return entities;
    }

    void clearTransientEntities() {
        entities.close();
    }

    RandomGenerator random() {
        return random;
    }

    void applyKit(ModeKit kit) {
        inventorySession.apply(kit);
    }

    void aimAt(LivingEntity target) {
        rotation.updateRotation(target);
    }

    double distanceTo(LivingEntity target) {
        return bot.distanceTo(target);
    }

    double healthRatio() {
        return bot.getHealth() / bot.getMaxHealth();
    }

    void approach(LivingEntity target, double desiredDistance) {
        moveRelativeTo(target, desiredDistance, false);
    }

    void retreat(LivingEntity target, double desiredDistance) {
        moveRelativeTo(target, desiredDistance, true);
    }

    void strafe(LivingEntity target, double strength) {
        Vec3 direction = target.position().subtract(bot.position()).normalize();
        double side = random.nextBoolean() ? 1.0D : -1.0D;
        movement.moveToPosition(bot.position().add(new Vec3(-direction.z, 0.0D, direction.x).scale(strength * side)));
    }

    void attack(LivingEntity target, int slot) {
        if (inventory.getCurrentSlot() != slot) {
            inventory.switchToSlot(slot);
        }
        attack.handleAttack(target);
        if (attack.getAttackCooldown() > tuning().attackCooldownTicks()) {
            attack.setAttackCooldown(tuning().attackCooldownTicks());
        }
    }

    void legacyCombat(Player target) {
        legacyCombat.executeCombatStrategy(target);
    }

    private void moveRelativeTo(LivingEntity target, double desiredDistance, boolean away) {
        Vec3 delta = target.position().subtract(bot.position());
        double horizontalDistance = Math.hypot(delta.x, delta.z);
        if (horizontalDistance < 0.001D) {
            movement.stopMovement();
            return;
        }
        double direction = away ? -1.0D : 1.0D;
        double travel = away
                ? Math.max(1.0D, desiredDistance - horizontalDistance)
                : Math.max(0.0D, horizontalDistance - desiredDistance);
        Vec3 destination = bot.position()
                .add(
                        delta.x / horizontalDistance * travel * direction,
                        0.0D,
                        delta.z / horizontalDistance * travel * direction);
        movement.setMovementSpeed(tuning().movementSpeed());
        movement.moveToPosition(destination);
    }

    @Override
    public void close() {
        if (bot.isUsingItem()) {
            bot.releaseUsingItem();
        }
        anchor.disable();
        crystal.setEnabled(false);
        entities.close();
        inventorySession.close();
        movement.resetCombatState();
    }
}
