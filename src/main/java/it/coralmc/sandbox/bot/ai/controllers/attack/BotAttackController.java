package it.coralmc.sandbox.bot.ai.controllers.attack;

import it.coralmc.sandbox.bot.ai.controllers.attack.helper.AttackExecutor;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.AttackStrategy;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.CooldownManager;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.JumpAttackManager;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.inter.IAttackStrategy;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import it.coralmc.sandbox.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import net.minecraft.world.entity.player.Player;

public class BotAttackController {

    private final Player bot;

    private final ICooldownManager cooldownManager;
    private final IAttackExecutor attackExecutor;
    private final IJumpAttackManager jumpAttackManager;
    private final IAttackStrategy attackStrategy;

    public BotAttackController(Player bot) {
        this.bot = bot;

        this.cooldownManager = new CooldownManager();
        this.attackExecutor = new AttackExecutor();
        this.jumpAttackManager = new JumpAttackManager(attackExecutor, cooldownManager);
        this.attackStrategy = new AttackStrategy(jumpAttackManager, attackExecutor, cooldownManager);
    }

    public void handleAttack(Player target) {
        if (!cooldownManager.canAttack()) {
            cooldownManager.tick();
            return;
        }

        attackStrategy.executeAttack(bot, target);
    }

    public void performNormalAttack(Player target) {
        attackExecutor.performNormalAttack(bot, target);
        cooldownManager.setRandomCooldown(20, 11);
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