package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class ModeCombatActions {
    private final Player bot;
    private final org.bukkit.entity.Player bukkitBot;
    private final BotAttackController attack;
    private final BotInventoryController inventory;
    private final Supplier<CombatTuning> tuning;

    ModeCombatActions(
            Player bot,
            org.bukkit.entity.Player bukkitBot,
            BotAttackController attack,
            BotInventoryController inventory,
            Supplier<CombatTuning> tuning) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.bukkitBot = Objects.requireNonNull(bukkitBot, "bukkitBot");
        this.attack = Objects.requireNonNull(attack, "attack");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.tuning = Objects.requireNonNull(tuning, "tuning");
    }

    double healthRatio() {
        return bot.getHealth() / bot.getMaxHealth();
    }

    double targetHealthRatio(LivingEntity target) {
        return target.getHealth() / target.getMaxHealth();
    }

    boolean isTargetBlocking(LivingEntity target) {
        return target instanceof Player player && player.isBlocking();
    }

    void defendWithOffhand() {
        inventory.startUsingItem(InteractionHand.OFF_HAND);
    }

    boolean isDefendingWithOffhand() {
        return bot.isUsingItem() && bot.getUsedItemHand() == InteractionHand.OFF_HAND;
    }

    void useMainhandItem() {
        inventory.startUsingItem(InteractionHand.MAIN_HAND);
    }

    boolean isIncomingAttackLikely(LivingEntity target) {
        double distance = bot.distanceTo(target);
        if (!(target instanceof Player player)) {
            return distance <= 3.6D;
        }

        Vec3 towardBot = bot.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (towardBot.lengthSqr() < 0.001D) {
            return distance <= 3.2D && !player.isUsingItem();
        }
        Vec3 direction = towardBot.normalize();
        double closingSpeed =
                player.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).dot(direction);
        double facingDot =
                player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().dot(direction);
        return ModeCombatPolicy.isIncomingPlayerAttack(
                distance,
                player.getAttackStrengthScale(0.5F),
                player.isBlocking(),
                player.isUsingItem(),
                closingSpeed,
                facingDot);
    }

    boolean canAttack() {
        return attack.canAttack();
    }

    void tickAttackCooldown() {
        attack.tickAttackCooldown();
    }

    void releaseUseItem() {
        inventory.releaseUsingItem();
    }

    void swingMainHand() {
        bukkitBot.swingMainHand();
    }

    void applyInstantHealth(int amplifier) {
        bukkitBot.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, amplifier, false, false, false));
        Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        bukkitBot.getWorld().spawnParticle(Particle.HEART, location, 18, 0.4D, 0.5D, 0.4D, 0.1D);
        bukkitBot.getWorld().playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.8F, 1.0F);
    }

    void applyCombatBuffs() {
        bukkitBot.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3600, 1, false, false, false));
        bukkitBot.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3600, 0, false, false, false));
        Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        bukkitBot.getWorld().spawnParticle(Particle.WITCH, location, 24, 0.35D, 0.7D, 0.35D, 0.08D);
        bukkitBot.getWorld().playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.8F, 1.15F);
    }

    void consumeGoldenApple(int slot) {
        if (!inventory.consumeItem(slot)) {
            return;
        }
        inventory.switchToSlot(slot);
        bot.setHealth((float) Math.min(bot.getMaxHealth(), bot.getHealth() + 4.0D));
        bukkitBot.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false, false));
        bukkitBot.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0, false, false, false));
        Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        bukkitBot.getWorld().playSound(location, Sound.ENTITY_GENERIC_EAT, 0.8F, 1.0F);
    }

    void attack(LivingEntity target, int slot) {
        if (inventory.getCurrentSlot() != slot) {
            inventory.switchToSlot(slot);
        }
        attack.handleAttack(target);
        if (attack.getAttackCooldown() > tuning.get().attackCooldownTicks()) {
            attack.setAttackCooldown(tuning.get().attackCooldownTicks());
        }
    }
}
