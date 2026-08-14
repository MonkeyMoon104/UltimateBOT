package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R3.EmptyNetworkManager;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R3.VersionedBotCraftPlayer;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/**
 * Minimal 1.16.5 fake {@link EntityPlayer}. AI state lives on {@link TrainingBotHandle_v1_16_R3}.
 */
public final class TrainingBot_v1_16_R3 extends EntityPlayer {

    private final TrainingBotHandle_v1_16_R3 handle;
    private final UltimateBot plugin;
    private final VersionedBotCraftPlayer bukkitPlayer;

    public TrainingBot_v1_16_R3(
            MinecraftServer server,
            WorldServer world,
            GameProfile profile,
            PlayerInteractManager interactManager,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {
        super(server, world, profile, interactManager);
        this.plugin = plugin;
        this.playerConnection = new PlayerConnection(server, new EmptyNetworkManager(), this);
        setLocation(x, y, z, yaw, pitch);
        setOnGround(true);
        this.abilities.isFlying = false;
        this.abilities.canFly = false;
        this.abilities.mayBuild = true;
        this.bukkitPlayer = new VersionedBotCraftPlayer(this);
        this.handle =
                new TrainingBotHandle_v1_16_R3(
                        this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
    }

    public TrainingBotHandle_v1_16_R3 handle() {
        return handle;
    }

    /**
     * Forces a fully charged attack (1.9+ cooldown). Without this, EntityHuman.attack scales damage
     * by {@code getAttackCooldown} (~0.2 when {@code at} is 0) → ~1 heart even with a diamond sword.
     */
    void forceFullAttackStrength() {
        int delay = Math.max(1, (int) Math.ceil(this.eR()));
        if (this.at < delay) {
            this.at = delay;
        }
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (ClassCastException ignored) {
            plugin.getLogger().finest("Skipped Bukkit compatibility tick during bot teardown");
        }
        // Never allow creative-fly physics on fake players (EntityHuman.travel skips gravity when
        // abilities.isFlying). Cobweb + raw move() without gravity caused permanent ascent in UHC.
        this.abilities.isFlying = false;
        this.abilities.canFly = false;
        this.abilities.mayBuild = true;

        // AI sets velocity after the vanilla player tick (which ignores fake-connection motion).
        handle.onNativeTick();
        applyBotMotion();
        // Packet-spawned fake players are not reliably tracker-synced — push pose to viewers.
        NMSBridgeManager.get().broadcastBotPosition(getBukkitEntity());
    }

    /**
     * Applies AI velocity with gravity, cobweb drag, and friction.
     *
     * <p>Important: {@link #move} uses a movement delta but does <strong>not</strong> write gravity
     * back into {@code mot}. Saving {@code getMot()} afterward re-applied the pre-gravity jump
     * forever (SwordPvP "every jump starts flying"). Also {@code EntityPlayer.onGround} is
     * unreliable with an empty connection — derive support from the block below.
     *
     * <p>Water: only skip land gravity when swimming or eyes are submerged (Water PvP). Feet-only
     * water must keep gravity or the bot hovers at fixed Y in shallow ponds.
     */
    private void applyBotMotion() {
        Vec3D motion = getMot();
        net.minecraft.server.v1_16_R3.BlockPosition eyes =
                new net.minecraft.server.v1_16_R3.BlockPosition(
                        this.locX(), this.locY() + this.getHeadHeight(), this.locZ());
        boolean eyesInWater = isWaterBlock(eyes);
        if (eyesInWater || this.isSwimming()) {
            applySwimMotion(motion);
            return;
        }
        boolean grounded = isStandingOnSolid() && motion.y <= 0.04D;
        this.onGround = grounded;

        if (isInCobweb()) {
            motion = new Vec3D(motion.x * 0.25D, motion.y * 0.05D, motion.z * 0.25D);
        }
        if (!grounded) {
            motion = motion.add(0.0D, -0.08D, 0.0D);
        }
        if (motion.x * motion.x + motion.y * motion.y + motion.z * motion.z > 1.0E-12D) {
            this.move(EnumMoveType.SELF, motion);
        }

        grounded = isStandingOnSolid();
        this.onGround = grounded && motion.y <= 0.0D;

        double drag = grounded ? 0.6D : 0.91D;
        double vy;
        if (grounded) {
            vy = 0.0D;
        } else {
            // Persist the gravity-adjusted Y — never raw getMot() (pre-gravity jump).
            vy = motion.y * 0.98D;
        }
        if (vy > 0.6D) {
            vy = 0.6D;
        }
        if (vy < -3.5D) {
            vy = -3.5D;
        }
        this.setMot(motion.x * drag, vy, motion.z * drag);
    }

    private void applySwimMotion(Vec3D motion) {
        // Idle mid-column: without a sink force, zero AI velocity freezes Y forever.
        if (motion.x * motion.x + motion.z * motion.z < 1.0E-6D && Math.abs(motion.y) < 0.02D) {
            motion = motion.add(0.0D, -0.03D, 0.0D);
        }
        if (motion.x * motion.x + motion.y * motion.y + motion.z * motion.z > 1.0E-12D) {
            this.move(EnumMoveType.SELF, motion);
        }
        this.onGround = false;
        this.setMot(motion.x * 0.8D, motion.y * 0.8D, motion.z * 0.8D);
    }

    private boolean isWaterBlock(net.minecraft.server.v1_16_R3.BlockPosition pos) {
        net.minecraft.server.v1_16_R3.IBlockData data = this.world.getType(pos);
        return data.getMaterial() == net.minecraft.server.v1_16_R3.Material.WATER
                || data.getBlock() == net.minecraft.server.v1_16_R3.Blocks.BUBBLE_COLUMN;
    }

    private boolean isStandingOnSolid() {
        net.minecraft.server.v1_16_R3.BlockPosition below =
                new net.minecraft.server.v1_16_R3.BlockPosition(
                        this.locX(), this.locY() - 0.05D, this.locZ());
        net.minecraft.server.v1_16_R3.IBlockData data = this.world.getType(below);
        return data.getMaterial().isSolid();
    }

    private boolean isInCobweb() {
        net.minecraft.server.v1_16_R3.BlockPosition feet =
                new net.minecraft.server.v1_16_R3.BlockPosition(this.locX(), this.locY(), this.locZ());
        net.minecraft.server.v1_16_R3.BlockPosition head = feet.up();
        return this.world.getType(feet).getBlock() == net.minecraft.server.v1_16_R3.Blocks.COBWEB
                || this.world.getType(head).getBlock() == net.minecraft.server.v1_16_R3.Blocks.COBWEB;
    }

    @Override
    public void die(DamageSource cause) {
        this.inventory.clear();
        super.die(cause);
        handle.onNativeDeath(bukkitKiller(cause));
    }

    @Override
    public boolean damageEntity(DamageSource source, float amount) {
        EntityDamageEvent event = this.getBukkitEntity().getLastDamageCause();
        boolean handled = handle.onNativeDamaged(amount, event);
        if (!handled) {
            return false;
        }
        return super.damageEntity(source, amount);
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return bukkitPlayer;
    }

    private org.bukkit.entity.@Nullable LivingEntity bukkitKiller(DamageSource cause) {
        Entity killer = cause.getEntity();
        if (killer == null) {
            return null;
        }
        org.bukkit.entity.Entity bukkitEntity = killer.getBukkitEntity();
        if (bukkitEntity instanceof org.bukkit.entity.LivingEntity) {
            return (org.bukkit.entity.LivingEntity) bukkitEntity;
        }
        return null;
    }
}
