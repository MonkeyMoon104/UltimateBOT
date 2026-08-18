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
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.gui.v26_2.NewBotGUI_v26_2;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class NMSBridge_v26_2 implements INMSBridge {

    private static final Set<PlatformCapability> CAPABILITIES = Set.copyOf(EnumSet.allOf(PlatformCapability.class));

    @Override
    public Set<PlatformCapability> capabilities() {
        return CAPABILITIES;
    }

    @Override
    public ITrainingBot createTrainingBot(
            Location spawn,
            BotProfileData profile,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {
        ServerLevel level = ((CraftWorld) spawn.getWorld()).getHandle();
        BlockPos pos = BlockPos.containing(spawn.getX(), spawn.getY(), spawn.getZ());
        return new TrainingBot_v26_2(
                level,
                pos,
                spawn.getYaw(),
                toGameProfile(profile),
                targetPlayer,
                follow,
                plugin,
                deadBotMessage,
                deadBotEventMessage,
                botOptions);
    }

    @Override
    public void registerBotEntity(ITrainingBot bot) {
        Player nativeBot = nativeBot(bot);
        if (nativeBot.level() instanceof ServerLevel level) {
            level.addFreshEntity(nativeBot);
        }
    }

    @Override
    public void addToProfileCache(ITrainingBot bot) {
        try {
            var server = ((CraftServer) Bukkit.getServer()).getServer();
            var services = server.services();
            var profile = nativeBot(bot).getGameProfile();
            services.nameToIdCache().add(new net.minecraft.server.players.NameAndId(profile));
            var paperServices = services.paper();
            if (paperServices != null && paperServices.filledProfileCache() != null) {
                paperServices.filledProfileCache().add(profile);
            }
        } catch (Exception exception) {
            Bukkit.getLogger()
                    .finest("Profile cache update failed: "
                            + exception.getClass().getName());
        }
    }

    @Override
    public void removeFromProfileCache(UUID botUUID) {
        try {
            var server = ((CraftServer) Bukkit.getServer()).getServer();
            var services = server.services();
            var profile = createGameProfile(botUUID, "", List.of());
            var nameAndId = new net.minecraft.server.players.NameAndId(profile);
            removeProfileCacheEntry(services.nameToIdCache(), botUUID, profile, nameAndId);
            var paperServices = services.paper();
            if (paperServices != null) {
                removeProfileCacheEntry(paperServices.filledProfileCache(), botUUID, profile, null);
            }
        } catch (Exception exception) {
            Bukkit.getLogger()
                    .finest("Profile cache update failed: "
                            + exception.getClass().getName());
        }
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
    public void moveBot(org.bukkit.entity.Player bot, double x, double y, double z) {
        nativePlayer(bot).snapTo(x, y, z);
    }

    @Override
    public BotProfileData copyProfileWithTextures(org.bukkit.entity.Player viewer, UUID botUUID, String botName) {
        GameProfile viewerProfile = ((CraftPlayer) viewer).getProfile();
        Collection<Property> textures = viewerProfile.properties().get("textures");
        if (textures.isEmpty()) {
            return new BotProfileData(botUUID, botName, List.of());
        }
        return toProfileData(createGameProfile(botUUID, botName, textures));
    }

    @Override
    public BotProfileData createProfileWithTexture(
            UUID botUUID,
            String botName,
            String textureValue,
            @org.jspecify.annotations.Nullable String textureSignature) {
        Property property = textureSignature == null || textureSignature.isBlank()
                ? new Property("textures", textureValue)
                : new Property("textures", textureValue, textureSignature);
        return toProfileData(createGameProfile(botUUID, botName, List.of(property)));
    }

    @Override
    public String getProfileName(BotProfileData profile) {
        return profile.name();
    }

    private GameProfile createGameProfile(UUID botUUID, String botName, Collection<Property> textures) {
        com.google.common.collect.ArrayListMultimap<String, Property> multimap =
                com.google.common.collect.ArrayListMultimap.create();
        textures.forEach(property -> multimap.put("textures", property));
        return new GameProfile(botUUID, botName, new PropertyMap(multimap));
    }

    private BotProfileData toProfileData(GameProfile profile) {
        List<BotProfileData.Texture> textures = profile.properties().get("textures").stream()
                .map(property -> new BotProfileData.Texture(property.name(), property.value(), property.signature()))
                .toList();
        return new BotProfileData(profile.id(), profile.name(), textures);
    }

    private GameProfile toGameProfile(BotProfileData profile) {
        List<Property> textures = profile.textures().stream()
                .map(texture ->
                        texture.signature() == null || texture.signature().isBlank()
                                ? new Property(texture.name(), texture.value())
                                : new Property(texture.name(), texture.value(), texture.signature()))
                .toList();
        return createGameProfile(profile.id(), profile.name(), textures);
    }

    @Override
    public boolean actuallyHurt(org.bukkit.entity.Player bot, float amount, EntityDamageEvent event) {
        return event == null || !event.isCancelled();
    }

    @Override
    public void hurt(
            org.bukkit.entity.LivingEntity target,
            org.bukkit.entity.@org.jspecify.annotations.Nullable Player attacker,
            float amount,
            DamageKind kind) {
        net.minecraft.world.entity.LivingEntity nativeTarget = ((CraftLivingEntity) target).getHandle();
        if (!(nativeTarget.level() instanceof ServerLevel level)) {
            return;
        }
        nativeTarget.hurtServer(level, damageSource(nativeTarget, attacker, kind), amount);
    }

    @Override
    public void explode(
            Location location,
            org.bukkit.entity.@org.jspecify.annotations.Nullable Player cause,
            float power,
            boolean blockDamage) {
        Level level = ((CraftWorld) location.getWorld()).getHandle();
        Player nativeCause = cause == null ? null : nativePlayer(cause);
        level.explode(
                nativeCause,
                location.getX(),
                location.getY(),
                location.getZ(),
                power,
                blockDamage ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    @Override
    public void playSound(Location location, String soundKey, String source, float volume, float pitch) {
        org.bukkit.SoundCategory soundCategory = org.bukkit.SoundCategory.valueOf(source.toUpperCase(Locale.ROOT));
        location.getWorld().playSound(location, soundKey, soundCategory, volume, pitch);
    }

    @Override
    public void playSoundOnPlayer(org.bukkit.entity.Player player, String soundKey, float volume, float pitch) {
        player.playSound(
                java.util.Objects.requireNonNull(player.getLocation(), "player location"),
                soundKey,
                org.bukkit.SoundCategory.PLAYERS,
                volume,
                pitch);
    }

    @Override
    public boolean useItemOnBlock(
            org.bukkit.entity.Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            com.monkey.ultimatebot.common.model.EquipmentSlotKind hand) {
        Player nativeBot = nativePlayer(bot);
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        BlockPos pos = BlockPos.containing(clicked.getX(), clicked.getY(), clicked.getZ());
        Vec3 hit = new Vec3(hitLocation.getX(), hitLocation.getY(), hitLocation.getZ());
        InteractionResult result = nmsStack.getItem()
                .useOn(new UseOnContext(
                        nativeBot.level(),
                        nativeBot,
                        toInteractionHand(hand),
                        nmsStack,
                        new BlockHitResult(hit, toDirection(face), pos, false)));
        return !result.equals(InteractionResult.PASS);
    }

    @Override
    public void throwEnderpearl(org.bukkit.entity.Player bot, Vector targetPos) {
        throwEnderpearl(nativePlayer(bot), new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()));
    }

    @Override
    public void sendTabListAdd(org.bukkit.entity.Player viewer, ITrainingBot bot) {
        Player nativeBot = nativeBot(bot);
        GameProfile profile = nativeBot.getGameProfile();
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(
                nativeBot.getUUID(),
                profile,
                true,
                0,
                GameType.SURVIVAL,
                Component.literal(getProfileName(toProfileData(profile))),
                true,
                0,
                null);
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                java.util.EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), List.of(entry));
        ((CraftPlayer) viewer).getHandle().connection.send(packet);
    }

    @Override
    public void sendSpawnAndMeta(org.bukkit.entity.Player viewer, ITrainingBot bot) {
        Player nativeBot = nativeBot(bot);
        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.connection.send(new ClientboundAddEntityPacket(
                nativeBot.getId(),
                nativeBot.getUUID(),
                nativeBot.getX(),
                nativeBot.getY(),
                nativeBot.getZ(),
                nativeBot.getXRot(),
                nativeBot.getYRot(),
                EntityTypes.PLAYER,
                0,
                new Vec3(0, 0, 0),
                nativeBot.getYHeadRot()));
        byte yHeadRot = packDegrees(nativeBot.getYHeadRot());
        byte yBodyRot = packDegrees(nativeBot.getYRot());
        byte xRot = packDegrees(nativeBot.getXRot());
        handle.connection.send(new ClientboundRotateHeadPacket(nativeBot, yHeadRot));
        handle.connection.send(
                new ClientboundMoveEntityPacket.Rot(nativeBot.getId(), yBodyRot, xRot, nativeBot.onGround()));
        sendMetadata(handle, nativeBot);
    }

    @Override
    public void sendEquipment(
            org.bukkit.entity.Player viewer,
            ITrainingBot bot,
            Map<com.monkey.ultimatebot.common.model.EquipmentSlotKind, ItemStack> equipment) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> converted =
                toNmsEquipment(equipment);
        if (!converted.isEmpty()) {
            ((CraftPlayer) viewer)
                    .getHandle()
                    .connection
                    .send(new ClientboundSetEquipmentPacket(nativeBot(bot).getId(), converted));
        }
    }

    @Override
    public void broadcastEquipment(
            ITrainingBot bot, Map<com.monkey.ultimatebot.common.model.EquipmentSlotKind, ItemStack> equipment) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> converted =
                toNmsEquipment(equipment);
        if (converted.isEmpty()) {
            return;
        }
        ClientboundSetEquipmentPacket packet =
                new ClientboundSetEquipmentPacket(nativeBot(bot).getId(), converted);
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) online).getHandle().connection.send(packet);
        }
    }

    @Override
    public void broadcastMetadata(ITrainingBot bot) {
        Player nativeBot = nativeBot(bot);
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            sendMetadata(((CraftPlayer) online).getHandle(), nativeBot);
        }
    }

    @Override
    public ItemStack getBotItem(ITrainingBot bot, com.monkey.ultimatebot.common.model.EquipmentSlotKind slot) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return ItemStackAccess.empty();
        }
        return CraftItemStack.asBukkitCopy(nativeBot(bot).getItemBySlot(nmsSlot));
    }

    @Override
    public void setBotItem(
            ITrainingBot bot,
            com.monkey.ultimatebot.common.model.EquipmentSlotKind slot,
            org.bukkit.inventory.@org.jspecify.annotations.Nullable ItemStack stack) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return;
        }
        nativeBot(bot).setItemSlot(nmsSlot, CraftItemStack.asNMSCopy(stack == null ? ItemStackAccess.empty() : stack));
    }

    @Override
    public void clearBotInventory(ITrainingBot bot) {
        nativeBot(bot).getInventory().clearContent();
    }

    @Override
    public void beginUsingBotItem(ITrainingBot bot, com.monkey.ultimatebot.common.model.EquipmentSlotKind hand) {
        nativeBot(bot).startUsingItem(toInteractionHand(hand));
    }

    @Override
    public void stopUsingBotItem(ITrainingBot bot) {
        nativeBot(bot).stopUsingItem();
    }

    @Override
    public boolean isBotOnGround(ITrainingBot bot) {
        return nativeBot(bot).onGround();
    }

    @Override
    public org.bukkit.util.Vector getBotVelocity(ITrainingBot bot) {
        net.minecraft.world.phys.Vec3 velocity = nativeBot(bot).getDeltaMovement();
        return new org.bukkit.util.Vector(velocity.x, velocity.y, velocity.z);
    }

    @Override
    public void setBotVelocity(ITrainingBot bot, org.bukkit.util.Vector velocity) {
        Player nativeBot = nativeBot(bot);
        nativeBot.setDeltaMovement(velocity.getX(), velocity.getY(), velocity.getZ());
        nativeBot.hurtMarked = true;
    }

    @Override
    public void setBotOnGround(ITrainingBot bot, boolean onGround) {
        nativeBot(bot).setOnGround(onGround);
    }

    @Override
    public float getBotFallDistance(ITrainingBot bot) {
        return (float) nativeBot(bot).fallDistance;
    }

    @Override
    public void setBotFallDistance(ITrainingBot bot, float fallDistance) {
        nativeBot(bot).fallDistance = fallDistance;
    }

    @Override
    public void attackTarget(ITrainingBot bot, org.bukkit.entity.LivingEntity target) {
        nativeBot(bot).attack(((CraftLivingEntity) target).getHandle());
    }

    public net.minecraft.server.level.ClientInformation createClientInformation() {
        return new net.minecraft.server.level.ClientInformation(
                "it_IT",
                10,
                ChatVisiblity.FULL,
                true,
                0,
                HumanoidArm.RIGHT,
                false,
                true,
                net.minecraft.server.level.ParticleStatus.ALL);
    }

    public ServerLevel getServerLevel(ServerPlayer player) {
        return player.level();
    }

    private void playSoundOnPlayer(Player player, SoundEvent sound, float volume, float pitch) {
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

    private void throwEnderpearl(Player bot, Vec3 targetPos) {
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
        bot.swing(InteractionHand.MAIN_HAND);
        playSoundOnPlayer(
                bot,
                net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW,
                0.5f,
                0.4f / (bot.level().getRandom().nextFloat() * 0.4f + 0.8f));
    }

    private DamageSource damageSource(
            net.minecraft.world.entity.LivingEntity target,
            org.bukkit.entity.@org.jspecify.annotations.Nullable Player attacker,
            DamageKind kind) {
        return switch (kind) {
            case PLAYER_ATTACK ->
                attacker == null
                        ? target.damageSources().generic()
                        : nativePlayer(attacker).damageSources().playerAttack(nativePlayer(attacker));
            case LAVA -> target.damageSources().lava();
            case GENERIC -> target.damageSources().generic();
        };
    }

    private static Player nativeBot(ITrainingBot bot) {
        if (bot instanceof Player nativeBot) {
            return nativeBot;
        }
        return nativePlayer(bot.asBukkitPlayer());
    }

    private static Player nativePlayer(org.bukkit.entity.Player player) {
        if (player instanceof CraftPlayer craftPlayer) {
            return craftPlayer.getHandle();
        }
        if (player instanceof CraftHumanEntity craftHuman) {
            return craftHuman.getHandle();
        }
        throw new IllegalArgumentException(
                "Unsupported player implementation: " + player.getClass().getName());
    }

    private static InteractionHand toInteractionHand(com.monkey.ultimatebot.common.model.EquipmentSlotKind hand) {
        return hand == com.monkey.ultimatebot.common.model.EquipmentSlotKind.OFF_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
    }

    private static Direction toDirection(BlockFace face) {
        return switch (face) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
            default -> Direction.UP;
        };
    }

    private static net.minecraft.world.entity.@org.jspecify.annotations.Nullable EquipmentSlot toNmsSlot(
            com.monkey.ultimatebot.common.model.EquipmentSlotKind slot) {
        return switch (slot) {
            case HAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND;
            case OFF_HAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            case FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;
            case LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            default -> null;
        };
    }

    private static List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>>
            toNmsEquipment(Map<com.monkey.ultimatebot.common.model.EquipmentSlotKind, ItemStack> equipment) {
        return equipment.entrySet().stream()
                .map(entry -> {
                    net.minecraft.world.entity.EquipmentSlot slot = toNmsSlot(entry.getKey());
                    return slot == null ? null : Pair.of(slot, CraftItemStack.asNMSCopy(entry.getValue()));
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static void sendMetadata(ServerPlayer viewer, Player bot) {
        List<SynchedEntityData.DataValue<?>> metadata = bot.getEntityData().getNonDefaultValues();
        if (metadata != null && !metadata.isEmpty()) {
            viewer.connection.send(new ClientboundSetEntityDataPacket(bot.getId(), metadata));
        }
    }

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }

    @Override
    public void openBotGui(org.bukkit.entity.Player player, UltimateBot plugin, BotType botType) {
        new NewBotGUI_v26_2(player, plugin, botType).open();
    }
}
