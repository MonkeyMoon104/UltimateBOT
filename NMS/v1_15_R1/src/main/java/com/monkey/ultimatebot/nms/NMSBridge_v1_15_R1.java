package com.monkey.ultimatebot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.TrainingBotHandle_v1_15_R1;
import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_15_R1;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_15_R1.VersionedItemActionContext;
import com.monkey.ultimatebot.bot.ai.fakeplayer.v1_15_R1.VersionedTntMinecart;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.EntityEnderCrystal;
import net.minecraft.server.v1_15_R1.EntityEnderPearl;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityMinecartTNT;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumDirection;
import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.EnumInteractionResult;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.Explosion;
import net.minecraft.server.v1_15_R1.ItemActionContext;
import net.minecraft.server.v1_15_R1.Items;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_15_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Real bot runtime for Spigot 1.15.2 ({@code v1_15_R1}).
 *
 * <p>Source stays Java 8-compatible so the module can load on Java 8 JVMs hosting this revision.
 */
public final class NMSBridge_v1_15_R1 implements INMSBridge {

    private static final Set<PlatformCapability> CAPABILITIES = PlatformCapability.through1_13();

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
        TrainingBot_v1_15_R1 entity =
                new TrainingBot_v1_15_R1(
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
        ((WorldServer) nativeBot.getWorld()).addEntity(nativeBot);
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
        nativeBot.setMot(0.0D, 0.0D, 0.0D);
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
        nativeBot.setHeadRotation(yaw);
        nativeBot.aC = yaw;
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
        // SoundEffect registry lookup is version-awkward; skip rather than crash.
    }

    @Override
    public void playSoundOnPlayer(Player player, String soundKey, float volume, float pitch) {

    }

