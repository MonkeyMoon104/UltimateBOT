package com.monkey.ultimatebot.access.item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.Nullable;

public final class EnchantmentAccess {

    private static final @Nullable Class<?> NAMESPACED_KEY = classOrNull("org.bukkit.NamespacedKey");
    private static final @Nullable Method MINECRAFT_KEY = method(NAMESPACED_KEY, "minecraft", String.class);
    private static final @Nullable Method GET_BY_KEY =
            NAMESPACED_KEY == null ? null : method(Enchantment.class, "getByKey", NAMESPACED_KEY);

    private EnchantmentAccess() {}

    public static @Nullable Enchantment byMinecraftKey(@Nullable String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("minecraft:")) {
            normalized = normalized.substring("minecraft:".length());
        }
        return resolve(aliases(normalized));
    }

    private static String[] aliases(String key) {
        if ("protection".equals(key)) {
            return new String[] {"PROTECTION", "PROTECTION_ENVIRONMENTAL", "protection"};
        }
        if ("blast_protection".equals(key)) {
            return new String[] {"BLAST_PROTECTION", "PROTECTION_EXPLOSIONS", "blast_protection"};
        }
        if ("fire_protection".equals(key)) {
            return new String[] {"FIRE_PROTECTION", "PROTECTION_FIRE", "fire_protection"};
        }
        if ("projectile_protection".equals(key)) {
            return new String[] {"PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE", "projectile_protection"};
        }
        if ("feather_falling".equals(key)) {
            return new String[] {"FEATHER_FALLING", "PROTECTION_FALL", "feather_falling"};
        }
        if ("unbreaking".equals(key)) {
            return new String[] {"UNBREAKING", "DURABILITY", "unbreaking"};
        }
        if ("sharpness".equals(key)) {
            return new String[] {"SHARPNESS", "DAMAGE_ALL", "sharpness"};
        }
        if ("smite".equals(key)) {
            return new String[] {"SMITE", "DAMAGE_UNDEAD", "smite"};
        }
        if ("bane_of_arthropods".equals(key)) {
            return new String[] {"BANE_OF_ARTHROPODS", "DAMAGE_ARTHROPODS", "bane_of_arthropods"};
        }
        if ("looting".equals(key)) {
            return new String[] {"LOOTING", "LOOT_BONUS_MOBS", "looting"};
        }
        if ("fortune".equals(key)) {
            return new String[] {"FORTUNE", "LOOT_BONUS_BLOCKS", "fortune"};
        }
        if ("efficiency".equals(key)) {
            return new String[] {"EFFICIENCY", "DIG_SPEED", "efficiency"};
        }
        if ("silk_touch".equals(key)) {
            return new String[] {"SILK_TOUCH", "silk_touch"};
        }
        if ("aqua_affinity".equals(key)) {
            return new String[] {"AQUA_AFFINITY", "WATER_WORKER", "aqua_affinity"};
        }
        if ("respiration".equals(key)) {
            return new String[] {"RESPIRATION", "OXYGEN", "respiration"};
        }
        if ("fire_aspect".equals(key)) {
            return new String[] {"FIRE_ASPECT", "fire_aspect"};
        }
        if ("knockback".equals(key)) {
            return new String[] {"KNOCKBACK", "knockback"};
        }
        if ("riptide".equals(key)) {
            return new String[] {"RIPTIDE", "riptide"};
        }
        if ("loyalty".equals(key)) {
            return new String[] {"LOYALTY", "loyalty"};
        }
        if ("impaling".equals(key)) {
            return new String[] {"IMPALING", "impaling"};
        }
        if ("channeling".equals(key)) {
            return new String[] {"CHANNELING", "channeling"};
        }
        return new String[] {key.toUpperCase(Locale.ROOT).replace('-', '_'), key};
    }

    private static @Nullable Enchantment resolve(String... names) {
        for (String name : names) {
            Enchantment byName = Enchantment.getByName(name);
            if (byName != null) {
                return byName;
            }
            try {
                Field field = Enchantment.class.getField(name);
                Object value = field.get(null);
                if (value instanceof Enchantment) {
                    return (Enchantment) value;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        return byKey(names[names.length - 1]);
    }

    private static @Nullable Enchantment byKey(String minecraftKey) {
        Method minecraft = MINECRAFT_KEY;
        Method getByKey = GET_BY_KEY;
        if (minecraft == null || getByKey == null) {
            return null;
        }
        try {
            Object namespaced = minecraft.invoke(null, minecraftKey);
            Object enchantment = getByKey.invoke(null, namespaced);
            return enchantment instanceof Enchantment ? (Enchantment) enchantment : null;
        } catch (ReflectiveOperationException | LinkageError | IllegalArgumentException ignored) {
            return null;
        }
    }

    private static @Nullable Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static @Nullable Method method(@Nullable Class<?> type, String name, Class<?>... params) {
        if (type == null) {
            return null;
        }
        try {
            return type.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
