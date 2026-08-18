package com.monkey.ultimatebot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_19_2;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class NMSBridge_v1_19_2 implements INMSBridge {

    /** 1.17-1.19.x: no MACE, WIND_CHARGE, or ARMOR_TRIM. */
    private static final Set<PlatformCapability> CAPABILITIES = PlatformCapability.through1_19();

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
        // Player ctor places at (blockX+0.5, blockY+1, blockZ+0.5). Pass the ground BlockPos only —
        // do not moveTo(spawn) with getHighestBlockAt Y or the bot ends up inside the surface block.
        BlockPos pos = new BlockPos(spawn.getX(), spawn.getY(), spawn.getZ());
        return new TrainingBot_v1_19_2(
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
        if (nativeBot.getLevel() instanceof ServerLevel level) {
            level.addFreshEntity(nativeBot);
        }
    }

    @Override
    public void addToProfileCache(ITrainingBot bot) {
        var cache = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getProfileCache();
        if (cache != null) {
            cache.add(nativeBot(bot).getGameProfile());
        }
    }

    @Override
    public void removeFromProfileCache(UUID botUUID) {
        Object cache = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getProfileCache();
        removeProfileCacheEntry(cache, botUUID, new GameProfile(botUUID, ""));
    }

    private void removeProfileCacheEntry(
            @org.jspecify.annotations.Nullable Object cache, UUID botUUID, GameProfile profile) {
        if (cache == null) {
            return;
        }
        for (Method method : cache.getClass().getMethods()) {
            if (!method.getName().equals("remove") || method.getParameterCount() != 1) {
                continue;
            }
            Object argument = resolveRemovalArgument(method.getParameterTypes()[0], botUUID, profile);
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
            Class<?> parameterType, UUID botUUID, GameProfile profile) {
        if (parameterType.isAssignableFrom(UUID.class)) {
            return botUUID;
        }
        if (parameterType.isAssignableFrom(GameProfile.class)) {
            return profile;
        }
        return null;
    }

    @Override
    public void moveBot(org.bukkit.entity.Player bot, double x, double y, double z) {
        nativePlayer(bot).moveTo(x, y, z);
    }

    @Override
    public BotProfileData copyProfileWithTextures(org.bukkit.entity.Player viewer, UUID botUUID, String botName) {
        GameProfile viewerProfile = ((CraftPlayer) viewer).getProfile();
        Collection<Property> textures = viewerProfile.getProperties().get("textures");
        GameProfile profile = new GameProfile(botUUID, botName);
        if (!textures.isEmpty()) {
            profile.getProperties().put("textures", textures.iterator().next());
        }
        return toProfileData(profile);
    }

    @Override
    public BotProfileData createProfileWithTexture(
            UUID botUUID, String botName, String textureValue, @org.jspecify.annotations.Nullable String textureSignature) {
        GameProfile profile = new GameProfile(botUUID, botName);
        Property property = textureSignature == null || textureSignature.isBlank()
                ? new Property("textures", textureValue)
                : new Property("textures", textureValue, textureSignature);
        profile.getProperties().put("textures", property);
        return toProfileData(profile);
    }

    @Override
    public String getProfileName(BotProfileData profile) {
        return profile.name();
    }

    private BotProfileData toProfileData(GameProfile profile) {
        List<BotProfileData.Texture> textures = profile.getProperties().get("textures").stream()
                .map(property -> new BotProfileData.Texture(property.getName(), property.getValue(), property.getSignature()))
                .toList();
        return new BotProfileData(profile.getId(), profile.getName(), textures);
    }

    private GameProfile toGameProfile(BotProfileData profile) {
        GameProfile gameProfile = new GameProfile(profile.id(), profile.name());
        for (BotProfileData.Texture texture : profile.textures()) {
            Property property = texture.signature() == null || texture.signature().isBlank()
                    ? new Property(texture.name(), texture.value())
                    : new Property(texture.name(), texture.value(), texture.signature());
            gameProfile.getProperties().put(texture.name(), property);
        }
        return gameProfile;
    }

    @Override
    public boolean actuallyHurt(org.bukkit.entity.Player bot, float amount, EntityDamageEvent event) {
        return event == null || !event.isCancelled();
    }

    @Override
    public void hurt(org.bukkit.entity.LivingEntity target, org.bukkit.entity.@org.jspecify.annotations.Nullable Player attacker, float amount, DamageKind kind) {
        net.minecraft.world.entity.LivingEntity nativeTarget = ((CraftLivingEntity) target).getHandle();
        if (!(nativeTarget.getLevel() instanceof ServerLevel)) {
            return;
        }
        nativeTarget.hurt(damageSource(attacker, kind), amount);
    }

    @Override
    public void explode(Location location, org.bukkit.entity.@org.jspecify.annotations.Nullable Player cause, float power, boolean blockDamage) {
        Level level = ((CraftWorld) location.getWorld()).getHandle();
        Player nativeCause = cause == null ? null : nativePlayer(cause);
        level.explode(
                nativeCause,
                location.getX(),
                location.getY(),
                location.getZ(),
                power,
                blockDamage ? Explosion.BlockInteraction.BREAK : Explosion.BlockInteraction.NONE);
    }

    @Override
    public void playSound(Location location, String soundKey, String source, float volume, float pitch) {
        Level level = ((CraftWorld) location.getWorld()).getHandle();
        ResourceLocation key = ResourceLocation.tryParse(soundKey);
        if (key == null) {
            return;
        }
        SoundEvent sound = Registry.SOUND_EVENT.get(key);
        if (sound == null) {
            return;
        }
        SoundSource soundSource = SoundSource.valueOf(source.toUpperCase(Locale.ROOT));
        playSound(level, new BlockPos(location.getX(), location.getY(), location.getZ()), sound, soundSource, volume, pitch);
    }

    @Override
    public void playSoundOnPlayer(org.bukkit.entity.Player player, String soundKey, float volume, float pitch) {
        ResourceLocation key = ResourceLocation.tryParse(soundKey);
        if (key == null) {
            return;
        }
        SoundEvent sound = Registry.SOUND_EVENT.get(key);
        if (sound == null) {
            return;
        }
        playSoundOnPlayer(nativePlayer(player), sound, volume, pitch);
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
        BlockPos pos = new BlockPos(clicked.getX(), clicked.getY(), clicked.getZ());
        Vec3 hit = new Vec3(hitLocation.getX(), hitLocation.getY(), hitLocation.getZ());
        InteractionResult result = nmsStack.getItem()
                .useOn(new UseOnContext(
                        nativeBot.getLevel(),
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
        ServerPlayer viewerHandle = ((CraftPlayer) viewer).getHandle();
        MinecraftServer server = viewerHandle.server;
        ServerLevel level = viewerHandle.getLevel();
        ServerPlayer tabPlayer = new ServerPlayer(server, level, profile, null);
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(
                ClientboundPlayerInfoPacket.Action.ADD_PLAYER, tabPlayer);
        viewerHandle.connection.send(packet);
    }

    @Override
    public void sendTabListRemove(org.bukkit.entity.Player viewer, ITrainingBot bot) {
        ServerPlayer viewerHandle = ((CraftPlayer) viewer).getHandle();
        GameProfile profile = nativeBot(bot).getGameProfile();
        ServerPlayer tabPlayer = new ServerPlayer(viewerHandle.server, viewerHandle.getLevel(), profile, null);
        viewerHandle.connection.send(
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, tabPlayer));
    }

    @Override
    public void sendSpawnAndMeta(org.bukkit.entity.Player viewer, ITrainingBot bot) {
        Player nativeBot = nativeBot(bot);
        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        // Named-player spawn; AddEntity(PLAYER) does not render fake players on these clients.
        handle.connection.send(new ClientboundAddPlayerPacket(nativeBot));
        byte yHeadRot = packDegrees(nativeBot.getYHeadRot());
        byte yBodyRot = packDegrees(nativeBot.getYRot());
        byte xRot = packDegrees(nativeBot.getXRot());
        handle.connection.send(new ClientboundRotateHeadPacket(nativeBot, yHeadRot));
        handle.connection.send(new ClientboundMoveEntityPacket.Rot(nativeBot.getId(), yBodyRot, xRot, nativeBot.isOnGround()));
        sendMetadata(handle, nativeBot);
    }

    @Override
    public void sendEquipment(org.bukkit.entity.Player viewer, ITrainingBot bot, Map<com.monkey.ultimatebot.common.model.EquipmentSlotKind, ItemStack> equipment) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> converted = toNmsEquipment(equipment);
        if (!converted.isEmpty()) {
            ((CraftPlayer) viewer).getHandle().connection.send(new ClientboundSetEquipmentPacket(nativeBot(bot).getId(), converted));
        }
    }

    @Override
    public void broadcastEquipment(ITrainingBot bot, Map<com.monkey.ultimatebot.common.model.EquipmentSlotKind, ItemStack> equipment) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> converted = toNmsEquipment(equipment);
        if (converted.isEmpty()) {
            return;
        }
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(nativeBot(bot).getId(), converted);
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
            return new ItemStack(Material.AIR);
        }
        return CraftItemStack.asBukkitCopy(nativeBot(bot).getItemBySlot(nmsSlot));
    }

    @Override
    public void setBotItem(ITrainingBot bot, com.monkey.ultimatebot.common.model.EquipmentSlotKind slot, org.bukkit.inventory.@org.jspecify.annotations.Nullable ItemStack stack) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return;
        }
        nativeBot(bot).setItemSlot(nmsSlot, CraftItemStack.asNMSCopy(stack == null ? new ItemStack(Material.AIR) : stack));
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
        return nativeBot(bot).isOnGround();
    }

    @Override
    public org.bukkit.util.Vector getBotVelocity(ITrainingBot bot) {
        net.minecraft.world.phys.Vec3 velocity = nativeBot(bot).getDeltaMovement();
        return new org.bukkit.util.Vector(velocity.x, velocity.y, velocity.z);
    }

    @Override
    public void setBotVelocity(ITrainingBot bot, org.bukkit.util.Vector velocity) {
        Player nativeBot = nativeBot(bot);
        // TrainingBot_v1_19_2 runs AI before super.tick(), so travel() consumes this same tick.
        nativeBot.setDeltaMovement(velocity.getX(), velocity.getY(), velocity.getZ());
        nativeBot.hurtMarked = true;
    }

    @Override
    public void setBotOnGround(ITrainingBot bot, boolean onGround) {
        nativeBot(bot).setOnGround(onGround);
    }
    @Override
    public float getBotFallDistance(ITrainingBot bot) {
        return nativeBot(bot).fallDistance;
    }

    @Override
    public void setBotFallDistance(ITrainingBot bot, float fallDistance) {
        nativeBot(bot).fallDistance = fallDistance;
    }

    @Override
    public void attackTarget(ITrainingBot bot, org.bukkit.entity.LivingEntity target) {
        nativeBot(bot).attack(((CraftLivingEntity) target).getHandle());
    }

    @Override
    public void setExplosiveMinecartFuseTicks(org.bukkit.entity.Entity minecart, int ticks) {
        // Cannot write MinecartTNT.fuse from the plugin classloader (private → IllegalAccessError).
        // Prefer Craft#setFuseTicks when present; otherwise inert (-1) is already the NMS default,
        // and arming uses a Bukkit explosion.
        try {
            java.lang.reflect.Method method = minecart.getClass().getMethod("setFuseTicks", int.class);
            method.invoke(minecart, ticks);
            return;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // fall through
        }
        if (ticks < 0) {
            return;
        }
        org.bukkit.Location location = minecart.getLocation();
        org.bukkit.World world = location.getWorld();
        minecart.remove();
        if (world != null) {
            world.createExplosion(location, 4.0F, false, true);
        }
    }

    public ServerLevel getServerLevel(ServerPlayer player) {
        return player.getLevel();
    }

    private void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, pos, sound, source, volume, pitch);
    }

    private void playSoundOnPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.playSound(sound, volume, pitch);
    }

    private void throwEnderpearl(Player bot, Vec3 targetPos) {
        ThrownEnderpearl enderpearl = new ThrownEnderpearl(EntityType.ENDER_PEARL, bot.getLevel());
        enderpearl.setOwner(bot);
        Vec3 botPos = bot.position().add(0, bot.getEyeHeight(), 0);
        Vec3 direction = targetPos.subtract(botPos);
        double distance = direction.length();
        double velocityScale = distance < 15 ? Math.min(distance * 0.08, 1.2) : Math.min(distance * 0.06, 1.8);
        Vec3 velocity = direction.normalize().scale(velocityScale);
        velocity = velocity.add(0, 0.2 + distance * 0.02, 0);
        enderpearl.setPos(botPos.x, botPos.y, botPos.z);
        enderpearl.setDeltaMovement(velocity);
        bot.getLevel().addFreshEntity(enderpearl);
        bot.swing(InteractionHand.MAIN_HAND);
        playSoundOnPlayer(
                bot,
                net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW,
                0.5f,
                0.4f / (bot.getLevel().getRandom().nextFloat() * 0.4f + 0.8f));
    }

    private DamageSource damageSource(
            org.bukkit.entity.@org.jspecify.annotations.Nullable Player attacker,
            DamageKind kind) {
        return switch (kind) {
            case PLAYER_ATTACK -> attacker == null
                    ? DamageSource.GENERIC
                    : DamageSource.playerAttack(nativePlayer(attacker));
            case LAVA -> DamageSource.LAVA;
            case GENERIC -> DamageSource.GENERIC;
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
        throw new IllegalArgumentException("Unsupported player implementation: " + player.getClass().getName());
    }

    private static InteractionHand toInteractionHand(com.monkey.ultimatebot.common.model.EquipmentSlotKind hand) {
        return hand == com.monkey.ultimatebot.common.model.EquipmentSlotKind.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
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

    private static net.minecraft.world.entity.@org.jspecify.annotations.Nullable EquipmentSlot toNmsSlot(com.monkey.ultimatebot.common.model.EquipmentSlotKind slot) {
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

    private static List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> toNmsEquipment(
            Map<com.monkey.ultimatebot.common.model.EquipmentSlotKind, ItemStack> equipment) {
        return equipment.entrySet().stream()
                .map(entry -> {
                    net.minecraft.world.entity.EquipmentSlot slot = toNmsSlot(entry.getKey());
                    return slot == null ? null : Pair.of(slot, CraftItemStack.asNMSCopy(entry.getValue()));
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static void sendMetadata(ServerPlayer viewer, Player bot) {
        viewer.connection.send(new ClientboundSetEntityDataPacket(bot.getId(), bot.getEntityData(), true));
    }

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }
}
