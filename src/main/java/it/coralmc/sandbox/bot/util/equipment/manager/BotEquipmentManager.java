package it.coralmc.sandbox.bot.util.equipment.manager;

import com.mojang.datafixers.util.Pair;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.bot.util.equipment.converter.EquipmentConverter;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BotEquipmentManager {

	private final BotEntityFinder botEntityFinder;
	private final BotRegistry botRegistry;

	public BotEquipmentManager(BotRegistry botRegistry) {
		this.botRegistry = botRegistry;
		this.botEntityFinder = new BotEntityFinder(botRegistry);
	}

	public void applyEquipment(LivingEntity bot, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
		Map<org.bukkit.inventory.EquipmentSlot, Boolean> emptyBlastProtection = new HashMap<>();
		applyEquipment(bot, armorMap, emptyBlastProtection);
	}

	public void applyEquipment(LivingEntity bot, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
							   Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
		for (var entry : armorMap.entrySet()) {
			EquipmentSlot slot = EquipmentConverter.toNMSSlot(entry.getKey());
			if (slot != null) {
				org.bukkit.inventory.ItemStack bukkitItem = entry.getValue().clone();

				boolean hasBlastProtection = blastProtectionMap.getOrDefault(entry.getKey(), false);
				applyArmorEnchants(bukkitItem, hasBlastProtection);

				ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
				bot.setItemSlot(slot, nmsItem);
			}
		}
	}

	private void applyArmorEnchants(org.bukkit.inventory.ItemStack item, boolean hasBlastProtection) {
		if (item == null || item.getType() == Material.AIR) return;

		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.removeEnchant(Enchantment.BLAST_PROTECTION);

			if (hasBlastProtection) {
				meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
			}

			item.setItemMeta(meta);
		}
	}

	public void broadcastEquipment(LivingEntity bot, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
								   Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
		List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();

		for (var entry : armorMap.entrySet()) {
			EquipmentSlot nmsSlot = EquipmentConverter.toNMSSlot(entry.getKey());
			if (nmsSlot != null) {
				org.bukkit.inventory.ItemStack bukkitItem = entry.getValue().clone();

				boolean hasBlastProtection = blastProtectionMap.getOrDefault(entry.getKey(), false);
				applyArmorEnchants(bukkitItem, hasBlastProtection);

				ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
				equipmentList.add(Pair.of(nmsSlot, nmsItem));
			}
		}

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

	public Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> getBotArmor(UUID playerUUID, BotRegistry botRegistry) {
		UUID botUUID = botRegistry.getBotUUID(playerUUID);
		if (botUUID == null) return null;

		ServerLevel world = botEntityFinder.getPlayerWorld(playerUUID);
		if (world == null) return null;

		LivingEntity botEntity = botEntityFinder.findBotAsLivingEntity(world, botUUID);
		if (botEntity == null) return null;

		Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armor = new EnumMap<>(org.bukkit.inventory.EquipmentSlot.class);

		for (org.bukkit.inventory.EquipmentSlot slot : EquipmentConverter.getArmorSlots()) {
			EquipmentSlot nmsSlot = EquipmentConverter.toNMSSlot(slot);
			if (nmsSlot != null) {
				ItemStack nmsItem = botEntity.getItemBySlot(nmsSlot);
				Material material = EquipmentConverter.toMaterial(nmsItem);

				if (material == null) {
					material = Material.AIR;
				}

				EntityEquipment equipment = botEntity.getBukkitLivingEntity().getEquipment();
				if (equipment == null) continue;

				armor.put(slot, equipment.getItem(slot));
			}
		}

		return armor;
	}

	public boolean updateBotArmor(UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap, BotRegistry botRegistry) {
		Map<org.bukkit.inventory.EquipmentSlot, Boolean> emptyBlastProtection = new HashMap<>();
		return updateBotArmor(ownerUUID, armorMap, emptyBlastProtection, botRegistry);
	}

	public boolean updateBotArmor(UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
								  Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap, BotRegistry botRegistry) {
		UUID botUUID = botRegistry.getBotUUID(ownerUUID);
		if (botUUID == null) return false;

		ServerLevel world = botEntityFinder.getPlayerWorld(ownerUUID);
		if (world == null) return false;

		LivingEntity botEntity = botEntityFinder.findBotAsLivingEntity(world, botUUID);
		if (botEntity == null) return false;

		applyEquipment(botEntity, armorMap, blastProtectionMap);
		broadcastEquipment(botEntity, armorMap, blastProtectionMap);

		return true;
	}

	public void fillInventory(TrainingBot bot, ItemStack itemStack) {
		for (int i = 0; i < bot.getInventory().items.size(); i++) {
			ItemStack current = bot.getInventory().items.get(i);
			if (current == null || current.isEmpty()) {
				bot.getInventory().items.set(i, itemStack.copy());
			}
		}
	}
}