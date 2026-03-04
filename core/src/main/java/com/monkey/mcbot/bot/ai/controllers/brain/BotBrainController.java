package com.monkey.mcbot.bot.ai.controllers.brain;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.BotAI;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BotBrainController {

    private final ITrainingBot bot;
    private final BotAI botAI;
    private org.bukkit.entity.Player targetPlayer;
    private final BotOptions botOptions;
    private boolean follow;
    private boolean combat;
    private final TargetingService targetingService;

    private Player cachedNmsTarget = null;
    private long lastNmsTargetUpdate = 0;
    private static final long NMS_CACHE_TIME = 100;

    public BotBrainController(ITrainingBot bot, SandboxTraining plugin,
                              org.bukkit.entity.Player targetPlayer, boolean follow, BotOptions botOptions) {
        this.targetingService = plugin.getTargetingService();
        this.bot = bot;
        this.targetPlayer = targetPlayer;
        this.follow = follow;
        this.botAI = new BotAI(bot.asPlayer(), plugin, botOptions);
        this.botOptions = botOptions;
        this.combat = botOptions.isCombat();
        configureBotAI();
    }

    private void configureBotAI() {
        if (targetPlayer != null && follow) {
            Player target = ((CraftPlayer) targetPlayer).getHandle();
            botAI.getRotationController().setInstantRotation(target);
        }
    }

    public void onTick() {
        if (targetPlayer == null || targetPlayer.isDead() || !targetPlayer.isOnline()) {
            return;
        }

        if (botOptions.isEventBot()) {
            updateTargetIfEventBot();
        }

        if (!follow) {
            return;
        }

        Player target = getNMSTarget();
        if (target == null) return;

        botAI.getRotationController().updateRotation(target);
        botAI.tick(targetPlayer);
    }

    private Player getNMSTarget() {
        long currentTime = System.currentTimeMillis();

        if (cachedNmsTarget != null && (currentTime - lastNmsTargetUpdate) < NMS_CACHE_TIME) {
            return cachedNmsTarget;
        }

        lastNmsTargetUpdate = currentTime;
        if (targetPlayer != null) {
            cachedNmsTarget = ((CraftPlayer) targetPlayer).getHandle();
        } else {
            cachedNmsTarget = null;
        }

        return cachedNmsTarget;
    }

    private void updateTargetIfEventBot() {
        org.bukkit.entity.Player newTarget = targetingService.findClosestPlayer(bot, 64.0);

        if (newTarget != null && newTarget != this.targetPlayer) {
            this.targetPlayer = newTarget;

            cachedNmsTarget = null;
            lastNmsTargetUpdate = 0;

            bot.getBotAI().getTeleportController().setTarget(newTarget);
        }
    }

    public BotAI getBotAI() {
        return botAI;
    }

    public org.bukkit.entity.Player getTargetPlayer() {
        return targetPlayer;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setCombat(boolean combat) {
        this.combat = combat;
    }

    public boolean isCombat() {
        return combat;
    }

    public BotOptions getBotOptions() {
        return botOptions;
    }
}