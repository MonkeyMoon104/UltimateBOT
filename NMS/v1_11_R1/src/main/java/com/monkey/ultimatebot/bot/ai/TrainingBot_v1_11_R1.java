package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_11_R1.EmptyNetworkManager;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Blocks;
import net.minecraft.server.v1_11_R1.DamageSource;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EnumMoveType;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.Material;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;
import net.minecraft.server.v1_11_R1.WorldServer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/**
 * Minimal 1.11.2 fake {@link EntityPlayer}. AI state lives on {@link TrainingBotHandle_v1_11_R1}.
 *
 * <p>Packet-spawned fake players need explicit motion application and position broadcast — vanilla
 * tick ignores empty-connection velocity on this revision.
 */
public final class TrainingBot_v1_11_R1 extends EntityPlayer {

    private final TrainingBotHandle_v1_11_R1 handle;
    private final UltimateBot plugin;

    public TrainingBot_v1_11_R1(
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
        this.handle =
                new TrainingBotHandle_v1_11_R1(
                        this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
    }

    public TrainingBotHandle_v1_11_R1 handle() {
        return handle;
    }

    /**
     * Forces a fully charged attack (1.9+ cooldown). Without this, EntityHuman.attack scales damage
     * by getAttackCooldown (~0.2 when the ticker is 0) — about one heart even with a diamond sword.
     */
    void forceFullAttackStrength() {
        int delay = Math.max(1, (int) Math.ceil(this.dg()));
        if (this.aE < delay) {
            this.aE = delay;
        }
    }

    @Override
    public void A_() {
        try {
            super.A_();
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
            this.move(EnumMoveType.SELF, mx, my, mz);
        }

        grounded = isStandingOnSolid();
        this.onGround = grounded && my <= 0.0D;

        double drag = grounded ? 0.6D : 0.91D;
        double vy;
        if (grounded) {
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
            this.move(EnumMoveType.SELF, mx, my, mz);
        }
        this.onGround = false;
        this.motX = finite(mx * 0.8D);
        this.motY = finite(my * 0.8D);
        this.motZ = finite(mz * 0.8D);
        this.velocityChanged = true;
    }

    private boolean isWaterBlock(BlockPosition pos) {
        IBlockData data = this.world.getType(pos);
        return data.getMaterial() == Material.WATER;
    }

    private boolean isStandingOnSolid() {
        BlockPosition below = new BlockPosition(this.locX, this.locY - 0.05D, this.locZ);
        IBlockData data = this.world.getType(below);
        return data.getMaterial().isSolid();
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
