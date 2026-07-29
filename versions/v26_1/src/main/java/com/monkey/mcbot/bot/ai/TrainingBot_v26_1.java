package com.monkey.mcbot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.mcbot.bot.ai.fakeplayer.BotCraftPlayer;
import com.monkey.mcbot.bot.ai.services.TotemTrackerService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public final class TrainingBot_v26_1 extends Player implements ITrainingBot {

    private final TrainingBotLogic logic;
    private final BotCraftPlayer craftEntity;
    private final MinecraftBot plugin;

    public TrainingBot_v26_1(Level level,
                               BlockPos pos,
                               float yRot,
                               GameProfile gameProfile,
                               org.bukkit.entity.Player targetPlayer,
                               boolean follow,
                               MinecraftBot plugin,
                               String deadBotMessage,
                               String deadBotEventMessage,
                               BotOptions botOptions) {

        super(level, gameProfile);

        this.snapTo(
                pos.getX() + 0.5,
                pos.getY() + 1,
                pos.getZ() + 0.5,
                yRot,
                0.0F
        );

        this.setNoGravity(false);
        this.setOnGround(false);
        this.plugin = plugin;
        this.craftEntity = new BotCraftPlayer(this);

        this.logic = new TrainingBotLogic(
                this, plugin, targetPlayer, follow,
                botOptions, deadBotMessage, deadBotEventMessage
        );

        getBotAI().getTeleportController().setTarget(targetPlayer);
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (ClassCastException ignored) {}
        craftEntity.setHandle(this);
        logic.onTick();
    }

    @Override
    public void die(DamageSource cause) {
        this.getInventory().clearContent();
        super.die(cause);
        logic.onDeath(cause);
    }

    @Override
    protected boolean actuallyHurt(ServerLevel level, DamageSource source,
                                   float amount, EntityDamageEvent event) {
        return logic.onActuallyHurt(level, source, amount, event);
    }

    @Override
    public void aiStep() {
        try {
            super.aiStep();
        } catch (ClassCastException ignored) {}
    }

    @Override
    public void completeUsingItem() {
        try {
            super.completeUsingItem();
        } catch (ClassCastException e) {
            if (this.getUseItem().getItem() == net.minecraft.world.item.Items.GOLDEN_APPLE) {
                getBotAI().getHealController().applyEffect();
            }
            this.releaseUsingItem();
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (player != this) super.playerTouch(player);
    }

    @Override
    public CraftHumanEntity getBukkitEntity() {
        craftEntity.setHandle(this);
        return craftEntity;
    }

    @Override public boolean isSpectator()      { return false; }
    @Override public boolean isCreative()        { return false; }
    @Override public boolean canPickUpLoot()     { return false; }
    @Override public boolean isPushable()        { return false; }

    @Override public BotBrainController getBrainController()    { return logic.getBrainController(); }
    @Override public TotemTrackerService getTotemTracker()      { return logic.getTotemTracker(); }
    @Override public org.bukkit.entity.Player getTargetPlayer() { return logic.getBrainController().getTargetPlayer(); }
    @Override public void setTotemCount(int count)              { logic.getTotemTracker().setTotemCount(count); }
    @Override public int getTotemCount()                        { return logic.getTotemTracker().getTotemCount(); }
    @Override public void setFollow(boolean f)                  { logic.getBrainController().setFollow(f); }
    @Override public boolean isFollow()                         { return logic.getBrainController().isFollow(); }
    @Override public void setCombat(boolean c)                  { logic.getBrainController().setCombat(c); }
    @Override public boolean isCombat()                         { return logic.getBrainController().isCombat(); }
    @Override public BotAI getBotAI()                           { return logic.getBrainController().getBotAI(); }
    @Override public MinecraftBot getPlugin()                { return plugin; }
    @Override public Player asPlayer()                          { return this; }

    @Override
    public boolean callSuperActuallyHurt(ServerLevel level, DamageSource source,
                                         float amount, EntityDamageEvent event) {
        return super.actuallyHurt(level, source, amount, event);
    }

    @Override
    public net.minecraft.world.level.GameType gameMode() {
        return net.minecraft.world.level.GameType.SURVIVAL;
    }
}

