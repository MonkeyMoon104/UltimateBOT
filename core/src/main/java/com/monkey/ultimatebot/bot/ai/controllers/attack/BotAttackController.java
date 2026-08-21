package com.monkey.ultimatebot.bot.ai.controllers.attack;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.combat.BotAttackEvent;
import com.monkey.ultimatebot.api.event.combat.BotAttackType;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.AttackExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.AttackStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.CooldownManager;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.JumpAttackManager;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IAttackStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import org.bukkit.entity.LivingEntity;

public class BotAttackController {

    private final ITrainingBot bot;

    private final ICooldownManager cooldownManager;
    private final IAttackExecutor attackExecutor;
    private final IJumpAttackManager jumpAttackManager;
    private final IAttackStrategy attackStrategy;

    public BotAttackController(ITrainingBot bot) {
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
        if (target == null || !target.isValid() || target.isDead()) {
            return false;
        }
        UltimateBot plugin = bot.getPlugin();
        java.util.UUID ownerUUID = plugin.getBotRegistry().getOwnerUUIDByBotUUID(bot.getUniqueId());
        if (ownerUUID == null) {
            return true;
        }
        BotSnapshot snapshot = plugin.getBotEventDispatcher().snapshot(ownerUUID, bot);
        if (snapshot == null) return true;
        BotAttackEvent event = plugin.getBotEventDispatcher()
                .publish(new BotAttackEvent(
                        plugin.getBotEventDispatcher().nextSequence(bot.getUniqueId()), snapshot, target, type));
        return !event.isCancelled();
    }

    public boolean canAttack() {
        return cooldownManager.canAttack();
    }

    public void tickAttackCooldown() {
        if (!cooldownManager.canAttack()) {
            cooldownManager.tick();
        }
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
