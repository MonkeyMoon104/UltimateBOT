package com.monkey.ultimatebot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.TrainingBot_v26_2;
import com.monkey.ultimatebot.gui.v26_2.NewBotGUI_v26_2;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;

public class NMSBridge_v26_2 implements INMSBridge {

    @Override
    public void hurtEntity(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        target.hurtServer(level, source, amount);
    }

    @Override
    public boolean actuallyHurt(
            Player bot, ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        if (bot instanceof com.monkey.ultimatebot.bot.ai.ITrainingBot trainingBot) {
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
                level.getRandom().nextLong());
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
                        player.level().getRandom().nextLong());
    }

    @Override
    public ClientInformation createClientInformation() {
        return new ClientInformation(
                "it_IT", 10, ChatVisiblity.FULL, true, 0, HumanoidArm.RIGHT, false, true, ParticleStatus.ALL);
    }

    @Override
    public ClientboundPlayerInfoUpdatePacket createAddPlayerPacket(UUID uuid, GameProfile profile, String displayName) {
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(
                uuid, profile, true, 0, GameType.SURVIVAL, Component.literal(displayName), true, 0, null);
        return new ClientboundPlayerInfoUpdatePacket(
                java.util.EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), List.of(entry));
    }

    @Override
    public ClientboundAddEntityPacket createSpawnPlayerPacket(
            int entityId, UUID uuid, double x, double y, double z, float xRot, float yRot, double yHeadRot) {
        return new ClientboundAddEntityPacket(
                entityId, uuid, x, y, z, xRot, yRot, EntityTypes.PLAYER, 0, new Vec3(0, 0, 0), yHeadRot);
    }

    @Override
    public ClientboundSetEquipmentPacket createEquipmentPacket(
            int entityId, List<Pair<EquipmentSlot, ItemStack>> equipment) {
        return new ClientboundSetEquipmentPacket(entityId, equipment);
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
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {
        return new TrainingBot_v26_2(
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
        Collection<Property> textures = viewerProfile.properties().get("textures");

        if (textures.isEmpty()) {
            return new GameProfile(botUUID, botName);
        }

        com.google.common.collect.ArrayListMultimap<String, Property> multimap =
                com.google.common.collect.ArrayListMultimap.create();
        textures.forEach(p -> multimap.put("textures", p));

        return new GameProfile(botUUID, botName, new PropertyMap(multimap));
    }

    @Override
    public GameProfile createProfileWithTexture(
            UUID botUUID,
            String botName,
            String textureValue,
            @org.jspecify.annotations.Nullable String textureSignature) {
        Property property = textureSignature == null || textureSignature.isBlank()
                ? new Property("textures", textureValue)
                : new Property("textures", textureValue, textureSignature);
        com.google.common.collect.ArrayListMultimap<String, Property> multimap =
                com.google.common.collect.ArrayListMultimap.create();
        multimap.put("textures", property);
        return new GameProfile(botUUID, botName, new PropertyMap(multimap));
    }

    @Override
    public BotProfileData getProfileData(GameProfile profile) {
        List<BotProfileData.Texture> textures = profile.properties().get("textures").stream()
                .map(property -> new BotProfileData.Texture(property.name(), property.value(), property.signature()))
                .toList();
        return new BotProfileData(profile.id(), profile.name(), textures);
    }

    @Override
    public void addToProfileCache(net.minecraft.world.entity.player.Player bot) {
        try {
            var server = ((CraftServer) Bukkit.getServer()).getServer();
            var services = server.services();
            var profile = bot.getGameProfile();

            services.nameToIdCache().add(new net.minecraft.server.players.NameAndId(profile));

            var paperServices = services.paper();
            if (paperServices != null && paperServices.filledProfileCache() != null) {
                paperServices.filledProfileCache().add(profile);
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void removeFromProfileCache(UUID botUUID) {
        try {
            var server = ((CraftServer) Bukkit.getServer()).getServer();
            var services = server.services();
            var profile = new GameProfile(botUUID, "");
            var nameAndId = new net.minecraft.server.players.NameAndId(profile);

            removeProfileCacheEntry(services.nameToIdCache(), botUUID, profile, nameAndId);

            var paperServices = services.paper();
            if (paperServices != null) {
                removeProfileCacheEntry(paperServices.filledProfileCache(), botUUID, profile, null);
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public String getProfileName(com.mojang.authlib.GameProfile profile) {
        return profile.name();
    }

    private void removeProfileCacheEntry(
            @org.jspecify.annotations.Nullable Object cache,
            UUID botUUID,
            GameProfile profile,
            @org.jspecify.annotations.Nullable Object nameAndId) {
        if (cache == null) {
            return;
        }
        for (Method method : cache.getClass().getMethods()) {
            if (!method.getName().equals("remove") || method.getParameterCount() != 1) {
                continue;
            }
            Object argument = resolveRemovalArgument(method.getParameterTypes()[0], botUUID, profile, nameAndId);
            if (argument != null) {
                try {
                    method.invoke(cache, argument);
                } catch (ReflectiveOperationException ignored) {
                    continue;
                }
                return;
            }
        }
    }

    private @org.jspecify.annotations.Nullable Object resolveRemovalArgument(
            Class<?> parameterType,
            UUID botUUID,
            GameProfile profile,
            @org.jspecify.annotations.Nullable Object nameAndId) {
        if (parameterType.isAssignableFrom(UUID.class)) {
            return botUUID;
        }
        if (parameterType.isAssignableFrom(GameProfile.class)) {
            return profile;
        }
        if (nameAndId != null && parameterType.isInstance(nameAndId)) {
            return nameAndId;
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
        ThrownEnderpearl enderpearl = new ThrownEnderpearl(EntityTypes.ENDER_PEARL, bot.level());
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

    @Override
    public void openBotGui(org.bukkit.entity.Player player, UltimateBot plugin, BotType botType) {
        new NewBotGUI_v26_2(player, plugin, botType).open();
    }
}
