package it.coralmc.sandbox.bot.ai.controllers.attack.helper;

import it.coralmc.sandbox.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class AttackExecutor implements IAttackExecutor {

    private float cachedBaseDamage = -1;
    private Player lastDamageCalculatedBot = null;
    private net.minecraft.server.level.ServerLevel cachedServerLevel = null;
    private Player lastServerLevelBot = null;

    @Override
    public void performCriticalAttack(Player bot, Player target) {
        try {
            float baseDamage = getCachedBaseDamage(bot);
            float criticalDamage = baseDamage * 1.5f;

            net.minecraft.server.level.ServerLevel serverLevel = getCachedServerLevel(bot);

            target.hurtServer(serverLevel, bot.damageSources().playerAttack(bot), criticalDamage);



        } catch (Exception e) {
            performNormalAttack(bot, target);
        }
    }

    @Override
    public void performNormalAttack(Player bot, Player target) {
        bot.attack(target);
        bot.swing(InteractionHand.MAIN_HAND);
    }

    private float getCachedBaseDamage(Player bot) {
        if (lastDamageCalculatedBot != bot || cachedBaseDamage < 0) {
            cachedBaseDamage = (float) bot.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            lastDamageCalculatedBot = bot;
        }
        return cachedBaseDamage;
    }

    private net.minecraft.server.level.ServerLevel getCachedServerLevel(Player bot) {
        if (lastServerLevelBot != bot || cachedServerLevel == null) {
            cachedServerLevel = (net.minecraft.server.level.ServerLevel) bot.level();
            lastServerLevelBot = bot;
        }
        return cachedServerLevel;
    }

    public void invalidateCache() {
        cachedBaseDamage = -1;
        lastDamageCalculatedBot = null;
        cachedServerLevel = null;
        lastServerLevelBot = null;
    }
}