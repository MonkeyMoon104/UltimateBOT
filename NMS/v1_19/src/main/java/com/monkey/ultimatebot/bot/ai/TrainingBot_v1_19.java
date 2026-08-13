package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_19.VersionedBotCraftPlayer;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public final class TrainingBot_v1_19 extends Player implements ITrainingBot {

    private final TrainingBotLogic logic;
    private final VersionedBotCraftPlayer craftEntity;
    private final UltimateBot plugin;

    public TrainingBot_v1_19(
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

        super(level, pos, yRot, gameProfile, null);

        this.setNoGravity(false);
        this.setOnGround(false);
        this.getFoodData().setFoodLevel(20);
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
        craftEntity.setHandle(this);
        // AI first so setDeltaMovement is consumed by travel() inside super.tick().
        logic.onTick();
        try {
            super.tick();
        } catch (ClassCastException ignored) {
            plugin.getLogger().finest("Skipped the Bukkit compatibility tick during bot teardown");
        }
        // Fake Player (not ServerPlayer): tracker may not push move packets reliably on 1.19.3.
        broadcastMotionToViewers();
    }

    /**
     * Vanilla slowly lerps head/body between AI updates; for fake players that breaks follow look on
     * 1.19.3 (head drifts away from the target while the body stays still).
     */
    @Override
    protected float tickHeadTurn(float bodyRotation, float headRotation) {
        this.yBodyRot = this.getYRot();
        return this.getYHeadRot();
    }

    private void broadcastMotionToViewers() {
        if (!(this.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ClientboundTeleportEntityPacket teleport = new ClientboundTeleportEntityPacket(this);
        byte head = (byte) Mth.floor(this.getYHeadRot() * 256.0F / 360.0F);
        ClientboundRotateHeadPacket headPacket = new ClientboundRotateHeadPacket(this, head);
        double maxDistSq = 64.0D * 64.0D;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.getId() == this.getId()) {
                continue;
            }
            double dx = viewer.getX() - this.getX();
            double dz = viewer.getZ() - this.getZ();
            if (dx * dx + dz * dz > maxDistSq) {
                continue;
            }
            viewer.connection.send(teleport);
            viewer.connection.send(headPacket);
        }
    }

    @Override
    public void die(DamageSource cause) {
        this.getInventory().clearContent();
        super.die(cause);
        logic.onDeath(bukkitKiller(cause));
    }

    @Override
    protected boolean damageEntity0(DamageSource source, float amount) {
        EntityDamageEvent event = this.getBukkitEntity().getLastDamageCause();
        boolean handled = logic.onDamaged(amount, event);
        if (!handled) {
            return false;
        }
        return super.damageEntity0(source, amount);
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
