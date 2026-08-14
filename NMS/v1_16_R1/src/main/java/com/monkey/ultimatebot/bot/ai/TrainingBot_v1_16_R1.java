package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R1.EmptyNetworkManager;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R1.VersionedBotCraftPlayer;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.server.v1_16_R1.DamageSource;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EnumMoveType;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import net.minecraft.server.v1_16_R1.PlayerConnection;
import net.minecraft.server.v1_16_R1.PlayerInteractManager;
import net.minecraft.server.v1_16_R1.Vec3D;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/**
 * Minimal 1.16.1 fake {@link EntityPlayer}. AI state lives on {@link TrainingBotHandle_v1_16_R1}.
 *
 * <p>Same motion path as 1.16.3: Paper {@code EntityTrackerEntry} syncs Bukkit velocity from NMS
 * mot, so keep fly off and clamp to finite values.
 */
public final class TrainingBot_v1_16_R1 extends EntityPlayer {

    private final TrainingBotHandle_v1_16_R1 handle;
    private final UltimateBot plugin;
    private final VersionedBotCraftPlayer bukkitPlayer;

    public TrainingBot_v1_16_R1(
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
        this.joining = false;
        setLocation(x, y, z, yaw, pitch);
        this.onGround = true;
        this.abilities.isFlying = false;
        this.abilities.canFly = false;
        this.abilities.mayBuild = true;
        this.bukkitPlayer = new VersionedBotCraftPlayer(this);
        this.handle =
                new TrainingBotHandle_v1_16_R1(
                        this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
    }

    public TrainingBotHandle_v1_16_R1 handle() {
        return handle;
    }

    /** Public helper: {@code setOnGround} is absent on this revision. */
    public void setOnGroundFlag(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (ClassCastException ignored) {
            plugin.getLogger().finest("Skipped Bukkit compatibility tick during bot teardown");
        }
        this.abilities.isFlying = false;
        this.abilities.canFly = false;
        this.abilities.mayBuild = true;

        handle.onNativeTick();
        applyBotMotion();
        NMSBridgeManager.get().broadcastBotPosition(getBukkitEntity());
    }

    /**
     * Applies AI velocity with gravity, cobweb drag, and friction.
     *
     * <p>Water: only skip land gravity when swimming or eyes are submerged (Water PvP). Feet-only
     * water must keep gravity or the bot hovers at fixed Y in shallow ponds.
     */
    private void applyBotMotion() {
        Vec3D motion = finiteMot(getMot());
        net.minecraft.server.v1_16_R1.BlockPosition eyes =
                new net.minecraft.server.v1_16_R1.BlockPosition(
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
            vy = motion.y * 0.98D;
        }
        if (vy > 0.6D) {
            vy = 0.6D;
        }
        if (vy < -3.5D) {
            vy = -3.5D;
        }
        this.setMot(finite(motion.x * drag), finite(vy), finite(motion.z * drag));
    }

    private void applySwimMotion(Vec3D motion) {
        if (motion.x * motion.x + motion.z * motion.z < 1.0E-6D && Math.abs(motion.y) < 0.02D) {
            motion = motion.add(0.0D, -0.03D, 0.0D);
        }
        if (motion.x * motion.x + motion.y * motion.y + motion.z * motion.z > 1.0E-12D) {
            this.move(EnumMoveType.SELF, motion);
        }
        this.onGround = false;
        this.setMot(finite(motion.x * 0.8D), finite(motion.y * 0.8D), finite(motion.z * 0.8D));
    }

    private boolean isWaterBlock(net.minecraft.server.v1_16_R1.BlockPosition pos) {
        net.minecraft.server.v1_16_R1.IBlockData data = this.world.getType(pos);
        return data.getMaterial() == net.minecraft.server.v1_16_R1.Material.WATER
                || data.getBlock() == net.minecraft.server.v1_16_R1.Blocks.BUBBLE_COLUMN;
    }

    private boolean isStandingOnSolid() {
        net.minecraft.server.v1_16_R1.BlockPosition below =
                new net.minecraft.server.v1_16_R1.BlockPosition(
                        this.locX(), this.locY() - 0.05D, this.locZ());
        net.minecraft.server.v1_16_R1.IBlockData data = this.world.getType(below);
        return data.getMaterial().isSolid();
    }

    private boolean isInCobweb() {
        net.minecraft.server.v1_16_R1.BlockPosition feet =
                new net.minecraft.server.v1_16_R1.BlockPosition(this.locX(), this.locY(), this.locZ());
        net.minecraft.server.v1_16_R1.BlockPosition head = feet.up();
        return this.world.getType(feet).getBlock() == net.minecraft.server.v1_16_R1.Blocks.COBWEB
                || this.world.getType(head).getBlock() == net.minecraft.server.v1_16_R1.Blocks.COBWEB;
    }

    private static Vec3D finiteMot(Vec3D motion) {
        return new Vec3D(finite(motion.x), finite(motion.y), finite(motion.z));
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0.0D;
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
