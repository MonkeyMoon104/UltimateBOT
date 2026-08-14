package com.monkey.ultimatebot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.TrainingBotHandle_v1_16_R3;
import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_16_R3;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R3.VersionedTntMinecart;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.EntityEnderPearl;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityMinecartTNT;
import net.minecraft.server.v1_16_R3.EntityPose;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumInteractionResult;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.Explosion;
import net.minecraft.server.v1_16_R3.ItemActionContext;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.MovingObjectPositionBlock;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Real bot runtime for Spigot/Paper 1.16.5 ({@code v1_16_R3}). See package-info for supported surface
 * and gaps versus 1.17+.
 *
 * <p>Source stays Java 8-compatible for consistent legacy bytecode policy with {@code v1_8_R3}.
 */
public final class NMSBridge_v1_16_R3 implements INMSBridge {

    private static final Set<PlatformCapability> CAPABILITIES = PlatformCapability.through1_16();

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
        WorldServer world =
                ((CraftWorld) java.util.Objects.requireNonNull(spawn.getWorld(), "spawn world")).getHandle();
        PlayerInteractManager interactManager = new PlayerInteractManager(world);
        TrainingBot_v1_16_R3 entity =
                new TrainingBot_v1_16_R3(
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
        // UserCache has no public remove-by-UUID on this revision.
    }

