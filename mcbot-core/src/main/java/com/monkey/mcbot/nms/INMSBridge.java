package com.monkey.mcbot.nms;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.gui.NewBotGUI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public interface INMSBridge {

    void hurtEntity(LivingEntity target, ServerLevel level, DamageSource source, float amount);

    boolean actuallyHurt(Player bot, ServerLevel level, DamageSource source, float amount, EntityDamageEvent event);

    void explode(Level level, Player cause, double x, double y, double z, float power);

    void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch);

    void playSoundOnPlayer(Player player, SoundEvent sound, float volume, float pitch);

    ClientInformation createClientInformation();

    ServerLevel getServerLevel(ServerPlayer player);

    ITrainingBot createTrainingBot(
            ServerLevel level,
            BlockPos pos,
            float yRot,
            GameProfile gameProfile,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            MinecraftBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions
    );

    void moveBot(Player bot, double x, double y, double z);

    GameProfile copyProfileWithTextures(org.bukkit.entity.Player viewer, UUID botUUID, String botName);

    void addToProfileCache(net.minecraft.world.entity.player.Player bot);

    void removeFromProfileCache(UUID botUUID);

    String getProfileName(com.mojang.authlib.GameProfile profile);

    InteractionResult useItemOnBlock(Player bot, ItemStack stack, BlockHitResult hitResult, InteractionHand hand);

    void throwEnderpearl(Player bot, net.minecraft.world.phys.Vec3 targetPos);

    default void openBotGui(org.bukkit.entity.Player player, MinecraftBot plugin, BotType botType) {
        new NewBotGUI(player, plugin, botType).open();
    }
}
