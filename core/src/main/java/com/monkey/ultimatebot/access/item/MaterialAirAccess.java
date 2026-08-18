package com.monkey.ultimatebot.access.item;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

public final class MaterialAirAccess {

    private static final @Nullable Method IS_AIR = resolve();

    private MaterialAirAccess() {}

    public static boolean isAir(Material material) {
        Objects.requireNonNull(material, "material");
        if (IS_AIR != null) {
            try {
                Object result = IS_AIR.invoke(material);
                return result instanceof Boolean && ((Boolean) result).booleanValue();
            } catch (ReflectiveOperationException ignored) {

            }
        }
        String name = material.name();
        return "AIR".equals(name) || "CAVE_AIR".equals(name) || "VOID_AIR".equals(name);
    }

    private static @Nullable Method resolve() {
        try {
            return Material.class.getMethod("isAir");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