    @Override
    public void moveBot(Player bot, double x, double y, double z) {
        EntityPlayer nativeBot = nativePlayer(bot);
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
        nativeBot.setHeadRotation(yaw);
        nativeBot.aC = yaw;
        byte packedYaw = packDegrees(yaw);
        byte packedPitch = packDegrees(pitch);
        PacketPlayOutEntityHeadRotation headPacket =
                new PacketPlayOutEntityHeadRotation(nativeBot, packedYaw);
        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        nativeBot.getId(), packedYaw, packedPitch, nativeBot.isOnGround());
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
        WorldServer world =
                ((CraftWorld) java.util.Objects.requireNonNull(location.getWorld(), "location world"))
                        .getHandle();
        EntityHuman nativeCause = cause == null ? null : nativePlayer(cause);
        world.createExplosion(
                nativeCause,
                location.getX(),
                location.getY(),
                location.getZ(),
                power,
                false,
                blockDamage ? Explosion.Effect.BREAK : Explosion.Effect.NONE);
    }

    @Override
    public void playSound(Location location, String soundKey, String source, float volume, float pitch) {
        // SoundEffect registry lookup is version-awkward; skip rather than crash on unknown keys.
    }

    @Override
    public void playSoundOnPlayer(Player player, String soundKey, float volume, float pitch) {}

    @Override
    public boolean useItemOnBlock(
            Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            EquipmentSlot hand) {
        EntityPlayer nativeBot = nativePlayer(bot);
        nativeBot.abilities.mayBuild = true;
        EnumHand enumHand = toHand(hand);
        net.minecraft.server.v1_16_R3.ItemStack requested = CraftItemStack.asNMSCopy(stack);
        if (requested.isEmpty()) {
            return false;
        }
        net.minecraft.server.v1_16_R3.ItemStack held =
                enumHand == EnumHand.OFF_HAND
                        ? nativeBot.getItemInOffHand()
                        : nativeBot.getItemInMainHand();
        // Melee often leaves a sword in-hand; CPvP still passes crystal/obsidian here.
        // Silent setSlot avoids ITEM_ARMOR_EQUIP_GENERIC spam.
        if (held.isEmpty() || held.getItem() != requested.getItem()) {
            nativeBot.setSlot(
                    enumHand == EnumHand.OFF_HAND ? EnumItemSlot.OFFHAND : EnumItemSlot.MAINHAND,
                    requested,
                    true);
            held =
                    enumHand == EnumHand.OFF_HAND
                            ? nativeBot.getItemInOffHand()
                            : nativeBot.getItemInMainHand();
        }
        if (held.isEmpty()) {
            return false;
        }
        BlockPosition pos = new BlockPosition(clicked.getX(), clicked.getY(), clicked.getZ());
        Vec3D hit = new Vec3D(hitLocation.getX(), hitLocation.getY(), hitLocation.getZ());
        EnumDirection direction = toNmsDirection(face);
        MovingObjectPositionBlock hitResult = new MovingObjectPositionBlock(hit, direction, pos, false);
        int previousCount = held.getCount();
        ItemActionContext context =
                new ItemActionContext(nativeBot.world, nativeBot, enumHand, held, hitResult);
        EnumInteractionResult result = held.placeItem(context, enumHand);
        if (result == EnumInteractionResult.SUCCESS || result == EnumInteractionResult.CONSUME) {
            restoreHeldCount(held, previousCount);
            nativeBot.swingHand(enumHand);
            return true;
        }
        result = held.getItem().a(context);
        if (result == EnumInteractionResult.SUCCESS || result == EnumInteractionResult.CONSUME) {
            restoreHeldCount(held, previousCount);
            nativeBot.swingHand(enumHand);
            return true;
        }
        return false;
    }

    private static void restoreHeldCount(net.minecraft.server.v1_16_R3.ItemStack nmsStack, int previousCount) {
        if (!nmsStack.isEmpty() && nmsStack.getCount() < previousCount) {
            nmsStack.setCount(previousCount);
        }
    }

    @Override
    public void throwEnderpearl(Player bot, Vector targetPos) {
        EntityPlayer nativeBot = nativePlayer(bot);
        EntityEnderPearl pearl = new EntityEnderPearl(nativeBot.world, nativeBot);
        double eyeY = nativeBot.locY() + nativeBot.getHeadHeight();
        double dx = targetPos.getX() - nativeBot.locX();
        double dy = targetPos.getY() - eyeY;
        double dz = targetPos.getZ() - nativeBot.locZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double scale =
                distance < 15 ? Math.min(distance * 0.08, 1.2) : Math.min(distance * 0.06, 1.8);
        pearl.setMot(new Vec3D(dx, dy + 0.2 + distance * 0.02, dz).d().a(scale));
        pearl.setPosition(nativeBot.locX(), eyeY, nativeBot.locZ());
        nativeBot.world.addEntity(pearl);
        nativeBot.swingHand(EnumHand.MAIN_HAND);
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
                        nativeBot.getId(), yaw, pitch, nativeBot.isOnGround()));
        handle.playerConnection.sendPacket(
                new PacketPlayOutEntityMetadata(nativeBot.getId(), nativeBot.getDataWatcher(), true));
    }

    @Override
    public void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment) {
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> converted =
                toNmsEquipment(equipment);
        if (converted.isEmpty()) {
            return;
        }
        ((CraftPlayer) viewer)
                .getHandle()
                .playerConnection
                .sendPacket(new PacketPlayOutEntityEquipment(nativeBot(bot).getId(), converted));
    }

    @Override
    public void broadcastEquipment(ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment) {
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> converted =
                toNmsEquipment(equipment);
        if (converted.isEmpty()) {
            return;
        }
        PacketPlayOutEntityEquipment packet =
                new PacketPlayOutEntityEquipment(nativeBot(bot).getId(), converted);
        for (Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
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
    public void setBotSwimming(ITrainingBot bot, boolean swimming) {
        EntityPlayer nativeBot = nativeBot(bot);
        nativeBot.setSprinting(swimming);
        // Flag 4 = swimming. Vanilla pose update runs in super.tick() before AI, so force pose here
        // and push metadata — packet-spawned bots are not tracker-synced for DataWatcher changes.
        nativeBot.setSwimming(swimming);
        if (swimming) {
            nativeBot.inWater = true;
            nativeBot.setPose(EntityPose.SWIMMING);
        } else {
            nativeBot.inWater = false;
            if (nativeBot.getPose() == EntityPose.SWIMMING) {
                nativeBot.setPose(EntityPose.STANDING);
            }
        }
        broadcastMetadata(bot);
    }

    @Override
    public ItemStack getBotItem(ITrainingBot bot, EquipmentSlot slot) {
        EnumItemSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return new ItemStack(Material.AIR);
        }
        return CraftItemStack.asBukkitCopy(nativeBot(bot).getEquipment(nmsSlot));
    }

    @Override
    public void setBotItem(ITrainingBot bot, EquipmentSlot slot, @Nullable ItemStack stack) {
        EnumItemSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return;
        }
        ItemStack resolved = stack == null ? new ItemStack(Material.AIR) : stack;
        // Hands stay silent: EntityHuman.setSlot otherwise plays ITEM_ARMOR_EQUIP_GENERIC on every
        // crystal/obsidian refill (CPvP spam). Armor must not be silent or attribute modifiers
        // (protection / toughness) never apply and the bot takes unarmored sword damage.
        boolean silent = nmsSlot == EnumItemSlot.MAINHAND || nmsSlot == EnumItemSlot.OFFHAND;
        nativeBot(bot).setSlot(nmsSlot, CraftItemStack.asNMSCopy(resolved), silent);
    }

    @Override
    public void clearBotInventory(ITrainingBot bot) {
        nativeBot(bot).inventory.clear();
    }

    @Override
    public void beginUsingBotItem(ITrainingBot bot, EquipmentSlot hand) {
        nativeBot(bot).c(toHand(hand));
    }

    @Override
    public void stopUsingBotItem(ITrainingBot bot) {
        nativeBot(bot).clearActiveItem();
    }

    @Override
    public boolean isBotOnGround(ITrainingBot bot) {
        return nativeBot(bot).isOnGround();
    }

    @Override
    public void setBotOnGround(ITrainingBot bot, boolean onGround) {
        nativeBot(bot).setOnGround(onGround);
    }

    @Override
    public Vector getBotVelocity(ITrainingBot bot) {
        Vec3D mot = nativeBot(bot).getMot();
        return new Vector(mot.x, mot.y, mot.z);
    }

    @Override
    public void setBotVelocity(ITrainingBot bot, Vector velocity) {
        EntityPlayer nativeBot = nativeBot(bot);
        nativeBot.setMot(velocity.getX(), velocity.getY(), velocity.getZ());
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
        nativeBot(bot).attack(((CraftLivingEntity) target).getHandle());
    }

    @Override
    public Entity spawnExplosiveMinecart(Location location) {
        WorldServer world =
                ((CraftWorld) java.util.Objects.requireNonNull(location.getWorld(), "world")).getHandle();
        VersionedTntMinecart cart =
                new VersionedTntMinecart(world, location.getX(), location.getY(), location.getZ());
        cart.dead = false;
        // addEntity0 after VehicleCreateEvent: Chunk.a then registerEntity (addEntityChunk).
        // During world entity-tick, registerEntity only queues and leaves valid=false, so
        // CraftEntity.isValid fails and core discards the cart. valid is a public field.
        int chunkX = MathHelper.floor(cart.locX() / 16.0D);
        int chunkZ = MathHelper.floor(cart.locZ() / 16.0D);
        world.getChunkAt(chunkX, chunkZ).a(cart);
        world.addEntityChunk(cart);
        cart.valid = true;
        return cart.getBukkitEntity();
    }

    @Override
    public void setExplosiveMinecartFuseTicks(Entity minecart, int ticks) {
        if (minecart == null) {
            return;
        }
        net.minecraft.server.v1_16_R3.Entity handle = ((CraftEntity) minecart).getHandle();
        if (!(handle instanceof EntityMinecartTNT)) {
            return;
        }
        if (ticks < 0) {
            // Ctor already leaves fuse at -1 (unprimed). No public unprime setter on this mapping.
            return;
        }
        EntityMinecartTNT tnt = (EntityMinecartTNT) handle;
        if (tnt instanceof VersionedTntMinecart) {
            ((VersionedTntMinecart) tnt).explodeNow();
        } else {
            tnt.world.explode(
                    tnt, tnt.locX(), tnt.locY(), tnt.locZ(), 4.0F, Explosion.Effect.BREAK);
            tnt.die();
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
        if (bot instanceof TrainingBotHandle_v1_16_R3) {
            return ((TrainingBotHandle_v1_16_R3) bot).nativeEntity();
        }
        if (bot instanceof EntityPlayer) {
            return (EntityPlayer) bot;
        }
        return nativePlayer(bot.asBukkitPlayer());
    }

    private static EntityPlayer nativePlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private static EnumHand toHand(EquipmentSlot hand) {
        return "OFF_HAND".equals(hand.name()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }

    private static EnumDirection toNmsDirection(BlockFace face) {
        switch (face) {
            case DOWN:
                return EnumDirection.DOWN;
            case UP:
                return EnumDirection.UP;
            case NORTH:
                return EnumDirection.NORTH;
            case SOUTH:
                return EnumDirection.SOUTH;
            case WEST:
                return EnumDirection.WEST;
            case EAST:
                return EnumDirection.EAST;
            default:
                return EnumDirection.UP;
        }
    }

    private static @Nullable EnumItemSlot toNmsSlot(EquipmentSlot slot) {
        String name = slot.name();
        if ("HAND".equals(name)) {
            return EnumItemSlot.MAINHAND;
        }
        if ("OFF_HAND".equals(name)) {
            return EnumItemSlot.OFFHAND;
        }
        if ("FEET".equals(name)) {
            return EnumItemSlot.FEET;
        }
        if ("LEGS".equals(name)) {
            return EnumItemSlot.LEGS;
        }
        if ("CHEST".equals(name)) {
            return EnumItemSlot.CHEST;
        }
        if ("HEAD".equals(name)) {
            return EnumItemSlot.HEAD;
        }
        return null;
    }

    private static List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> toNmsEquipment(
            Map<EquipmentSlot, ItemStack> equipment) {
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> converted =
                new ArrayList<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>>();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            EnumItemSlot slot = toNmsSlot(entry.getKey());
            if (slot != null) {
                converted.add(Pair.of(slot, CraftItemStack.asNMSCopy(entry.getValue())));
            }
        }
        return converted;
    }

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }
}
