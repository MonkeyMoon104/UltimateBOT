package it.coralmc.sandbox.bot.util.packets;

import com.mojang.authlib.GameProfile;
import it.coralmc.sandbox.bot.util.TrainingBot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class Packet {

	public void sendAddPlayerPacket(Player viewer, TrainingBot bot) {
		GameProfile profile = bot.getGameProfile();
		Component displayName = Component.literal(profile.getName());

		ClientboundPlayerInfoUpdatePacket.Entry playerInfo = new ClientboundPlayerInfoUpdatePacket.Entry(
			bot.getUUID(),
			profile,
			true,
			0,
			GameType.SURVIVAL,
			displayName,
			true,
			0,
			null
		);

		ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
			EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
			List.of(playerInfo)
		);

		ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
		handle.connection.send(packet);
	}

	public void sendSpawnPlayerPacket(Player viewer, TrainingBot fakePlayer) {
		int entityId = fakePlayer.getId();
		UUID uuid = fakePlayer.getUUID();
		double x = fakePlayer.getX();
		double y = fakePlayer.getY();
		double z = fakePlayer.getZ();
		float xRot = fakePlayer.getXRot();
		float yRot = fakePlayer.getYRot();
		EntityType<?> type = EntityType.PLAYER;
		int data = 0;
		Vec3 deltaMovement = new Vec3(0, 0, 0);
		double yHeadRot = fakePlayer.getYHeadRot();

		ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
			entityId, uuid, x, y, z,
			xRot, yRot, type, data, deltaMovement, yHeadRot
		);

		ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
		handle.connection.send(spawnPacket);
	}
}
