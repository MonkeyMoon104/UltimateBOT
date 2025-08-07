package it.coralmc.sandbox.bot.util.equipment.converter;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipmentConverter {

	public static EquipmentSlot toNMSSlot(org.bukkit.inventory.EquipmentSlot bukkitSlot) {
		return switch (bukkitSlot) {
			case HEAD -> EquipmentSlot.HEAD;
			case CHEST -> EquipmentSlot.CHEST;
			case LEGS -> EquipmentSlot.LEGS;
			case FEET -> EquipmentSlot.FEET;
			case HAND -> EquipmentSlot.MAINHAND;
			case OFF_HAND -> EquipmentSlot.OFFHAND;
			case BODY -> null;
		};
	}

	public static org.bukkit.inventory.EquipmentSlot toBukkitSlot(EquipmentSlot nmsSlot) {
		return switch (nmsSlot) {
			case HEAD -> org.bukkit.inventory.EquipmentSlot.HEAD;
			case CHEST -> org.bukkit.inventory.EquipmentSlot.CHEST;
			case LEGS -> org.bukkit.inventory.EquipmentSlot.LEGS;
			case FEET -> org.bukkit.inventory.EquipmentSlot.FEET;
			case MAINHAND -> org.bukkit.inventory.EquipmentSlot.HAND;
			case OFFHAND -> org.bukkit.inventory.EquipmentSlot.OFF_HAND;
			case BODY -> null;
		};
	}

	public static List<Pair<EquipmentSlot, ItemStack>> toNMSEquipmentList(
		Map<org.bukkit.inventory.EquipmentSlot, Material> armorMap) {

		List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();

		for (var entry : armorMap.entrySet()) {
			EquipmentSlot slot = toNMSSlot(entry.getKey());
			if (slot != null) {
				ItemStack nmsItem = CraftItemStack.asNMSCopy(
					new org.bukkit.inventory.ItemStack(entry.getValue()));
				equipmentList.add(Pair.of(slot, nmsItem));
			}
		}

		return equipmentList;
	}

	public static Material toMaterial(ItemStack nmsItem) {
		if (nmsItem == null || nmsItem.isEmpty()) {
			return Material.AIR;
		}
		org.bukkit.inventory.ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
		return bukkitItem.getType();
	}

	public static ItemStack toNMSItemStack(Material material) {
		return CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material));
	}

	public static org.bukkit.inventory.EquipmentSlot[] getArmorSlots() {
		return new org.bukkit.inventory.EquipmentSlot[]{
			org.bukkit.inventory.EquipmentSlot.HEAD,
			org.bukkit.inventory.EquipmentSlot.CHEST,
			org.bukkit.inventory.EquipmentSlot.LEGS,
			org.bukkit.inventory.EquipmentSlot.FEET
		};
	}
}