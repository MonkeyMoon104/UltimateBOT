package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/**
 * Bukkit-facing handle for the 1.9.2 fake player.
 *
 * <p>Kept separate from {@link EntityPlayer} so Spigot-mapped methods do not clash with
 * {@link ITrainingBot}.
 */
public final class TrainingBotHandle_v1_9_R1 implements ITrainingBot {

    private final TrainingBot_v1_9_R1 entity;
    private final TrainingBotLogic logic;
    private final UltimateBot plugin;

    TrainingBotHandle_v1_9_R1(
            TrainingBot_v1_9_R1 entity,
            UltimateBot plugin,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            BotOptions botOptions,
            String deadBotMessage,
            String deadBotEventMessage) {
        this.entity = entity;
        this.plugin = plugin;
        this.logic = new TrainingBotLogic(
                this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
        getBotAI().getTeleportController().setTarget(targetPlayer);
    }

    public EntityPlayer nativeEntity() {
        return entity;
    }

    void onNativeTick() {
        logic.onTick();
    }

    boolean onNativeDamaged(float amount, EntityDamageEvent event) {
        return logic.onDamaged(amount, event);
    }

    void onNativeDeath(org.bukkit.entity.@Nullable LivingEntity killer) {
        logic.onDeath(killer);
    }

    @Override
    public BotBrainController getBrainController() {
        return logic.getBrainController();
    }

    @Override
    public TotemTrackerService getTotemTracker() {
        return logic.getTotemTracker();
    }

    @Override
    public org.bukkit.entity.@Nullable Player getTargetPlayer() {
        return logic.getBrainController().getTargetPlayer();
    }

    @Override
    public void setTotemCount(int count) {
        logic.getTotemTracker().setTotemCount(count);
    }

    @Override
    public int getTotemCount() {
        return logic.getTotemTracker().getTotemCount();
    }

    @Override
    public void setFollow(boolean follow) {
        logic.getBrainController().setFollow(follow);
    }

    @Override
    public boolean isFollow() {
        return logic.getBrainController().isFollow();
    }

    @Override
    public void setCombat(boolean combat) {
        logic.getBrainController().setCombat(combat);
    }

    @Override
    public boolean isCombat() {
        return logic.getBrainController().isCombat();
    }

    @Override
    public void prepareFullAttackStrength() {
        entity.forceFullAttackStrength();
    }

    @Override
    public BotAI getBotAI() {
        return logic.getBrainController().getBotAI();
    }

    @Override
    public UltimateBot getPlugin() {
        return plugin;
    }

    @Override
    public org.bukkit.entity.Player asBukkitPlayer() {
        return entity.getBukkitEntity();
    }

    @Override
    public void onDamaged(EntityDamageEvent event) {
        if (logic.getBrainController().getBotAI().usesCustomBrain()) {
            logic.getBrainController().getBotAI().customBrainDamaged(event);
        }
    }

    @Override
    public void onDeath(org.bukkit.entity.@Nullable LivingEntity killer) {
        logic.onDeath(killer);
    }
}
