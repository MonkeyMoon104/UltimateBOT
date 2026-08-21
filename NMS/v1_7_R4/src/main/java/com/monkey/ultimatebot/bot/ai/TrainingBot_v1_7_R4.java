package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_7_R4.EmptyNetworkManager;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Material;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

public final class TrainingBot_v1_7_R4 extends EntityPlayer {

    private final TrainingBotHandle_v1_7_R4 handle;
    private final UltimateBot plugin;
    private final int tickTaskId;
    private int lastBotTick = Integer.MIN_VALUE;

    public TrainingBot_v1_7_R4(
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
        this.playerConnection = new PlayerConnection(server, new EmptyNetworkManager(), this) {
            @Override
            public void sendPacket(Packet packet) {}
        };
        this.joining = false;
        this.onGround = true;
        this.W = 1.0F;
        this.abilities.isFlying = false;
        this.abilities.canFly = false;
        this.abilities.mayBuild = true;
        setPositionRotation(x, y, z, yaw, pitch);
        this.handle = new TrainingBotHandle_v1_7_R4(
                this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
        this.tickTaskId = plugin.getServer()
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

    public TrainingBotHandle_v1_7_R4 handle() {
        return handle;
    }

    @Override
    public void h() {
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
            super.h();
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
        int eyeX = floor(this.locX);
        int eyeY = floor(this.locY + this.getHeadHeight());
        int eyeZ = floor(this.locZ);
        if (isWaterBlock(eyeX, eyeY, eyeZ)) {
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

    private boolean isWaterBlock(int x, int y, int z) {
        Block block = this.world.getType(x, y, z);
        return block.getMaterial() == Material.WATER;
    }

    private boolean isStandingOnSolid() {
        int x = floor(this.locX);
        int y = floor(this.locY - 0.05D);
        int z = floor(this.locZ);
        Block block = this.world.getType(x, y, z);
        return block.getMaterial().isSolid();
    }

    private boolean isInCobweb() {
        int x = floor(this.locX);
        int y = floor(this.locY);
        int z = floor(this.locZ);
        return this.world.getType(x, y, z) == Blocks.WEB || this.world.getType(x, y + 1, z) == Blocks.WEB;
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0D : value;
    }

    @Override
    public void die(DamageSource cause) {
        if (this.dead) {
            return;
        }
        cancelTickTask();
        this.inventory.items = new net.minecraft.server.v1_7_R4.ItemStack[36];
        this.inventory.armor = new net.minecraft.server.v1_7_R4.ItemStack[4];
        this.dead = true;
        this.setHealth(0.0F);
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
