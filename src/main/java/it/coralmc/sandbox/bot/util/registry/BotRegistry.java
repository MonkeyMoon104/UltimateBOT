package it.coralmc.sandbox.bot.util.registry;

import it.coralmc.sandbox.bot.util.TrainingBot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BotRegistry {

	private static final Map<UUID, TrainingBot> spawnedBots = new HashMap<>();

	public static void registerBot(UUID playerUUID, TrainingBot bot) {
		spawnedBots.put(playerUUID, bot);
	}

	public static boolean isBotSpawned(UUID playerUUID) {
		return spawnedBots.containsKey(playerUUID);
	}

	public static UUID getBotUUID(UUID playerUUID) {
		return spawnedBots.get(playerUUID).getUUID();
	}

	public static void removeBot(UUID playerUUID) {
		spawnedBots.remove(playerUUID);
	}

	public static void removeBotByUUID(UUID botUUID) {
		spawnedBots.entrySet().removeIf(entry -> entry.getValue().getUUID().equals(botUUID));
	}

	public static Map<UUID, TrainingBot> getAllBots() {
		return spawnedBots;
	}

	public static void clear() {
		spawnedBots.clear();
	}
}