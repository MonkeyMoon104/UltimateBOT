package it.coralmc.sandbox.bot.ai.controllers.attack;

import it.coralmc.sandbox.bot.ai.controllers.attack.inter.IAttackExecutor;
import it.coralmc.sandbox.bot.ai.controllers.attack.inter.IAttackStrategy;
import it.coralmc.sandbox.bot.ai.controllers.attack.inter.ICooldownManager;
import it.coralmc.sandbox.bot.ai.controllers.attack.inter.IJumpAttackManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class AttackStrategy implements IAttackStrategy {

    private final IJumpAttackManager jumpAttackManager;
    private final IAttackExecutor attackExecutor;
    private final ICooldownManager cooldownManager;

    public AttackStrategy(IJumpAttackManager jumpAttackManager, IAttackExecutor attackExecutor, ICooldownManager cooldownManager) {
        this.jumpAttackManager = jumpAttackManager;
        this.attackExecutor = attackExecutor;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public void executeAttack(Player bot, Player target) {
        if (jumpAttackManager.isInJumpAttack()) {
            jumpAttackManager.handleJumpAttack(bot, target);
            return;
        }

        if (bot.onGround()) {
            jumpAttackManager.initiateJumpAttack(bot);
        } else if (!bot.onGround()) {
            bot.swing(InteractionHand.MAIN_HAND);
            attackExecutor.performCriticalAttack(bot, target);
            cooldownManager.setRandomCooldown(20, 11);
        }
    }
}