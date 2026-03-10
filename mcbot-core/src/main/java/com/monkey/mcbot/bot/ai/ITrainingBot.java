package com.monkey.mcbot.bot.ai;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.mcbot.bot.ai.services.TotemTrackerService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public interface ITrainingBot {

    boolean isCombat();
    void setCombat(boolean combat);

    boolean isFollow();
    void setFollow(boolean follow);

    int getTotemCount();
    void setTotemCount(int count);

    org.bukkit.entity.Player getTargetPlayer();

    BotBrainController getBrainController();
    BotAI getBotAI();
    TotemTrackerService getTotemTracker();

    MinecraftBot getPlugin();

    boolean callSuperActuallyHurt(ServerLevel level, DamageSource source,
                                  float amount, EntityDamageEvent event);

    Player asPlayer();
}