package com.monkey.ultimatebot.bot.ai;

import com.mojang.authlib.GameProfile;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_8_R1.EmptyNetworkManager;
import net.minecraft.server.v1_8_R1.DamageSource;
import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.PlayerConnection;
import net.minecraft.server.v1_8_R1.PlayerInteractManager;
import net.minecraft.server.v1_8_R1.WorldServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/** Minimal 1.8 fake {@link EntityPlayer}. AI state lives on {@link TrainingBotHandle_v1_8_R1}. */
public final class TrainingBot_v1_8_R1 extends EntityPlayer {

    private final TrainingBotHandle_v1_8_R1 handle;
    private final UltimateBot plugin;

    public TrainingBot_v1_8_R1(
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
        this.onGround = false;
        this.handle =
                new TrainingBotHandle_v1_8_R1(
                        this, plugin, targetPlayer, follow, botOptions, deadBotMessage, deadBotEventMessage);
    }

    public TrainingBotHandle_v1_8_R1 handle() {
        return handle;
    }

    @Override
    public void s_() {
        try {
            super.s_();
        } catch (ClassCastException ignored) {
            plugin.getLogger().finest("Skipped Bukkit compatibility tick during bot teardown");
        }
        handle.onNativeTick();
    }

    @Override
    public void die(DamageSource cause) {
        this.inventory.items = new net.minecraft.server.v1_8_R1.ItemStack[36];
        this.inventory.armor = new net.minecraft.server.v1_8_R1.ItemStack[4];
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
