package it.coralmc.sandbox.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.bot.util.equipment.manager.BotEquipmentManager;
import it.coralmc.sandbox.bot.util.packets.Packet;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BotSpawner {

	private final SandboxTraining plugin;
	private final BotRegistry botRegistry;
	private final BotEquipmentManager botEquipmentManager;
	private final Packet packet;
	private final PlayerArmorManager playerArmorManager;
	private final BotEntityFinder botEntityFinder;

	public BotSpawner(SandboxTraining plugin, BotRegistry botRegistry, BotEquipmentManager botEquipmentManager, Packet packet, BotEntityFinder botEntityFinder) {
		this.plugin = plugin;
		this.botRegistry = botRegistry;
		this.botEquipmentManager = botEquipmentManager;
        this.packet = packet;
        this.playerArmorManager = plugin.getPlayerArmorManager();
        this.botEntityFinder = botEntityFinder;
    }

	public void spawnFakeBot(Player viewer, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap, boolean follow, int totem) {
		Map<org.bukkit.inventory.EquipmentSlot, Boolean> emptyBlastProtection = new HashMap<>();
		spawnFakeBot(viewer, armorMap, emptyBlastProtection, follow, totem);
	}

	public void spawnFakeBot(Player viewer, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
							 Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap, boolean follow, int totem) {
		ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
		ServerLevel world = handle.serverLevel().getLevel();

		UUID botUUID = UUID.randomUUID();
		FileConfiguration config = plugin.getConfig();
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
		Block block = loc.getWorld().getHighestBlockAt(loc);

		TrainingBot bot = new TrainingBot(
				world,
				BlockPos.containing(block.getX(), block.getY(), block.getZ()),
				0,
				profile,
				viewer,
				follow,
				this,
				plugin,
				plugin.getConfig().getString("messages.dead-bot-msg", "You have killed the bot!")
		);

		bot.setTotemCount(totem);

		world.addFreshEntity(bot);
		//setupBotInventory(bot, totem);
		bot.getBotAI().manageTotem();
		botEquipmentManager.applyEquipment(bot, armorMap, blastProtectionMap);

		broadcastBotToPlayers(bot, armorMap, blastProtectionMap);
		botRegistry.registerBot(viewer.getUniqueId(), bot);
	}

	public boolean isBotSpawned(UUID playerUUID) {
		return botRegistry.isBotSpawned(playerUUID);
	}

	public void despawnBot(Player owner) {
		UUID ownerUUID = owner.getUniqueId();
		UUID botUUID = botRegistry.getBotUUID(ownerUUID);
		if (botUUID == null) return;

		ServerPlayer handle = ((CraftPlayer) owner).getHandle();
		ServerLevel world = handle.serverLevel();

		if (botEntityFinder.removeEntity(world, botUUID)) {
			botRegistry.removeBot(ownerUUID);
		}
	}

	public void despawnAllBots() {
		Map<UUID, TrainingBot> allBots = botRegistry.getAllBots();
		for (UUID ownerUUID : new HashMap<>(allBots).keySet()) {
			Player owner = Bukkit.getPlayer(ownerUUID);
			if (owner != null && owner.isOnline()) {
				despawnBot(owner);
			} else {
				botRegistry.removeBot(ownerUUID);
			}
		}
	}
	public Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> getBotArmor(UUID playerUUID) {
		return botEquipmentManager.getBotArmor(playerUUID, botRegistry);
	}

	public void updateBotArmor(UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
		Map<org.bukkit.inventory.EquipmentSlot, Boolean> emptyBlastProtection = new HashMap<>();
		updateBotArmor(ownerUUID, armorMap, emptyBlastProtection);
	}

	public void updateBotArmor(UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
							   Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
		botEquipmentManager.updateBotArmor(ownerUUID, armorMap, blastProtectionMap, botRegistry);
	}

	public void removeBot(UUID botUUID) {
		botRegistry.removeBotByUUID(botUUID);
	}


	private ClientInformation createClientInformation() {
		return new ClientInformation(
			"it_IT", 10, ChatVisiblity.FULL, true,
			0, HumanoidArm.RIGHT, false, true, ParticleStatus.ALL
		);
	}

	private void setupBotInventory(TrainingBot bot, int totemCount) {
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

	public void updateBotTotemCount(UUID ownerUUID, int totemCount) {
		TrainingBot bot = botEntityFinder.getBotByOwnerUUID(ownerUUID);
		if (bot != null) {
			bot.setTotemCount(totemCount);
			bot.getBotAI().manageTotem();
		}
	}


	private void broadcastBotToPlayers(TrainingBot bot, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
		Map<org.bukkit.inventory.EquipmentSlot, Boolean> emptyBlastProtection = new HashMap<>();
		broadcastBotToPlayers(bot, armorMap, emptyBlastProtection);
	}

	private void broadcastBotToPlayers(TrainingBot bot, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
									   Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
		for (Player online : Bukkit.getOnlinePlayers()) {
			packet.sendAddPlayerPacket(online, bot);
			packet.sendSpawnPlayerPacket(online, bot);
		}

		botEquipmentManager.broadcastEquipment(bot, armorMap, blastProtectionMap);
	}

	public void updateBotFollow(UUID ownerUUID, boolean follow) {
		TrainingBot bot = botEntityFinder.getBotByOwnerUUID(ownerUUID);
		if (bot != null) {
			bot.setFollow(follow);
		}
	}

	public void despawnBotInWorld(Player owner, World fromWorld) {
		UUID ownerUUID = owner.getUniqueId();
		UUID botUUID = botRegistry.getBotUUID(ownerUUID);
		if (botUUID == null) return;

		ServerLevel world = ((CraftWorld) fromWorld).getHandle();
		if (botEntityFinder.removeEntity(world, botUUID)) {
			botRegistry.removeBot(ownerUUID);
		}
	}
}