package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        if (!bot.isUsingItem()) {
            bot.startUsingItem(InteractionHand.OFF_HAND);
        }
    }

    void releaseUseItem() {
        if (bot.isUsingItem()) {
            bot.releaseUsingItem();
        }
    }

    void applyInstantHealth(int amplifier) {
        bukkitBot.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, amplifier, false, false, false));
        Location location = Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
        bukkitBot.getWorld().spawnParticle(Particle.INSTANT_EFFECT, location, 18, 0.4D, 0.5D, 0.4D, 0.1D);
        bukkitBot.getWorld().playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.8F, 1.0F);
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
