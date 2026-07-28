package com.monkey.mcbot.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * PacketEvents-backed client rendering. The final metadata sync remains native
 * because it copies the entity's version-specific data watchers verbatim.
 */
public final class PacketEventsBotPacketGateway implements BotPacketGateway {

    private static final PacketEventsBotPacketGateway INSTANCE = new PacketEventsBotPacketGateway();

    private PacketEventsBotPacketGateway() {
    }

    public static PacketEventsBotPacketGateway get() {
        return INSTANCE;
    }

    @Override
    public void show(Player viewer, ITrainingBot bot) {
        GameProfile profile = bot.asPlayer().getGameProfile();
        UserProfile userProfile = toUserProfile(profile);
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo =
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        userProfile, true, 0, GameMode.SURVIVAL,
                        Component.text(profile.getName()), null
                );

        send(viewer, new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER),
                playerInfo
        ));

        net.minecraft.world.entity.player.Player entity = bot.asPlayer();
        send(viewer, new WrapperPlayServerSpawnPlayer(
                entity.getId(),
                entity.getUUID(),
                new Vector3d(entity.getX(), entity.getY(), entity.getZ()),
                entity.getYRot(),
                entity.getXRot(),
                List.of()
        ));
        send(viewer, new WrapperPlayServerEntityHeadLook(entity.getId(), entity.getYHeadRot()));
        send(viewer, new WrapperPlayServerEntityRotation(
                entity.getId(), entity.getYRot(), entity.getXRot(), entity.onGround()
        ));
        sendNativeMetadata(viewer, entity);
        sendCurrentEquipment(viewer, entity);
    }

    @Override
    public void sendEquipment(Player viewer, int entityId, List<BotEquipment> equipment) {
        if (equipment.isEmpty()) {
            return;
        }

        List<Equipment> converted = equipment.stream()
                .map(entry -> new Equipment(
                        toPacketSlot(entry.slot()),
                        SpigotConversionUtil.fromBukkitItemStack(entry.item())
                ))
                .toList();
        send(viewer, new WrapperPlayServerEntityEquipment(entityId, converted));
    }

    @Override
    public void sendCurrentEquipment(Player viewer, LivingEntity bot) {
        List<BotEquipment> equipment = new ArrayList<>();
        equipment.add(new BotEquipment(
                EquipmentSlot.HAND,
                CraftItemStack.asBukkitCopy(bot.getMainHandItem())
        ));
        equipment.add(new BotEquipment(
                EquipmentSlot.OFF_HAND,
                CraftItemStack.asBukkitCopy(bot.getOffhandItem())
        ));
        equipment.add(new BotEquipment(
                EquipmentSlot.FEET,
                CraftItemStack.asBukkitCopy(bot.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET))
        ));
        equipment.add(new BotEquipment(
                EquipmentSlot.LEGS,
                CraftItemStack.asBukkitCopy(bot.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS))
        ));
        equipment.add(new BotEquipment(
                EquipmentSlot.CHEST,
                CraftItemStack.asBukkitCopy(bot.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST))
        ));
        equipment.add(new BotEquipment(
                EquipmentSlot.HEAD,
                CraftItemStack.asBukkitCopy(bot.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))
        ));
        sendEquipment(viewer, bot.getId(), equipment);
    }

    private static UserProfile toUserProfile(GameProfile profile) {
        List<TextureProperty> textures = profile.getProperties().get("textures").stream()
                .map(PacketEventsBotPacketGateway::toTextureProperty)
                .toList();
        return new UserProfile(profile.getId(), profile.getName(), textures);
    }

    private static TextureProperty toTextureProperty(Property property) {
        return new TextureProperty(property.name(), property.value(), property.signature());
    }

    private static com.github.retrooper.packetevents.protocol.player.EquipmentSlot toPacketSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.OFF_HAND;
            case FEET -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.BOOTS;
            case LEGS -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.LEGGINGS;
            case CHEST -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.CHEST_PLATE;
            case HEAD -> com.github.retrooper.packetevents.protocol.player.EquipmentSlot.HELMET;
            default -> throw new IllegalArgumentException("Unsupported equipment slot: " + slot);
        };
    }

    private static void send(Player viewer, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    private static void sendNativeMetadata(Player viewer, net.minecraft.world.entity.player.Player bot) {
        List<SynchedEntityData.DataValue<?>> metadata = bot.getEntityData().getNonDefaultValues();
        if (metadata == null || metadata.isEmpty()) {
            return;
        }

        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        handle.connection.send(new ClientboundSetEntityDataPacket(bot.getId(), metadata));
    }
}
