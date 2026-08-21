package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_21_1.VersionedBotCraftPlayer;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public final class TrainingBot_v1_21_1 extends Player implements ITrainingBot {

    private final TrainingBotLogic logic;
    private final VersionedBotCraftPlayer craftEntity;
    private final UltimateBot plugin;

    public TrainingBot_v1_21_1(
            Level level,
            BlockPos pos,
            float yRot,
            GameProfile gameProfile,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {

        super(level, pos, yRot, gameProfile);

        this.setNoGravity(false);
        this.setOnGround(false);
        this.plugin = plugin;
        this.craftEntity = new VersionedBotCraftPlayer(this);

        this.logic = new TrainingBotLogic(
                this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);

        getBotAI().getTeleportController().setTarget(targetPlayer);
    }

    @Override
    public void prepareFullAttackStrength() {
        int delay = Math.max(1, (int) Math.ceil(this.getCurrentItemAttackStrengthDelay()));
        if (this.attackStrengthTicker < delay) {
            this.attackStrengthTicker = delay;
        }
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (ClassCastException ignored) {
            plugin.getLogger().finest("Skipped the Bukkit compatibility tick during bot teardown");
        }
        craftEntity.setHandle(this);
        logic.onTick();
    }

    @Override
    public void die(DamageSource cause) {
        if (this.dead || this.isRemoved()) {
            return;
        }
        this.getInventory().clearContent();
        this.dead = true;
        this.setHealth(0.0F);
        logic.onDeath(bukkitKiller(cause));
    }

    @Override
    protected boolean actuallyHurt(DamageSource source, float amount, EntityDamageEvent event) {
        boolean handled = logic.onDamaged(amount, event);
        if (!handled) {
            return false;
        }
        return super.actuallyHurt(source, amount, event);
    }

    @Override
    public void aiStep() {
        try {
            super.aiStep();
        } catch (ClassCastException ignored) {
            plugin.getLogger().finest("Skipped the Bukkit compatibility AI step during bot teardown");
        }
    }

    @Override
    public void completeUsingItem() {
        try {
            super.completeUsingItem();
        } catch (ClassCastException e) {
            if (this.getUseItem().getItem().equals(net.minecraft.world.item.Items.GOLDEN_APPLE)) {
                getBotAI().getHealController().applyEffect();
            }
            this.releaseUsingItem();
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.equals(player)) super.playerTouch(player);
    }

    @Override
    public CraftHumanEntity getBukkitEntity() {
        craftEntity.setHandle(this);
        return craftEntity;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
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
    public org.bukkit.entity.@org.jspecify.annotations.Nullable Player getTargetPlayer() {
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
    public void setFollow(boolean f) {
        logic.getBrainController().setFollow(f);
    }

    @Override
    public boolean isFollow() {
        return logic.getBrainController().isFollow();
    }

    @Override
    public void setCombat(boolean c) {
        logic.getBrainController().setCombat(c);
    }

    @Override
    public boolean isCombat() {
        return logic.getBrainController().isCombat();
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
        craftEntity.setHandle(this);
        return craftEntity;
    }

    @Override
    public void onDamaged(EntityDamageEvent event) {
        if (logic.getBrainController().getBotAI().usesCustomBrain()) {
            logic.getBrainController().getBotAI().customBrainDamaged(event);
        }
    }

    @Override
    public void onDeath(org.bukkit.entity.@org.jspecify.annotations.Nullable LivingEntity killer) {
        logic.onDeath(killer);
    }

    private org.bukkit.entity.@org.jspecify.annotations.Nullable LivingEntity bukkitKiller(DamageSource cause) {
        net.minecraft.world.entity.Entity killer = cause.getEntity();
        if (killer == null) {
            return null;
        }
        org.bukkit.entity.Entity bukkitEntity = killer.getBukkitEntity();
        return bukkitEntity instanceof org.bukkit.entity.LivingEntity livingEntity ? livingEntity : null;
    }
}
