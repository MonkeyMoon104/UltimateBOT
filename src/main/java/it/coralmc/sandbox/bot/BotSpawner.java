package it.coralmc.sandbox.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.bot.util.equipment.manager.BotEquipmentManager;
import it.coralmc.sandbox.bot.util.packets.Packet;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class BotSpawner {

	public static void spawnFakeBot(Player viewer, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap, boolean follow, int totem) {
		ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
		ServerLevel world = handle.serverLevel().getLevel();

		UUID botUUID = UUID.randomUUID();
		FileConfiguration config = SandboxTraining.getInstance().getConfig();
		String botName = config.getString("bot.name", "CrystalBot");
		GameProfile playerProfile = ((CraftPlayer) viewer).getProfile();
		Collection<Property> properties = playerProfile.getProperties().get("textures");

		Property property = null;

		if (!properties.isEmpty()) {
			property = properties.iterator().next();
		}

		GameProfile profile = new GameProfile(botUUID, botName);
		if (property != null) profile.getProperties().put("textures", property);
		ClientInformation clientInfo = createClientInformation();

		Location loc = viewer.getLocation();
		TrainingBot bot = new TrainingBot(
			world,
			BlockPos.containing(loc.getX(), loc.getY(), loc.getZ()),
			0,
			profile,
			viewer,
			follow
		);

		bot.setTotemCount(totem);

		world.addFreshEntity(bot);
		setupBotInventory(bot, totem);
		BotEquipmentManager.applyEquipment(bot, armorMap);

		broadcastBotToPlayers(bot, armorMap);
		BotRegistry.registerBot(viewer.getUniqueId(), bot);
	}

	public static boolean isBotSpawned(UUID playerUUID) {
		return BotRegistry.isBotSpawned(playerUUID);
	}

	public static void despawnBot(Player owner) {
		UUID ownerUUID = owner.getUniqueId();
		UUID botUUID = BotRegistry.getBotUUID(ownerUUID);
		if (botUUID == null) return;

		ServerPlayer handle = ((CraftPlayer) owner).getHandle();
		ServerLevel world = handle.serverLevel();

		if (BotEntityFinder.removeEntity(world, botUUID)) {
			BotRegistry.removeBot(ownerUUID);
		}
	}

	public static Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> getBotArmor(UUID playerUUID) {
		return BotEquipmentManager.getBotArmor(playerUUID);
	}

	public static void updateBotArmor(UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
		BotEquipmentManager.updateBotArmor(ownerUUID, armorMap);
	}

	public static void removeBot(UUID botUUID) {
		BotRegistry.removeBotByUUID(botUUID);
	}


	private static ClientInformation createClientInformation() {
		return new ClientInformation(
			"it_IT", 10, ChatVisiblity.FULL, true,
			0, HumanoidArm.RIGHT, false, true, ParticleStatus.ALL
		);
	}

	private static void setupBotInventory(TrainingBot bot, int totemCount) {
		if (totemCount == -1) {
			org.bukkit.inventory.ItemStack bukkitTotem = new org.bukkit.inventory.ItemStack(Material.TOTEM_OF_UNDYING);
			ItemStack nmsTotem = CraftItemStack.asNMSCopy(bukkitTotem);
			bot.setItemSlot(EquipmentSlot.OFFHAND, nmsTotem);
		} else if (totemCount > 0) {
			org.bukkit.inventory.ItemStack bukkitTotem = new org.bukkit.inventory.ItemStack(Material.TOTEM_OF_UNDYING);
			ItemStack nmsTotem = CraftItemStack.asNMSCopy(bukkitTotem);
			bot.setItemSlot(EquipmentSlot.OFFHAND, nmsTotem);
		} else {
			bot.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}
	}

	public static void updateBotTotemCount(UUID ownerUUID, int totemCount) {
		TrainingBot bot = BotEntityFinder.getBotByOwnerUUID(ownerUUID);
		if (bot != null) {
			bot.setTotemCount(totemCount);

			if (totemCount == -1 || totemCount > 0) {
				org.bukkit.inventory.ItemStack bukkitTotem = new org.bukkit.inventory.ItemStack(Material.TOTEM_OF_UNDYING);
				ItemStack nmsTotem = CraftItemStack.asNMSCopy(bukkitTotem);
				bot.setItemSlot(EquipmentSlot.OFFHAND, nmsTotem);
			} else {
				bot.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
			}
		}
	}


	private static void broadcastBotToPlayers(TrainingBot bot, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
		for (Player online : Bukkit.getOnlinePlayers()) {
			Packet.sendAddPlayerPacket(online, bot);
			Packet.sendSpawnPlayerPacket(online, bot);
		}

		BotEquipmentManager.broadcastEquipment(bot, armorMap);
	}

	public static void updateBotFollow(UUID ownerUUID, boolean follow) {
		TrainingBot bot = BotEntityFinder.getBotByOwnerUUID(ownerUUID);
		if (bot != null) {
			bot.setFollow(follow);
		}
	}

}