package it.coralmc.sandbox.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.bot.util.Packet;
import it.coralmc.sandbox.bot.util.TrainingBot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
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
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.*;

public class BotSpawner {

    private static final Map<UUID, UUID> spawnedBots = new HashMap<>();

    public static void spawnFakeBot(Player viewer, Map<org.bukkit.inventory.EquipmentSlot, Material> armorMap, boolean follow) {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
        ServerLevel world = handle.serverLevel().getLevel();

        UUID botUUID = UUID.randomUUID();
        FileConfiguration config = SandboxBot.getInstance().getConfig();
        String botName = config.getString("bot.name");

        GameProfile profile = new GameProfile(botUUID, botName);
        ClientInformation clientInfo = new ClientInformation(
                "it_IT", 10, ChatVisiblity.FULL, true,
                0, HumanoidArm.RIGHT, false, true, ParticleStatus.ALL
        );

        Location loc = viewer.getLocation();

        TrainingBot bot = new TrainingBot(world, BlockPos.containing(loc.getX(), loc.getY(), loc.getZ()), 0, profile, viewer, follow);
        world.addFreshEntity(bot);

        for (var entry : armorMap.entrySet()) {
            Bukkit.getLogger().info("Armor map size: " + (armorMap == null ? "null" : armorMap.size()));
            EquipmentSlot slot = switch (entry.getKey()) {
                case HEAD -> EquipmentSlot.HEAD;
                case CHEST -> EquipmentSlot.CHEST;
                case LEGS -> EquipmentSlot.LEGS;
                case FEET -> EquipmentSlot.FEET;
                default -> null;
            };
            if (slot != null) {
                ItemStack nmsItem = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(entry.getValue()));
                bot.setItemSlot(slot, nmsItem);
                bot.inventoryMenu.broadcastChanges();
            }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer handleb = ((CraftPlayer) online).getHandle();

            Packet.sendAddPlayerPacket(online, bot);
            Packet.sendSpawnPlayerPacket(online, bot);

            List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
            for (var entry : armorMap.entrySet()) {
                EquipmentSlot slot = switch (entry.getKey()) {
                    case HEAD -> EquipmentSlot.HEAD;
                    case CHEST -> EquipmentSlot.CHEST;
                    case LEGS -> EquipmentSlot.LEGS;
                    case FEET -> EquipmentSlot.FEET;
                    default -> null;
                };
                if (slot != null) {
                    ItemStack nmsItem = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(entry.getValue()));
                    equipmentList.add(Pair.of(slot, nmsItem));
                }
            }

            if (!equipmentList.isEmpty()) {
                ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(
                        bot.getId(), equipmentList
                );
                handleb.connection.send(equipmentPacket);
            }
        }

        spawnedBots.put(viewer.getUniqueId(), botUUID);
    }
}