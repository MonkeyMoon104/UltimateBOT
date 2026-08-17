package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.compat.AttributeAccess;
import com.monkey.ultimatebot.compat.CombatCadenceAccess;
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import com.monkey.ultimatebot.compat.ParticleAccess;
import com.monkey.ultimatebot.compat.PlayerAttackCooldownAccess;
import com.monkey.ultimatebot.compat.PlayerHandRaisedAccess;
import com.monkey.ultimatebot.compat.PotionEffectAccess;
import com.monkey.ultimatebot.compat.PotionEffectTypeAccess;
import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public final class ModeCombatActions {
    private final ITrainingBot bot;
    private final Player bukkitBot;
    private final BotAttackController attack;
    private final BotInventoryController inventory;
    private final Supplier<CombatTuning> tuning;

    ModeCombatActions(
            ITrainingBot bot,
            Player bukkitBot,
            BotAttackController attack,
            BotInventoryController inventory,
            Supplier<CombatTuning> tuning) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.bukkitBot = Objects.requireNonNull(bukkitBot, "bukkitBot");
        this.attack = Objects.requireNonNull(attack, "attack");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.tuning = Objects.requireNonNull(tuning, "tuning");
    }

    public double healthRatio() {
        return bot.healthValue() / bot.maxHealthValue();
    }

    public double targetHealthRatio(LivingEntity target) {
        return target.getHealth() / AttributeAccess.maxHealthValue(target);
    }

    public boolean isTargetBlocking(LivingEntity target) {
        return target instanceof Player && ((Player) target).isBlocking();
    }

    public void defendWithOffhand() {
        EquipmentSlot offHand = EquipmentSlotAccess.offHand();
        if (offHand != null) {
            inventory.startUsingItem(offHand);
        }
    }

    public boolean isDefendingWithOffhand() {
        return bot.isUsingItem();
    }

    public void useMainhandItem() {
        inventory.startUsingItem(EquipmentSlot.HAND);
    }

    public boolean isIncomingAttackLikely(LivingEntity target) {
        Location botLocation = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        Location targetLocation = Objects.requireNonNull(target.getLocation(), "target location");
        double distance = botLocation.distance(targetLocation);
        if (!(target instanceof Player)) {
            return distance <= 3.6D;
        }
        Player player = (Player) target;

        Location playerLocation = Objects.requireNonNull(player.getLocation(), "player location");
        Vector towardBot = botLocation.toVector().subtract(playerLocation.toVector()).setY(0.0D);
        if (towardBot.lengthSquared() < 0.001D) {
            return distance <= 3.2D && !PlayerHandRaisedAccess.isRaised(player);
        }
        Vector direction = towardBot.normalize();
        double closingSpeed =
                player.getVelocity().clone().setY(0.0D).dot(direction);
        Vector look = playerLocation.getDirection().setY(0.0D);
        double facingDot = look.lengthSquared() < 0.001D ? 0.0D : look.normalize().dot(direction);
        return ModeCombatPolicy.isIncomingPlayerAttack(
                distance,
                PlayerAttackCooldownAccess.get(player),
                player.isBlocking(),
                PlayerHandRaisedAccess.isRaised(player),
                closingSpeed,
                facingDot);
    }

    public boolean canAttack() {
        return attack.canAttack();
    }

    public void tickAttackCooldown() {
        attack.tickAttackCooldown();
    }

    public void releaseUseItem() {
        inventory.releaseUsingItem();
    }

    public void swingMainHand() {
        bot.swingMainHand();
    }

    public void applyInstantHealth(int amplifier) {
        int safeAmplifier = Math.max(0, amplifier);
        double before = bot.healthValue();
        bukkitBot.addPotionEffect(
                PotionEffectAccess.of(PotionEffectTypeAccess.instantHealth(), 1, safeAmplifier, false, false, false));
        // Splash items are color-only. Instant Health as an effect can no-op on fake players
        // (RegainHealth cancelled, or heal() skipped at 0 HP). Gapple already uses setHealth.
        if (bot.healthValue() <= before + 0.01D) {
            bot.setHealthValue(before + (double) (4 << Math.min(safeAmplifier, 8)));
        }
        Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        bukkitBot.getWorld().spawnParticle(ParticleAccess.heart(), location, 18, 0.4D, 0.5D, 0.4D, 0.1D);
        bukkitBot.getWorld().playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.8F, 1.0F);
    }

    public void applyCombatBuffs() {
        bukkitBot.addPotionEffect(PotionEffectAccess.of(PotionEffectTypeAccess.speed(), 3600, 1, false, false, false));
        bukkitBot.addPotionEffect(
                PotionEffectAccess.of(PotionEffectTypeAccess.strength(), 3600, 0, false, false, false));
        Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        bukkitBot.getWorld().spawnParticle(ParticleAccess.witch(), location, 24, 0.35D, 0.7D, 0.35D, 0.08D);
        bukkitBot.getWorld().playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.8F, 1.15F);
    }

    public void consumeGoldenApple(int slot) {
        if (!inventory.consumeItem(slot)) {
            return;
        }
        inventory.switchToSlot(slot);
        bot.setHealthValue(Math.min(bot.maxHealthValue(), bot.healthValue() + 4.0D));
        bukkitBot.addPotionEffect(
                PotionEffectAccess.of(PotionEffectTypeAccess.regeneration(), 100, 1, false, false, false));
        bukkitBot.addPotionEffect(
                PotionEffectAccess.of(PotionEffectTypeAccess.absorption(), 1200, 0, false, false, false));
        if (MinecraftVersionAccess.isAtLeast(1, 9)) {
            Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
            bukkitBot.getWorld().playSound(location, Sound.ENTITY_GENERIC_EAT, 0.8F, 1.0F);
        }
    }

    public void attack(LivingEntity target, int slot) {
        if (inventory.getCurrentSlot() != slot) {
            inventory.switchToSlot(slot);
        }
        attack.handleAttack(target);
        applySwingDelay();
    }

    public void attackNormally(LivingEntity target, int slot) {
        if (inventory.getCurrentSlot() != slot) {
            inventory.switchToSlot(slot);
        }
        if (!attack.canAttack()) {
            attack.tickAttackCooldown();
            return;
        }
        attack.performNormalAttack(target);
        applySwingDelay();
    }

    /** Direct melee without jump-crit orchestration (e.g. aerial mace smash). */
    public void forceMelee(LivingEntity target, int slot) {
        if (inventory.getCurrentSlot() != slot) {
            inventory.switchToSlot(slot);
        }
        attack.setAttackCooldown(0);
        attack.performNormalAttack(target);
        applySwingDelay();
    }

    private void applySwingDelay() {
        int delay = CombatCadenceAccess.swingDelayTicks(tuning.get().attackCooldownTicks());
        if (attack.getAttackCooldown() > delay) {
            attack.setAttackCooldown(delay);
        }
    }
}