    @Override
    public boolean useItemOnBlock(
            Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            EquipmentSlotKind hand) {
        EntityPlayer nativeBot = nativePlayer(bot);
        nativeBot.abilities.mayBuild = true;
        EnumHand enumHand = toHand(hand);
        net.minecraft.server.v1_15_R1.ItemStack requested = CraftItemStack.asNMSCopy(stack);
        if (requested.isEmpty()) {
            return false;
        }
        net.minecraft.server.v1_15_R1.ItemStack held =
                enumHand == EnumHand.OFF_HAND
                        ? nativeBot.getItemInOffHand()
                        : nativeBot.getItemInMainHand();
        // Melee often leaves a sword in-hand; CPvP still passes crystal/obsidian here.
        if (held.isEmpty() || held.getItem() != requested.getItem()) {
            setHeldSilently(nativeBot, enumHand, requested);
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
                new VersionedItemActionContext(nativeBot.world, nativeBot, enumHand, held, hitResult);
        // Prefer raw Item.useOn like 1.17+: ItemStack.placeItem wraps BlockPlaceEvent, which
        // protection plugins cancel for fake players.
        EnumInteractionResult result = held.getItem().a(context);
        if (isAccepted(result)) {
            restoreHeldCount(held, previousCount);
            nativeBot.a(enumHand);
            return true;
        }
        result = held.placeItem(context, enumHand);
        if (isAccepted(result)) {
            restoreHeldCount(held, previousCount);
            nativeBot.a(enumHand);
            return true;
        }
        WorldServer world = (WorldServer) nativeBot.world;
        if (held.getItem() == Items.END_CRYSTAL && placeEndCrystalDirect(world, nativeBot, pos)) {
            restoreHeldCount(held, previousCount);
            nativeBot.a(enumHand);
            return true;
        }
        if (stack.getType() == Material.OBSIDIAN && placeObsidianDirect(world, pos, direction)) {
            restoreHeldCount(held, previousCount);
            nativeBot.a(enumHand);
            return true;
        }
        return false;
    }

    private static boolean isAccepted(EnumInteractionResult result) {
        return result == EnumInteractionResult.SUCCESS || result == EnumInteractionResult.CONSUME;
    }

    private static boolean placeEndCrystalDirect(
            WorldServer world, EntityPlayer placer, BlockPosition clicked) {
        net.minecraft.server.v1_15_R1.IBlockData data = world.getType(clicked);
        if (data.getBlock() != Blocks.OBSIDIAN && data.getBlock() != Blocks.BEDROCK) {
            return false;
        }
        BlockPosition above = clicked.up();
        if (!world.getType(above).isAir()) {
            return false;
        }
        double x = above.getX();
        double y = above.getY();
        double z = above.getZ();
        // Ignore players/bots standing on the pillar — they blocked vanilla ItemEndCrystal.
        if (!world.getEntities(
                        placer,
                        new AxisAlignedBB(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D),
                        entity -> !(entity instanceof EntityHuman))
                .isEmpty()) {
            return false;
        }
        EntityEnderCrystal crystal = new EntityEnderCrystal(world, x + 0.5D, y, z + 0.5D);
        crystal.setShowingBottom(false);
        return world.addEntity(crystal, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    private static boolean placeObsidianDirect(WorldServer world, BlockPosition clicked, EnumDirection face) {
        BlockPosition placeAt = clicked.shift(face);
        if (!world.getType(placeAt).isAir()) {
            return false;
        }
        return world.setTypeAndData(placeAt, Blocks.OBSIDIAN.getBlockData(), 3);
    }

    private static void setHeldSilently(
            EntityPlayer nativeBot, EnumHand hand, net.minecraft.server.v1_15_R1.ItemStack stack) {
        EnumItemSlot slot = hand == EnumHand.OFF_HAND ? EnumItemSlot.OFFHAND : EnumItemSlot.MAINHAND;
        nativeBot.getAttributeMap().a(nativeBot.getEquipment(slot).a(slot));
        if (hand == EnumHand.OFF_HAND) {
            nativeBot.inventory.extraSlots.set(0, stack);
        } else {
            nativeBot.inventory.items.set(nativeBot.inventory.itemInHandIndex, stack);
        }
        nativeBot.getAttributeMap().b(stack.a(slot));
    }

    private static void restoreHeldCount(net.minecraft.server.v1_15_R1.ItemStack nmsStack, int previousCount) {
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
        handle.playerConnection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(nativeBot.getId(), yaw, pitch, nativeBot.onGround));
        handle.playerConnection.sendPacket(
                new PacketPlayOutEntityMetadata(nativeBot.getId(), nativeBot.getDataWatcher(), true));
    }

    @Override
    public void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlotKind, ItemStack> equipment) {
        EntityPlayer handle = ((CraftPlayer) viewer).getHandle();
        int entityId = nativeBot(bot).getId();
        for (Map.Entry<EquipmentSlotKind, ItemStack> entry : equipment.entrySet()) {
            EnumItemSlot slot = toNmsSlot(entry.getKey());
            if (slot == null) {
                continue;
            }
            handle.playerConnection.sendPacket(
                    new PacketPlayOutEntityEquipment(
                            entityId, slot, CraftItemStack.asNMSCopy(entry.getValue())));
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
    public void setBotSwimming(ITrainingBot bot, boolean swimming) {
        EntityPlayer nativeBot = nativeBot(bot);
        if (nativeBot instanceof TrainingBot_v1_15_R1) {
            ((TrainingBot_v1_15_R1) nativeBot).applySwimState(swimming);
        } else {
            nativeBot.setSprinting(swimming);
            nativeBot.setSwimming(swimming);
            nativeBot.inWater = swimming;
        }
        broadcastMetadata(bot);
    }

    @Override
    public ItemStack getBotItem(ITrainingBot bot, EquipmentSlotKind slot) {
        EnumItemSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return new ItemStack(Material.AIR);
        }
        return CraftItemStack.asBukkitCopy(nativeBot(bot).getEquipment(nmsSlot));
    }

    @Override
    public void setBotItem(ITrainingBot bot, EquipmentSlotKind slot, @Nullable ItemStack stack) {
        EnumItemSlot nmsSlot = toNmsSlot(slot);
        if (nmsSlot == null) {
            return;
        }
        ItemStack resolved = stack == null ? new ItemStack(Material.AIR) : stack;
        net.minecraft.server.v1_15_R1.ItemStack nms = CraftItemStack.asNMSCopy(resolved);
        // Hands: write inventory lists (no setSlot). setSlot plays ITEM_ARMOR_EQUIP_GENERIC.
        if (nmsSlot == EnumItemSlot.MAINHAND || nmsSlot == EnumItemSlot.OFFHAND) {
            setHeldSilently(
                    nativeBot(bot),
                    nmsSlot == EnumItemSlot.OFFHAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND,
                    nms);
            return;
        }
        nativeBot(bot).setSlot(nmsSlot, nms);
    }

    @Override
    public void clearBotInventory(ITrainingBot bot) {
        nativeBot(bot).inventory.clear();
    }

    @Override
    public void beginUsingBotItem(ITrainingBot bot, EquipmentSlotKind hand) {
        nativeBot(bot).c(toHand(hand));
    }

    @Override
    public void stopUsingBotItem(ITrainingBot bot) {
        nativeBot(bot).clearActiveItem();
    }

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
        Vec3D mot = nativeBot(bot).getMot();
        return new Vector(
                Double.isFinite(mot.x) ? mot.x : 0.0D,
                Double.isFinite(mot.y) ? mot.y : 0.0D,
                Double.isFinite(mot.z) ? mot.z : 0.0D);
    }

    @Override
    public void setBotVelocity(ITrainingBot bot, Vector velocity) {
        EntityPlayer nativeBot = nativeBot(bot);
        double x = velocity.getX();
        double y = velocity.getY();
        double z = velocity.getZ();
        nativeBot.setMot(
                Double.isFinite(x) ? x : 0.0D, Double.isFinite(y) ? y : 0.0D, Double.isFinite(z) ? z : 0.0D);
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

    /**
     * {@code EntityHuman.attack} uses {@code GenericAttributes.ATTACK_DAMAGE}, not the hotbar list.
     * Silent hand writes skip AttributeMap updates, so re-apply the held item's modifiers here
     * (remove+add of the current item is a no-op when living-tick already applied them).
     */
    private static void applyHandItemAttributes(EntityPlayer nativeBot, EnumItemSlot slot) {
        net.minecraft.server.v1_15_R1.ItemStack held = nativeBot.getEquipment(slot);
        if (held == null) {
            return;
        }
        nativeBot.getAttributeMap().a(held.a(slot));
        nativeBot.getAttributeMap().b(held.a(slot));
    }

    @Override
    public void attackTarget(ITrainingBot bot, LivingEntity target) {
        EntityPlayer nativeBot = nativeBot(bot);
        applyHandItemAttributes(nativeBot, EnumItemSlot.MAINHAND);
        nativeBot.attack(((CraftLivingEntity) target).getHandle());
    }

    @Override
    public Entity spawnExplosiveMinecart(Location location) {
        WorldServer world =
                ((CraftWorld) java.util.Objects.requireNonNull(location.getWorld(), "world")).getHandle();
        VersionedTntMinecart cart =
                new VersionedTntMinecart(world, location.getX(), location.getY(), location.getZ());
        cart.dead = false;
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
        net.minecraft.server.v1_15_R1.Entity handle = ((CraftEntity) minecart).getHandle();
        if (!(handle instanceof EntityMinecartTNT)) {
            return;
        }
        if (ticks < 0) {
            return;
        }
        EntityMinecartTNT tnt = (EntityMinecartTNT) handle;
        if (tnt instanceof VersionedTntMinecart) {
            ((VersionedTntMinecart) tnt).explodeNow();
        } else {
            tnt.world.explode(tnt, tnt.locX(), tnt.locY(), tnt.locZ(), 4.0F, Explosion.Effect.BREAK);
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
        if (bot instanceof TrainingBotHandle_v1_15_R1) {
            return ((TrainingBotHandle_v1_15_R1) bot).nativeEntity();
        }
        if (bot instanceof EntityPlayer) {
            return (EntityPlayer) bot;
        }
        return nativePlayer(bot.asBukkitPlayer());
    }

    private static EntityPlayer nativePlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private static EnumHand toHand(EquipmentSlotKind hand) {
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

    private static @Nullable EnumItemSlot toNmsSlot(EquipmentSlotKind slot) {
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

    private static byte packDegrees(float value) {
        return (byte) (value * 256.0F / 360.0F);
    }
}
