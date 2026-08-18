package com.monkey.ultimatebot.access.entity;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class EntityLookupAccess {

    private static final @Nullable Method GET_ENTITY = resolve();

    private EntityLookupAccess() {}

    public static @Nullable Entity get(@Nullable UUID id) {
        if (id == null) {
            return null;
        }
        Player player = Bukkit.getPlayer(id);
        if (player != null) {
            return player;
        }
        if (GET_ENTITY != null) {
            try {
                Object result = GET_ENTITY.invoke(null, id);
                return result instanceof Entity ? (Entity) result : null;
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (id.equals(entity.getUniqueId())) {
                    return entity;
                }
            }
        }
        return null;
    }

    private static @Nullable Method resolve() {
        try {
            return Bukkit.class.getMethod("getEntity", UUID.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
