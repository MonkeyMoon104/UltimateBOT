package com.monkey.ultimatebot.nms;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.TrainingBotHandle_v1_7_R4;
import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_7_R4;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.EntityEnderPearl;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityLook;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Real bot runtime for Spigot 1.7.10 ({@code v1_7_R4}).
 *
 * <p>Source is Java 8-compatible so the module can load on Java 8 JVMs that host 1.7.10 servers.
 */
public final class NMSBridge_v1_7_R4 implements INMSBridge {

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
        TrainingBot_v1_7_R4 entity =
                new TrainingBot_v1_7_R4(
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
        nativeBot.world.addEntity(nativeBot);
    }

    @Override
    public void addToProfileCache(ITrainingBot bot) {
        ((CraftServer) Bukkit.getServer()).getServer().getUserCache().a(nativeBot(bot).getProfile());
    }

    @Override
    public void removeFromProfileCache(UUID botUUID) {}

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
        for (Player online : onlinePlayers()) {
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
        nativeBot.aO = yaw;
        nativeBot.aN = yaw;
        byte packedYaw = packDegrees(yaw);
        byte packedPitch = packDegrees(pitch);
        PacketPlayOutEntityHeadRotation headPacket =
                new PacketPlayOutEntityHeadRotation(nativeBot, packedYaw);
        Packet lookPacket =
                createEntityLookPacket(nativeBot.getId(), packedYaw, packedPitch, nativeBot.onGround);
        Location botLoc = bot.getLocation();
        double maxDistSq = 64.0D * 64.0D;
        for (Player online : onlinePlayers()) {
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
        Packet packet = createPlayerInfoPacket(nativeBot, true);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void sendTabListRemove(Player viewer, ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        Packet packet = createPlayerInfoPacket(nativeBot, false);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    private static Packet createPlayerInfoPacket(EntityPlayer nativeBot, boolean add) {
        try {
            Class<?> packetClass = PacketPlayOutPlayerInfo.class;
            String factoryName = add ? "addPlayer" : "removePlayer";
            try {
                java.lang.reflect.Method factory = packetClass.getDeclaredMethod(factoryName, EntityPlayer.class);
                factory.setAccessible(true);
                Object packet = factory.invoke(null, nativeBot);
                if (packet instanceof Packet) {
                    return (Packet) packet;
                }
            } catch (NoSuchMethodException ignored) {}

            java.lang.reflect.Constructor<?>[] ctors = packetClass.getDeclaredConstructors();

            String name = nativeBot.getName();
            int ping = nativeBot.ping;
            GameProfile profile = nativeBot.getProfile();

            for (java.lang.reflect.Constructor<?> ctor : ctors) {
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length == 0) {
                    ctor.setAccessible(true);
                    Object instance = ctor.newInstance();
                    setPacketFieldIfPresent(packetClass, instance, "a", name);
                    setPacketFieldIfPresent(packetClass, instance, "b", Boolean.valueOf(add));
                    setPacketFieldIfPresent(packetClass, instance, "c", Integer.valueOf(ping));
                    setPacketFieldIfPresent(packetClass, instance, "action", Integer.valueOf(add ? 0 : 4));
                    setPacketFieldIfPresent(packetClass, instance, "player", profile);
                    setPacketFieldIfPresent(packetClass, instance, "gamemode", Integer.valueOf(0));
                    setPacketFieldIfPresent(packetClass, instance, "ping", Integer.valueOf(ping));
                    setPacketFieldIfPresent(packetClass, instance, "username", name);
                    if (instance instanceof Packet) {
                        return (Packet) instance;
                    }
                    continue;
                }
                if (p.length < 1 || p.length > 3) {
                    continue;
                }

                Object[] args = new Object[p.length];
                boolean supported = true;
                for (int i = 0; i < p.length; i++) {
                    Class<?> t = p[i];
                    if (t == String.class) {
                        args[i] = name;
                    } else if (t == boolean.class || t == Boolean.class) {
                        args[i] = Boolean.valueOf(add);
                    } else if (t == int.class || t == Integer.class) {
                        args[i] = Integer.valueOf(ping);
                    } else if (t == long.class || t == Long.class) {
                        args[i] = Long.valueOf((long) ping);
                    } else if (t == GameProfile.class) {
                        args[i] = profile;
                    } else {
                        supported = false;
                        break;
                    }
                }

                if (!supported) {
                    continue;
                }

                ctor.setAccessible(true);
                Object instance = ctor.newInstance(args);
                if (instance instanceof Packet) {
                    return (Packet) instance;
                }
            }

            StringBuilder sb = new StringBuilder();
            for (java.lang.reflect.Constructor<?> ctor : ctors) {
                sb.append("(");
                Class<?>[] p = ctor.getParameterTypes();
                for (int i = 0; i < p.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(p[i].getName());
                }
                sb.append(") ");
            }
            int publicCtorCount = packetClass.getConstructors().length;
            throw new IllegalStateException(
                    "No compatible PacketPlayOutPlayerInfo ctor found. " +
                            "declaredCtorCount=" + ctors.length +
                            ", publicCtorCount=" + publicCtorCount +
                            ", isInterface=" + packetClass.isInterface() +
                            ", isEnum=" + packetClass.isEnum() +
                            ", isAnnotation=" + packetClass.isAnnotation() +
                            ", Available: " + sb);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create PacketPlayOutPlayerInfo via reflection", e);
        }
    }

    private static void setPacketFieldIfPresent(
            Class<?> ownerClass, Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ignored) {}
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
                createEntityLookPacket(nativeBot.getId(), yaw, pitch, nativeBot.onGround));
        handle.playerConnection.sendPacket(
                new PacketPlayOutEntityMetadata(nativeBot.getId(), nativeBot.getDataWatcher(), true));
    }

