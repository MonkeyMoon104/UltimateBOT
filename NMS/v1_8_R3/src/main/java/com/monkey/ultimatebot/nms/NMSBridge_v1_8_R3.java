package com.monkey.ultimatebot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.TrainingBotHandle_v1_8_R3;
import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_8_R3;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityEnderPearl;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Real bot runtime for Spigot 1.8.8 ({@code v1_8_R3}). See package-info for supported surface and
 * gaps versus 1.17+.
 *
 * <p>Source is Java 8-compatible so the module can load on Java 8 JVMs that host 1.8.8 servers.
 */
public final class NMSBridge_v1_8_R3 implements INMSBridge {

    private static final Set<PlatformCapability> CAPABILITIES = PlatformCapability.through1_8();

    @Override
    public Set<PlatformCapability> capabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean isBotRuntimeSupported() {
        return true;
    }

    @Override
    public ITrainingBot createTrainingBot(
            Location spawn,
            BotProfileData profile,
            Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) spawn.getWorld()).getHandle();
        PlayerInteractManager interactManager = new PlayerInteractManager(world);
        TrainingBot_v1_8_R3 entity =
                new TrainingBot_v1_8_R3(
                        server,
                        world,
                        toGameProfile(profile),
                        interactManager,
                        spawn.getX(),
                        spawn.getY(),
                        spawn.getZ(),
                        spawn.getYaw(),
                        spawn.getPitch(),
                        targetPlayer,
                        follow,
                        plugin,
                        deadBotMessage,
                        deadBotEventMessage,
                        botOptions);
        return entity.handle();
    }

    @Override
    public void registerBotEntity(ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        nativeBot.getWorld().addEntity(nativeBot);
    }

    @Override
    public void addToProfileCache(ITrainingBot bot) {
        ((CraftServer) Bukkit.getServer()).getServer().getUserCache().a(nativeBot(bot).getProfile());
    }

    @Override
    public void removeFromProfileCache(UUID botUUID) {
        // UserCache on 1.8 has no public remove-by-UUID; leave the soft cache entry.
    }

    @Override
    public void moveBot(Player bot, double x, double y, double z) {
        EntityPlayer nativeBot = nativePlayer(bot);
        nativeBot.motX = 0.0D;
        nativeBot.motY = 0.0D;
        nativeBot.motZ = 0.0D;
        nativeBot.fallDistance = 0.0F;
        nativeBot.setPositionRotation(x, y, z, nativeBot.yaw, nativeBot.pitch);
        broadcastBotPosition(bot);
    }

    @Override
    public void broadcastBotPosition(Player bot) {
        EntityPlayer nativeBot = nativePlayer(bot);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(nativeBot);
        byte packedYaw = packDegrees(nativeBot.yaw);
        PacketPlayOutEntityHeadRotation headPacket =
                new PacketPlayOutEntityHeadRotation(nativeBot, packedYaw);
        Location botLoc = bot.getLocation();
        double maxDistSq = 64.0D * 64.0D;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getEntityId() == bot.getEntityId()) {
                continue;
            }
            if (online.getWorld() != bot.getWorld()) {
                continue;
            }
            Location viewerLoc = online.getLocation();
            double dx = viewerLoc.getX() - botLoc.getX();
            double dz = viewerLoc.getZ() - botLoc.getZ();
            if (dx * dx + dz * dz > maxDistSq) {
                continue;
            }
            EntityPlayer handle = ((CraftPlayer) online).getHandle();
            handle.playerConnection.sendPacket(teleportPacket);
            handle.playerConnection.sendPacket(headPacket);
        }
    }

    @Override
    public void setBotRotation(Player bot, float yaw, float pitch) {
        EntityPlayer nativeBot = nativePlayer(bot);
        nativeBot.yaw = yaw;
        nativeBot.pitch = pitch;
        nativeBot.aI = yaw;
        nativeBot.aK = yaw;
        byte packedYaw = packDegrees(yaw);
        byte packedPitch = packDegrees(pitch);
        PacketPlayOutEntityHeadRotation headPacket =
                new PacketPlayOutEntityHeadRotation(nativeBot, packedYaw);
        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        nativeBot.getId(), packedYaw, packedPitch, nativeBot.onGround);
        Location botLoc = bot.getLocation();
        double maxDistSq = 64.0D * 64.0D;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getEntityId() == bot.getEntityId()) {
                continue;
            }
            if (online.getWorld() != bot.getWorld()) {
                continue;
            }
            Location viewerLoc = online.getLocation();
            double dx = viewerLoc.getX() - botLoc.getX();
            double dz = viewerLoc.getZ() - botLoc.getZ();
            if (dx * dx + dz * dz > maxDistSq) {
                continue;
            }
            EntityPlayer handle = ((CraftPlayer) online).getHandle();
            handle.playerConnection.sendPacket(headPacket);
            handle.playerConnection.sendPacket(lookPacket);
        }
    }

    @Override
    public BotProfileData copyProfileWithTextures(Player viewer, UUID botUUID, String botName) {
        GameProfile viewerProfile = nativePlayer(viewer).getProfile();
        GameProfile profile = new GameProfile(botUUID, botName);
        profile.getProperties().putAll(viewerProfile.getProperties());
        return toProfileData(profile);
    }

    @Override
    public BotProfileData createProfileWithTexture(
            UUID botUUID, String botName, String textureValue, @Nullable String textureSignature) {
        GameProfile profile = new GameProfile(botUUID, botName);
        Property property =
                textureSignature == null || textureSignature.isEmpty()
                        ? new Property("textures", textureValue)
                        : new Property("textures", textureValue, textureSignature);
        profile.getProperties().put("textures", property);
        return toProfileData(profile);
    }

    @Override
    public String getProfileName(BotProfileData profile) {
        return profile.name();
    }

    @Override
    public boolean actuallyHurt(Player bot, float amount, EntityDamageEvent event) {
        return event == null || !event.isCancelled();
    }

    @Override
    public void hurt(LivingEntity target, @Nullable Player attacker, float amount, DamageKind kind) {
        EntityLiving nativeTarget = ((CraftLivingEntity) target).getHandle();
        nativeTarget.damageEntity(damageSource(attacker, kind), amount);
    }

    @Override
    public void explode(Location location, @Nullable Player cause, float power, boolean blockDamage) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        EntityHuman nativeCause = cause == null ? null : nativePlayer(cause);
        world.createExplosion(
                nativeCause, location.getX(), location.getY(), location.getZ(), power, false, blockDamage);
    }

    @Override
    public void playSound(Location location, String soundKey, String source, float volume, float pitch) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.makeSound(location.getX(), location.getY(), location.getZ(), soundKey, volume, pitch);
    }

    @Override
    public void playSoundOnPlayer(Player player, String soundKey, float volume, float pitch) {
        EntityPlayer handle = nativePlayer(player);
        handle.world.makeSound(handle, soundKey, volume, pitch);
    }

    @Override
    public boolean useItemOnBlock(
            Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            EquipmentSlotKind hand) {
        return false;
    }

    @Override
    public void throwEnderpearl(Player bot, Vector targetPos) {
        EntityPlayer nativeBot = nativePlayer(bot);
        EntityEnderPearl pearl = new EntityEnderPearl(nativeBot.world, nativeBot);
        double dx = targetPos.getX() - nativeBot.locX;
        double dy = targetPos.getY() - (nativeBot.locY + nativeBot.getHeadHeight());
        double dz = targetPos.getZ() - nativeBot.locZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float velocity =
                distance < 15
                        ? (float) Math.min(distance * 0.08, 1.2)
                        : (float) Math.min(distance * 0.06, 1.8);
        pearl.shoot(dx, dy + 0.2 + distance * 0.02, dz, velocity, 0.0F);
        nativeBot.world.addEntity(pearl);
    }

    @Override
    public void sendTabListAdd(Player viewer, ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        ((CraftPlayer) viewer)
                .getHandle()
                .playerConnection
                .sendPacket(
                        new PacketPlayOutPlayerInfo(
                                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nativeBot));
    }

    @Override
    public void sendTabListRemove(Player viewer, ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        ((CraftPlayer) viewer)
                .getHandle()
                .playerConnection
                .sendPacket(
                        new PacketPlayOutPlayerInfo(
                                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nativeBot));
    }

    @Override
    public void sendSpawnAndMeta(Player viewer, ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        EntityPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(nativeBot));
        byte yaw = packDegrees(nativeBot.yaw);
        byte pitch = packDegrees(nativeBot.pitch);
        handle.playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(nativeBot, yaw));
        handle.playerConnection.sendPacket(
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        nativeBot.getId(), yaw, pitch, nativeBot.onGround));
        handle.playerConnection.sendPacket(
                new PacketPlayOutEntityMetadata(nativeBot.getId(), nativeBot.getDataWatcher(), true));
    }

    @Override
    public void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlotKind, ItemStack> equipment) {
        EntityPlayer handle = ((CraftPlayer) viewer).getHandle();
        int entityId = nativeBot(bot).getId();
        for (Map.Entry<EquipmentSlotKind, ItemStack> entry : equipment.entrySet()) {
            Integer slot = toEquipmentIndex(entry.getKey());
            if (slot == null) {
                continue;
            }
            handle.playerConnection.sendPacket(
                    new PacketPlayOutEntityEquipment(
                            entityId, slot.intValue(), CraftItemStack.asNMSCopy(entry.getValue())));
        }
    }

    @Override
    public void broadcastEquipment(ITrainingBot bot, Map<EquipmentSlotKind, ItemStack> equipment) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            sendEquipment(online, bot, equipment);
        }
    }

    @Override
    public void broadcastMetadata(ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        PacketPlayOutEntityMetadata packet =
                new PacketPlayOutEntityMetadata(nativeBot.getId(), nativeBot.getDataWatcher(), true);
        for (Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    public ItemStack getBotItem(ITrainingBot bot, EquipmentSlotKind slot) {
        Integer index = toEquipmentIndex(slot);
        if (index == null) {
            return new ItemStack(Material.AIR);
        }
        net.minecraft.server.v1_8_R3.ItemStack nms = nativeBot(bot).getEquipment(index.intValue());
        return nms == null ? new ItemStack(Material.AIR) : CraftItemStack.asBukkitCopy(nms);
    }

    @Override
    public void setBotItem(ITrainingBot bot, EquipmentSlotKind slot, @Nullable ItemStack stack) {
        ItemStack resolved = stack == null ? new ItemStack(Material.AIR) : stack;
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(resolved);
        EntityPlayer nativeBot = nativeBot(bot);
        String name = slot.name();
        // EntityHuman.setEquipment stores armor[i] (length 4). Packet/getEquipment(int) use
        // 0=hand, 1=boots, 4=helmet — HEAD as 4 AIOOBE; HAND as 0 overwrites boots.
        if ("HAND".equals(name)) {
            int hotbar = nativeBot.inventory.itemInHandIndex;
            if (hotbar >= 0 && hotbar < 9) {
                applyHeldItemAttributes(nativeBot, nativeBot.inventory.getItemInHand(), false);
                nativeBot.inventory.items[hotbar] = nms;
                applyHeldItemAttributes(nativeBot, nms, true);
            }
            return;
        }
        int armorIndex = toArmorIndex(name);
        if (armorIndex < 0) {
            return;
        }
        nativeBot.inventory.armor[armorIndex] = nms;
    }

    @Override
    public void clearBotInventory(ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        nativeBot.inventory.items = new net.minecraft.server.v1_8_R3.ItemStack[36];
        nativeBot.inventory.armor = new net.minecraft.server.v1_8_R3.ItemStack[4];
    }

    @Override
    public void beginUsingBotItem(ITrainingBot bot, EquipmentSlotKind hand) {}

    @Override
    public void stopUsingBotItem(ITrainingBot bot) {}

    @Override
    public boolean isBotOnGround(ITrainingBot bot) {
        return nativeBot(bot).onGround;
    }

    @Override
    public void setBotOnGround(ITrainingBot bot, boolean onGround) {
        nativeBot(bot).onGround = onGround;
    }

    @Override
    public Vector getBotVelocity(ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        return new Vector(nativeBot.motX, nativeBot.motY, nativeBot.motZ);
    }

    @Override
    public void setBotVelocity(ITrainingBot bot, Vector velocity) {
        EntityPlayer nativeBot = nativeBot(bot);
        nativeBot.motX = velocity.getX();
        nativeBot.motY = velocity.getY();
        nativeBot.motZ = velocity.getZ();
        nativeBot.velocityChanged = true;
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
    public void attackTarget(ITrainingBot bot, LivingEntity target) {
        EntityPlayer nativeBot = nativeBot(bot);
        syncMainHandAttackAttributes(nativeBot);
        nativeBot.attack(((CraftLivingEntity) target).getHandle());
    }

    /**
     * EntityPlayer.t_() on 1.8 never runs EntityLiving equipment-change detection, so ItemSword
     * {@code ATTACK_DAMAGE} modifiers stay off the map (fist 1.0). {@code ItemStack#B()} is the
     * item modifier multimap; remove+add of the current item is a no-op when already applied.
     */
    private static void syncMainHandAttackAttributes(EntityPlayer nativeBot) {
        net.minecraft.server.v1_8_R3.ItemStack held = nativeBot.inventory.getItemInHand();
        applyHeldItemAttributes(nativeBot, held, false);
        applyHeldItemAttributes(nativeBot, held, true);
    }

    private static void applyHeldItemAttributes(
            EntityPlayer nativeBot, net.minecraft.server.v1_8_R3.ItemStack stack, boolean add) {
        if (stack == null || stack.getItem() == null) {
            return;
        }
        if (add) {
            nativeBot.getAttributeMap().b(stack.B());
        } else {
            nativeBot.getAttributeMap().a(stack.B());
        }
    }

    private DamageSource damageSource(@Nullable Player attacker, DamageKind kind) {
        switch (kind) {
            case PLAYER_ATTACK:
                return attacker == null
                        ? DamageSource.GENERIC
                        : DamageSource.playerAttack(nativePlayer(attacker));
            case LAVA:
                return DamageSource.LAVA;
            case GENERIC:
            default:
                return DamageSource.GENERIC;
        }
    }

    private BotProfileData toProfileData(GameProfile profile) {
        List<BotProfileData.Texture> textures = new ArrayList<BotProfileData.Texture>();
        for (Property property : profile.getProperties().get("textures")) {
            textures.add(
                    new BotProfileData.Texture(property.getName(), property.getValue(), property.getSignature()));
        }
        return new BotProfileData(profile.getId(), profile.getName(), textures);
    }

    private GameProfile toGameProfile(BotProfileData profile) {
        GameProfile gameProfile = new GameProfile(profile.id(), profile.name());
        for (BotProfileData.Texture texture : profile.textures()) {
            Property property =
                    texture.signature() == null || texture.signature().isEmpty()
                            ? new Property(texture.name(), texture.value())
                            : new Property(texture.name(), texture.value(), texture.signature());
            gameProfile.getProperties().put(texture.name(), property);
        }
        return gameProfile;
    }

    private static EntityPlayer nativeBot(ITrainingBot bot) {
        if (bot instanceof TrainingBotHandle_v1_8_R3) {
            return ((TrainingBotHandle_v1_8_R3) bot).nativeEntity();
        }
        if (bot instanceof EntityPlayer) {
            return (EntityPlayer) bot;
        }
        return nativePlayer(bot.asBukkitPlayer());
    }

    private static EntityPlayer nativePlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private static @Nullable Integer toEquipmentIndex(EquipmentSlotKind slot) {
        String name = slot.name();
        if ("OFF_HAND".equals(name)) {
            return null;
        }
        if ("HAND".equals(name)) {
            return Integer.valueOf(0);
        }
        if ("FEET".equals(name)) {
            return Integer.valueOf(1);
        }
        if ("LEGS".equals(name)) {
            return Integer.valueOf(2);
        }
        if ("CHEST".equals(name)) {
            return Integer.valueOf(3);
        }
        if ("HEAD".equals(name)) {
            return Integer.valueOf(4);
        }
        return null;
    }

    /** PlayerInventory.armor: 0 boots, 1 legs, 2 chest, 3 helmet. */
    private static int toArmorIndex(String name) {
        if ("FEET".equals(name)) {
            return 0;
        }
        if ("LEGS".equals(name)) {
            return 1;
        }
        if ("CHEST".equals(name)) {
            return 2;
        }
        if ("HEAD".equals(name)) {
            return 3;
        }
        return -1;
    }

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }
}
