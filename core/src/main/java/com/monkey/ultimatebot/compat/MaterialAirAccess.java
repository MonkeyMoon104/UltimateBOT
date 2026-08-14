package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

/**
 * {@link Material#isAir()} across Paper versions that share a Craft revision.
 *
 * <p>The method exists from 1.14.4. Paper 1.14.3 is still Craft {@code v1_14_R1}, so this is
 * dual-path in core — not a new NMS package. Fallback matches {@code AIR} / {@code CAVE_AIR} /
 * {@code VOID_AIR} by name so 1.8 never links those enum constants.
 */
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
                // fall through to name check
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
