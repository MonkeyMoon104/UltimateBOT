package com.monkey.mcbot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.TrainingBot_v1_21_4;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class NMSBridge_v1_21_4 implements INMSBridge {

    @Override
    public void hurtEntity(Player target, ServerLevel level, DamageSource source, float amount) {
        target.hurtServer(level, source, amount);
    }

    @Override
    public boolean actuallyHurt(Player bot, ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        if (bot instanceof com.monkey.mcbot.bot.ai.ITrainingBot trainingBot) {
            return trainingBot.callSuperActuallyHurt(level, source, amount, event);
        }
        return false;
    }

    @Override
    public void explode(Level level, Player cause, double x, double y, double z, float power) {
        level.explode(cause, x, y, z, power, Level.ExplosionInteraction.NONE);
    }

    @Override
    public void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, pos, sound, source, volume, pitch);
    }

    @Override
    public void playSoundOnPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.playSound(sound, volume, pitch);
    }

    @Override
    public ClientInformation createClientInformation() {
        return new ClientInformation(
                "it_IT", 10, ChatVisiblity.FULL, true,
                0, HumanoidArm.RIGHT, false, true, ParticleStatus.ALL
        );
    }

    @Override
    public ClientboundPlayerInfoUpdatePacket createAddPlayerPacket(
            UUID uuid, GameProfile profile, String displayName) {

        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(
                uuid, profile, true, 0, GameType.SURVIVAL,
                Component.literal(displayName), true, 0, null
        );
        return new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                List.of(entry)
        );
    }

    @Override
    public ClientboundAddEntityPacket createSpawnPlayerPacket(
            int entityId, UUID uuid,
            double x, double y, double z,
            float xRot, float yRot, double yHeadRot) {

        return new ClientboundAddEntityPacket(
                entityId, uuid, x, y, z,
                xRot, yRot,
                EntityType.PLAYER, 0,
                new Vec3(0, 0, 0),
                yHeadRot
        );
    }

    @Override
    public ClientboundSetEquipmentPacket createEquipmentPacket(
            int entityId, List<Pair<EquipmentSlot, ItemStack>> equipment) {
        return new ClientboundSetEquipmentPacket(entityId, equipment);
    }

    @Override
    public ServerLevel getServerLevel(ServerPlayer player) {
        return player.serverLevel();
    }

    @Override
    public ITrainingBot createTrainingBot(ServerLevel level,
                                          BlockPos pos,
                                          float yRot,
                                          GameProfile gameProfile,
                                          org.bukkit.entity.Player targetPlayer,
                                          boolean follow,
                                          SandboxTraining plugin,
                                          String deadBotMessage,
                                          String deadBotEventMessage,
                                          BotOptions botOptions) {
        return new TrainingBot_v1_21_4(
                level, pos, yRot, gameProfile,
                targetPlayer, follow, plugin,
                deadBotMessage, deadBotEventMessage, botOptions
        );
    }

    @Override
    public void moveBot(Player bot, double x, double y, double z) {
        bot.moveTo(x, y, z);
    }

    @Override
    public GameProfile copyProfileWithTextures(org.bukkit.entity.Player viewer, UUID botUUID, String botName) {
        GameProfile viewerProfile = ((CraftPlayer) viewer).getProfile();
        Collection<Property> textures = viewerProfile.getProperties().get("textures");
        GameProfile profile = new GameProfile(botUUID, botName);
        if (!textures.isEmpty()) {
            profile.getProperties().put("textures", textures.iterator().next());
        }
        return profile;
    }

    @Override
    public void addToProfileCache(net.minecraft.world.entity.player.Player bot) {
        ((CraftServer) Bukkit.getServer()).getHandle().getServer()
                .getProfileCache().add(bot.getGameProfile());
    }

    @Override
    public String getProfileName(com.mojang.authlib.GameProfile profile) {
        return profile.getName();
    }
}