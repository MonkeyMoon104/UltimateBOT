package com.monkey.ultimatebot.utils;

import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class EntityUtils {
    public static Entity findBotByUUID(ServerLevel world, UUID botUUID) {
        List<? extends Entity> entities =
                world.getEntities(EntityTypeTest.forClass(Entity.class), entity -> entity.getUUID()
                        .equals(botUUID));

        return entities.isEmpty() ? null : entities.getFirst();
    }

    public static LivingEntity findBotAsLivingEntity(ServerLevel world, UUID botUUID) {
        Entity entity = findBotByUUID(world, botUUID);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public static ServerLevel getPlayerWorld(UUID playerUUID) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(playerUUID)) {
                ServerPlayer handle = ((CraftPlayer) online).getHandle();
                return NMSBridgeManager.get().getServerLevel(handle);
            }
        }
        return null;
    }

    public static boolean removeEntity(ServerLevel world, UUID entityUUID) {
        if (world != null) {
            Entity entity = findBotByUUID(world, entityUUID);
            if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return true;
            }
        }
        return removeEntityInLoadedWorlds(entityUUID);
    }

    public static boolean removeEntityInLoadedWorlds(UUID entityUUID) {
        for (World loadedWorld : Bukkit.getWorlds()) {
            ServerLevel handle = ((CraftWorld) loadedWorld).getHandle();
            Entity entity = findBotByUUID(handle, entityUUID);
            if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                return true;
            }
        }
        return false;
    }
}