    private static Packet createEntityLookPacket(int entityId, byte yaw, byte pitch, boolean onGround) {
        try {
            Class<?> packetClass = PacketPlayOutEntityLook.class;
            for (java.lang.reflect.Constructor<?> ctor : packetClass.getDeclaredConstructors()) {
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length == 3
                        && (p[0] == int.class || p[0] == Integer.class)
                        && (p[1] == byte.class || p[1] == Byte.class)
                        && (p[2] == byte.class || p[2] == Byte.class)) {
                    ctor.setAccessible(true);
                    Object packet = ctor.newInstance(
                            Integer.valueOf(entityId), Byte.valueOf(yaw), Byte.valueOf(pitch));
                    if (packet instanceof Packet) {
                        return (Packet) packet;
                    }
                }
                if (p.length == 4
                        && (p[0] == int.class || p[0] == Integer.class)
                        && (p[1] == byte.class || p[1] == Byte.class)
                        && (p[2] == byte.class || p[2] == Byte.class)
                        && (p[3] == boolean.class || p[3] == Boolean.class)) {
                    ctor.setAccessible(true);
                    Object packet = ctor.newInstance(
                            Integer.valueOf(entityId),
                            Byte.valueOf(yaw),
                            Byte.valueOf(pitch),
                            Boolean.valueOf(onGround));
                    if (packet instanceof Packet) {
                        return (Packet) packet;
                    }
                }
            }
            throw new IllegalStateException("No compatible PacketPlayOutEntityLook constructor found");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build PacketPlayOutEntityLook", e);
        }
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
        for (Player online : onlinePlayers()) {
            sendEquipment(online, bot, equipment);
        }
    }

    @Override
    public void broadcastMetadata(ITrainingBot bot) {
        EntityPlayer nativeBot = nativeBot(bot);
        PacketPlayOutEntityMetadata packet =
                new PacketPlayOutEntityMetadata(nativeBot.getId(), nativeBot.getDataWatcher(), true);
        for (Player online : onlinePlayers()) {
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    public ItemStack getBotItem(ITrainingBot bot, EquipmentSlotKind slot) {
        Integer index = toEquipmentIndex(slot);
        if (index == null) {
            return new ItemStack(Material.AIR);
        }
        net.minecraft.server.v1_7_R4.ItemStack nms = nativeBot(bot).getEquipment(index.intValue());
        return nms == null ? new ItemStack(Material.AIR) : CraftItemStack.asBukkitCopy(nms);
    }

    @Override
    public void setBotItem(ITrainingBot bot, EquipmentSlotKind slot, @Nullable ItemStack stack) {
        ItemStack resolved = stack == null ? new ItemStack(Material.AIR) : stack;
        net.minecraft.server.v1_7_R4.ItemStack nms = CraftItemStack.asNMSCopy(resolved);
        EntityPlayer nativeBot = nativeBot(bot);
        String name = slot.name();
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
        nativeBot.inventory.items = new net.minecraft.server.v1_7_R4.ItemStack[36];
        nativeBot.inventory.armor = new net.minecraft.server.v1_7_R4.ItemStack[4];
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

    private static void syncMainHandAttackAttributes(EntityPlayer nativeBot) {
        net.minecraft.server.v1_7_R4.ItemStack held = nativeBot.inventory.getItemInHand();
        applyHeldItemAttributes(nativeBot, held, false);
        applyHeldItemAttributes(nativeBot, held, true);
    }

    private static void applyHeldItemAttributes(
            EntityPlayer nativeBot, net.minecraft.server.v1_7_R4.ItemStack stack, boolean add) {
        if (stack == null || stack.getItem() == null) {
            return;
        }
        if (add) {
            nativeBot.getAttributeMap().b(stack.D());
        } else {
            nativeBot.getAttributeMap().a(stack.D());
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
        if (bot instanceof TrainingBotHandle_v1_7_R4) {
            return ((TrainingBotHandle_v1_7_R4) bot).nativeEntity();
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

    private static List<Player> onlinePlayers() {
        List<Player> players = new ArrayList<Player>();
        for (Object raw : MinecraftServer.getServer().getPlayerList().players) {
            if (raw instanceof EntityPlayer) {
                players.add(((EntityPlayer) raw).getBukkitEntity());
            }
        }
        return players;
    }

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }
}
