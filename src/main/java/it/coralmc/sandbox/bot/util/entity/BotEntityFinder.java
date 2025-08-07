package it.coralmc.sandbox.bot.util.entity;

import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class BotEntityFinder {

	public static Entity findBotByUUID(ServerLevel world, UUID botUUID) {
		List<? extends Entity> entities = world.getEntities(
			EntityTypeTest.forClass(Entity.class),
			entity -> entity.getUUID().equals(botUUID)
		);

		return entities.isEmpty() ? null : entities.get(0);
	}

	public static LivingEntity findBotAsLivingEntity(ServerLevel world, UUID botUUID) {
		Entity entity = findBotByUUID(world, botUUID);
		return entity instanceof LivingEntity ? (LivingEntity) entity : null;
	}

	public static ServerLevel getPlayerWorld(UUID playerUUID) {
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.getUniqueId().equals(playerUUID)) {
				ServerPlayer handle = ((CraftPlayer) online).getHandle();
				return handle.serverLevel();
			}
		}
		return null;
	}

	public static ServerPlayer getPlayerHandle(UUID playerUUID) {
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.getUniqueId().equals(playerUUID)) {
				return ((CraftPlayer) online).getHandle();
			}
		}
		return null;
	}

	public static boolean removeEntity(ServerLevel world, UUID entityUUID) {
		Entity entity = findBotByUUID(world, entityUUID);
		if (entity != null) {
			entity.remove(Entity.RemovalReason.DISCARDED);
			return true;
		}
		return false;
	}

	public static TrainingBot getBotByOwnerUUID(UUID ownerUUID) {
		ServerLevel world = getPlayerWorld(ownerUUID);
		if (world == null) return null;

		UUID botUUID = BotRegistry.getBotUUID(ownerUUID);
		Entity entity = findBotByUUID(world, botUUID);

		if (entity instanceof TrainingBot trainingBot) {
			return trainingBot;
		}

		return null;
	}


	public static boolean entityExists(ServerLevel world, UUID entityUUID) {
		return findBotByUUID(world, entityUUID) != null;
	}
}