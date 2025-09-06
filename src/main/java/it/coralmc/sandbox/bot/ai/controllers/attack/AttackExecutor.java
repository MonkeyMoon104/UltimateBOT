package it.coralmc.sandbox.bot.ai.controllers.attack;

import it.coralmc.sandbox.bot.ai.controllers.attack.inter.IAttackExecutor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class AttackExecutor implements IAttackExecutor {

    @Override
    public void performCriticalAttack(Player bot, Player target) {
        try {
            float baseDamage = (float) bot.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            float criticalDamage = baseDamage * 1.5f;

            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) bot.level();

            target.hurtServer(serverLevel, bot.damageSources().playerAttack(bot), criticalDamage);

            bot.level().broadcastEntityEvent(target, (byte) 4);

            bot.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.0f);

        } catch (Exception e) {
            bot.attack(target);
        }
    }

    @Override
    public void performNormalAttack(Player bot, Player target) {
        bot.swing(InteractionHand.MAIN_HAND);
        bot.attack(target);
    }
}