package com.monkey.mcbot.bot.ai.controllers.attack;

import com.monkey.mcbot.api.event.combat.BotAttackEvent;
import com.monkey.mcbot.api.event.combat.BotAttackType;
import com.monkey.mcbot.api.model.BotSnapshot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.AttackExecutor;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.AttackStrategy;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.CooldownManager;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.JumpAttackManager;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IAttackStrategy;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BotAttackController {

    private final Player bot;

    private final ICooldownManager cooldownManager;
    private final IAttackExecutor attackExecutor;
    private final IJumpAttackManager jumpAttackManager;
    private final IAttackStrategy attackStrategy;

    public BotAttackController(Player bot) {
        this.bot = java.util.Objects.requireNonNull(bot, "bot");

        this.cooldownManager = new CooldownManager();
        this.attackExecutor = new AttackExecutor();
        this.jumpAttackManager = new JumpAttackManager(attackExecutor, cooldownManager);
        this.attackStrategy = new AttackStrategy(jumpAttackManager, attackExecutor, cooldownManager);
    }

    public void handleAttack(LivingEntity target) {
        if (!cooldownManager.canAttack()) {
            cooldownManager.tick();
            return;
        }

        if (!allowAttack(target, BotAttackType.MELEE)) {
            cooldownManager.setCooldown(5);
            return;
        }
        attackStrategy.executeAttack(bot, target);
    }

    public void performNormalAttack(LivingEntity target) {
        if (!allowAttack(target, BotAttackType.MELEE)) {
            cooldownManager.setCooldown(5);
            return;
        }
        attackExecutor.performNormalAttack(bot, target);
        cooldownManager.setRandomCooldown(20, 11);
    }

    private boolean allowAttack(LivingEntity target, BotAttackType type) {
        if (!(bot instanceof ITrainingBot trainingBot) || target == null) return target != null;
        var plugin = trainingBot.getPlugin();
        var ownerUUID = plugin.getBotRegistry().getOwnerUUIDByBotUUID(bot.getUUID());
        if (ownerUUID == null || !(target.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity bukkitTarget)) {
            return true;
        }
        BotSnapshot snapshot = plugin.getBotEventDispatcher().snapshot(ownerUUID, trainingBot);
        if (snapshot == null) return true;
        BotAttackEvent event = plugin.getBotEventDispatcher()
                .publish(new BotAttackEvent(
                        plugin.getBotEventDispatcher().nextSequence(bot.getUUID()), snapshot, bukkitTarget, type));
        return !event.isCancelled();
    }

    public boolean canAttack() {
        return cooldownManager.canAttack();
    }

    public void setAttackCooldown(int cooldown) {
        cooldownManager.setCooldown(cooldown);
    }

    public int getAttackCooldown() {
        return cooldownManager.getCooldown();
    }

    public boolean isInJumpAttack() {
        return jumpAttackManager.isInJumpAttack();
    }
}
