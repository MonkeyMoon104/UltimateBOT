package com.monkey.mcbot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.TrainingBot_v1_21_8;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;

public class NMSBridge_v1_21_8 implements INMSBridge {

    @Override
    public void hurtEntity(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        target.hurtServer(level, source, amount);
    }

    @Override
    public boolean actuallyHurt(
            Player bot, ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        if (bot instanceof com.monkey.mcbot.bot.ai.ITrainingBot trainingBot) {
            return trainingBot.callSuperActuallyHurt(level, source, amount, event);
        }
        return false;
    }

    @Override
    public void explode(Level level, Player cause, double x, double y, double z, float power, boolean blockDamage) {
        level.explode(
                cause,
                x,
                y,
                z,
                power,
                blockDamage ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    @Override
    public void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSeededSound(
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                sound,
                source,
                volume,
                pitch,
                level.random.nextLong());
    }

    @Override
    public void playSoundOnPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.level()
                .playSeededSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        sound,
                        SoundSource.PLAYERS,
                        volume,
                        pitch,
                        player.level().random.nextLong());
    }

    @Override
    public ClientInformation createClientInformation() {
        return new ClientInformation(
                "it_IT", 10, ChatVisiblity.FULL, true, 0, HumanoidArm.RIGHT, false, true, ParticleStatus.ALL);
    }

    @Override
    public ServerLevel getServerLevel(ServerPlayer player) {
        return player.level();
    }

    @Override
    public ITrainingBot createTrainingBot(
            ServerLevel level,
            BlockPos pos,
            float yRot,
            GameProfile gameProfile,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            MinecraftBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {
        return new TrainingBot_v1_21_8(
                level,
                pos,
                yRot,
                gameProfile,
                targetPlayer,
                follow,
                plugin,
                deadBotMessage,
                deadBotEventMessage,
                botOptions);
    }

    @Override
    public void moveBot(Player bot, double x, double y, double z) {
        bot.snapTo(x, y, z);
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
        ((CraftServer) Bukkit.getServer())
                .getHandle()
                .getServer()
                .getProfileCache()
                .add(bot.getGameProfile());
    }

    @Override
    public void removeFromProfileCache(UUID botUUID) {
        Object cache =
                ((CraftServer) Bukkit.getServer()).getHandle().getServer().getProfileCache();
        removeProfileCacheEntry(cache, botUUID, new GameProfile(botUUID, ""));
    }

    @Override
    public String getProfileName(com.mojang.authlib.GameProfile profile) {
        return profile.getName();
    }

    private void removeProfileCacheEntry(Object cache, UUID botUUID, GameProfile profile) {
        for (Method method : cache.getClass().getMethods()) {
            if (!method.getName().equals("remove") || method.getParameterCount() != 1) {
                continue;
            }
            Object argument = resolveRemovalArgument(method.getParameterTypes()[0], botUUID, profile);
            if (argument != null) {
                try {
                    method.invoke(cache, argument);
                } catch (ReflectiveOperationException ignored) {
                    // Try the next compatible cache removal overload.
                }
                return;
            }
        }
    }

    private Object resolveRemovalArgument(Class<?> parameterType, UUID botUUID, GameProfile profile) {
        if (parameterType.isAssignableFrom(UUID.class)) {
            return botUUID;
        }
        if (parameterType.isAssignableFrom(GameProfile.class)) {
            return profile;
        }
        return null;
    }

    @Override
    public InteractionResult useItemOnBlock(
            Player bot, ItemStack stack, BlockHitResult hitResult, InteractionHand hand) {
        return stack.getItem().useOn(new UseOnContext(bot.level(), bot, hand, stack, hitResult));
    }

    @Override
    public void throwEnderpearl(Player bot, Vec3 targetPos) {
        ThrownEnderpearl enderpearl =
                new ThrownEnderpearl(net.minecraft.world.entity.EntityType.ENDER_PEARL, bot.level());
        enderpearl.setOwner(bot);

        Vec3 botPos = bot.position().add(0, bot.getEyeHeight(), 0);
        Vec3 direction = targetPos.subtract(botPos);
        double distance = direction.length();

        double velocityScale = distance < 15 ? Math.min(distance * 0.08, 1.2) : Math.min(distance * 0.06, 1.8);

        Vec3 velocity = direction.normalize().scale(velocityScale);
        velocity = velocity.add(0, 0.2 + distance * 0.02, 0);

        enderpearl.setPos(botPos.x, botPos.y, botPos.z);
        enderpearl.setDeltaMovement(velocity);
        bot.level().addFreshEntity(enderpearl);

        bot.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        NMSBridgeManager.get()
                .playSoundOnPlayer(
                        bot,
                        net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW,
                        0.5f,
                        0.4f / (bot.level().getRandom().nextFloat() * 0.4f + 0.8f));
    }
}
