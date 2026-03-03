package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper;

import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.IPearlThrower;
import com.monkey.mcbot.nms.NMSBridgeManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PearlThrower implements IPearlThrower {

    private final Level level;

    public PearlThrower(Level level) {
        this.level = level;
    }

    @Override
    public void throwEnderpearl(Player bot, Vec3 targetPos) {
        ThrownEnderpearl enderpearl = new ThrownEnderpearl(net.minecraft.world.entity.EntityType.ENDER_PEARL, level);
        enderpearl.setOwner(bot);

        Vec3 botPos = bot.position().add(0, bot.getEyeHeight(), 0);
        Vec3 direction = targetPos.subtract(botPos);
        double distance = direction.length();

        double velocityScale;
        if (distance < 15) {
            velocityScale = Math.min(distance * 0.08, 1.2);
        } else {
            velocityScale = Math.min(distance * 0.06, 1.8);
        }

        Vec3 velocity = direction.normalize().scale(velocityScale);

        double gravityCompensation = distance * 0.02;
        velocity = velocity.add(0, 0.2 + gravityCompensation, 0);

        enderpearl.setPos(botPos.x, botPos.y, botPos.z);
        enderpearl.setDeltaMovement(velocity);
        level.addFreshEntity(enderpearl);

        bot.swing(InteractionHand.MAIN_HAND);
        NMSBridgeManager.get().playSoundOnPlayer(
                bot,
                net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW,
                0.5f,
                0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
        );    }
}