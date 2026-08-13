package com.monkey.ultimatebot.utils;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public class EntityUtils {
    public static @Nullable Entity findBotByUUID(@Nullable World world, UUID botUUID) {
        if (world == null || botUUID == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(botUUID);
        return entity != null && entity.getWorld().getUID().equals(world.getUID()) ? entity : null;
    }

    public static @Nullable LivingEntity findBotAsLivingEntity(@Nullable World world, UUID botUUID) {
        Entity entity = findBotByUUID(world, botUUID);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    public static boolean removeEntity(@Nullable World world, UUID entityUUID) {
        if (world != null) {
            Entity entity = findBotByUUID(world, entityUUID);
            if (entity != null) {
                entity.remove();
                return true;
            }
        }
        return removeEntityInLoadedWorlds(entityUUID);
    }

    public static boolean removeEntityInLoadedWorlds(UUID entityUUID) {
        Entity entity = Bukkit.getEntity(entityUUID);
        if (entity != null) {
            entity.remove();
            return true;
        }
        return false;
    }
}
