package it.coralmc.sandbox.bot.ai.handlers;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.bot.ai.BotAI;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import net.minecraft.world.entity.player.Player;

public class BotAIController {

    private final TrainingBot bot;
    private final BotAI botAI;
    private org.bukkit.entity.Player targetPlayer;
    private final BotOptions botOptions;
    private boolean follow;

    public BotAIController(TrainingBot bot, SandboxTraining plugin,
                           org.bukkit.entity.Player targetPlayer, boolean follow, BotOptions botOptions) {
        this.bot = bot;
        this.targetPlayer = targetPlayer;
        this.follow = follow;
        this.botAI = new BotAI(bot, plugin);
        this.botOptions = botOptions;
        configureBotAI();
    }

    private void configureBotAI() {
        if (targetPlayer != null && follow) {
            Player target = ((org.bukkit.craftbukkit.entity.CraftPlayer) targetPlayer).getHandle();
            botAI.getRotationController().setInstantRotation(target);
        }
    }

    public void onTick() {
        if (targetPlayer != null && !targetPlayer.isDead()) {
            Player target = ((org.bukkit.craftbukkit.entity.CraftPlayer) targetPlayer).getHandle();
            botAI.getRotationController().updateRotation(target);
        }
        if (follow) {
            botAI.tick(targetPlayer);
        }
    }

    public BotAI getBotAI() { return botAI; }
    public org.bukkit.entity.Player getTargetPlayer() { return targetPlayer; }
    public void setFollow(boolean follow) { this.follow = follow; }
    public boolean isFollow() { return follow; }
    public BotOptions getBotOptions() {
        return botOptions;
    }
}
