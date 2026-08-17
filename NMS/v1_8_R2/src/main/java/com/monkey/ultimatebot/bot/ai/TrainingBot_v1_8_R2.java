package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_8_R2.EmptyNetworkManager;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.DamageSource;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.MinecraftServer;
import net.minecraft.server.v1_8_R2.PlayerConnection;
import net.minecraft.server.v1_8_R2.PlayerInteractManager;
import net.minecraft.server.v1_8_R2.WorldServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/**
 * Minimal 1.8.3 fake {@link EntityPlayer}. AI state lives on {@link TrainingBotHandle_v1_8_R2}.
 *
 * <p>Packet-spawned fake players need explicit motion application and position broadcast — vanilla
 * tick ignores empty-connection velocity on this revision.
 */
public final class TrainingBot_v1_8_R2 extends EntityPlayer {

    private final TrainingBotHandle_v1_8_R2 handle;
    private final UltimateBot plugin;
    private final int tickTaskId;
    private int lastBotTick = Integer.MIN_VALUE;

    public TrainingBot_v1_8_R2(
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
        this.collidesWithEntities = false;
        setLocation(x, y, z, yaw, pitch);
        this.onGround = true;
        // Vanilla player stepHeight is 0.6 — cannot climb a full block without a jump, and our
        // jump is eaten while still intersecting the floor. 1.0 lets Entity.move step up 1 block.
        this.S = 1.0F;
        this.abilities.isFlying = false;
        this.abilities.canFly = false;
        this.abilities.mayBuild = true;
        this.handle =
                new TrainingBotHandle_v1_8_R2(
                        this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
        // World.tickEntities skips EntityPlayer; PlayerConnection.E_() is never invoked for a
        // fake channel. Drive AI + motion from the Bukkit scheduler on this revision only.
        this.tickTaskId =
                plugin.getServer()
                        .getScheduler()
                        .scheduleSyncRepeatingTask(
                                plugin,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runBotTick();
                                    }
                                },
                                1L,
                                1L);
    }

    public TrainingBotHandle_v1_8_R2 handle() {
        return handle;
    }

    @Override
    public void t_() {
        runBotTick();
    }

    @Override
    public void l() {
        runBotTick();
    }

    private void runBotTick() {
        if (this.dead) {
            cancelTickTask();
            return;
        }
        int now = MinecraftServer.currentTick;
        if (now == lastBotTick) {
            return;
        }
        lastBotTick = now;
        try {
            super.t_();
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

    private void cancelTickTask() {
        if (tickTaskId >= 0) {
            plugin.getServer().getScheduler().cancelTask(tickTaskId);
        }
    }

    private void applyBotMotion() {
        double mx = finite(this.motX);
        double my = finite(this.motY);
        double mz = finite(this.motZ);
        BlockPosition eyes = new BlockPosition(this.locX, this.locY + this.getHeadHeight(), this.locZ);
        if (isWaterBlock(eyes)) {
            applySwimMotion(mx, my, mz);
            return;
        }
        boolean grounded = isStandingOnSolid() && my <= 0.04D;
        this.onGround = grounded;

        if (isInCobweb()) {
            mx *= 0.25D;
            my *= 0.05D;
            mz *= 0.25D;
        }
        if (!grounded) {
            my -= 0.08D;
        }
        if (mx * mx + my * my + mz * mz > 1.0E-12D) {
            this.move(mx, my, mz);
        }

        grounded = isStandingOnSolid();
        boolean landed = grounded && my <= 0.04D;
        this.onGround = landed;

        double drag = landed ? 0.6D : 0.91D;
        double vy;
        if (landed) {
            vy = 0.0D;
        } else {
            vy = my * 0.98D;
        }
        if (vy > 0.6D) {
            vy = 0.6D;
        }
        if (vy < -3.5D) {
            vy = -3.5D;
        }
        this.motX = finite(mx * drag);
        this.motY = finite(vy);
        this.motZ = finite(mz * drag);
        this.velocityChanged = true;
    }

    private void applySwimMotion(double mx, double my, double mz) {
        if (mx * mx + mz * mz < 1.0E-6D && Math.abs(my) < 0.02D) {
            my -= 0.03D;
        }
        if (mx * mx + my * my + mz * mz > 1.0E-12D) {
            this.move(mx, my, mz);
        }
        this.onGround = false;
        this.motX = finite(mx * 0.8D);
        this.motY = finite(my * 0.8D);
        this.motZ = finite(mz * 0.8D);
        this.velocityChanged = true;
    }

    private boolean isWaterBlock(BlockPosition pos) {
        IBlockData data = this.world.getType(pos);
        return data.getBlock().getMaterial() == Material.WATER;
    }

    private boolean isStandingOnSolid() {
        BlockPosition below = new BlockPosition(this.locX, this.locY - 0.05D, this.locZ);
        IBlockData data = this.world.getType(below);
        return data.getBlock().getMaterial().isSolid();
    }

    private boolean isInCobweb() {
        BlockPosition feet = new BlockPosition(this.locX, this.locY, this.locZ);
        BlockPosition head = feet.up();
        return this.world.getType(feet).getBlock() == Blocks.WEB
                || this.world.getType(head).getBlock() == Blocks.WEB;
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0.0D;
    }

    @Override
    public void die(DamageSource cause) {
        cancelTickTask();
        this.inventory.items = new net.minecraft.server.v1_8_R2.ItemStack[36];
        this.inventory.armor = new net.minecraft.server.v1_8_R2.ItemStack[4];
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
    @SuppressWarnings("cast")
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) super.getBukkitEntity();
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
