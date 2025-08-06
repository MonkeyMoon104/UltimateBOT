package it.coralmc.sandbox.bot.util.equipment.manager;

import com.mojang.datafixers.util.Pair;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.equipment.converter.EquipmentConverter;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class BotEquipmentManager {

    public static void applyEquipment(LivingEntity bot, Map<org.bukkit.inventory.EquipmentSlot, Material> armorMap) {
        for (var entry : armorMap.entrySet()) {
            EquipmentSlot slot = EquipmentConverter.toNMSSlot(entry.getKey());
            if (slot != null) {
                ItemStack nmsItem = EquipmentConverter.toNMSItemStack(entry.getValue());
                bot.setItemSlot(slot, nmsItem);
            }
        }
    }

    public static void broadcastEquipment(LivingEntity bot, Map<org.bukkit.inventory.EquipmentSlot, Material> armorMap) {
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = EquipmentConverter.toNMSEquipmentList(armorMap);

        if (!equipmentList.isEmpty()) {
            ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(
                    bot.getId(), equipmentList
            );

            for (Player online : Bukkit.getOnlinePlayers()) {
                ServerPlayer handle = ((CraftPlayer) online).getHandle();
                handle.connection.send(equipmentPacket);
            }
        }
    }

    public static Map<org.bukkit.inventory.EquipmentSlot, Material> getBotArmor(UUID playerUUID) {
        UUID botUUID = BotRegistry.getBotUUID(playerUUID);
        if (botUUID == null) return null;

        ServerLevel world = BotEntityFinder.getPlayerWorld(playerUUID);
        if (world == null) return null;

        LivingEntity botEntity = BotEntityFinder.findBotAsLivingEntity(world, botUUID);
        if (botEntity == null) return null;

        Map<org.bukkit.inventory.EquipmentSlot, Material> armor = new EnumMap<>(org.bukkit.inventory.EquipmentSlot.class);

        for (org.bukkit.inventory.EquipmentSlot slot : EquipmentConverter.getArmorSlots()) {
            EquipmentSlot nmsSlot = EquipmentConverter.toNMSSlot(slot);
            if (nmsSlot != null) {
                ItemStack nmsItem = botEntity.getItemBySlot(nmsSlot);
                Material material = EquipmentConverter.toMaterial(nmsItem);

                if (material == null) {
                    material = Material.AIR;
                }

                armor.put(slot, material);
            }
        }

        return armor;
    }

    public static boolean updateBotArmor(UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, Material> armorMap) {
        UUID botUUID = BotRegistry.getBotUUID(ownerUUID);
        if (botUUID == null) return false;

        ServerLevel world = BotEntityFinder.getPlayerWorld(ownerUUID);
        if (world == null) return false;

        LivingEntity botEntity = BotEntityFinder.findBotAsLivingEntity(world, botUUID);
        if (botEntity == null) return false;

        applyEquipment(botEntity, armorMap);
        broadcastEquipment(botEntity, armorMap);

        return true;
    }

    public static void fillInventory(TrainingBot bot, ItemStack itemStack) {
        for (int i = 0; i < bot.getInventory().items.size(); i++) {
            ItemStack current = bot.getInventory().items.get(i);
            if (current == null || current.isEmpty()) {
                bot.getInventory().items.set(i, itemStack.copy());
            }
        }
    }
}