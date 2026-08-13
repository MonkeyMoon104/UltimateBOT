package com.monkey.ultimatebot.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Resolves Bukkit {@link Attribute} constants across Paper versions.
 *
 * <p>Pre-~1.21.2 APIs expose {@code GENERIC_MAX_HEALTH} / {@code GENERIC_ATTACK_DAMAGE} (often as an
 * enum). Newer APIs rename them to {@code MAX_HEALTH} / {@code ATTACK_DAMAGE} and may expose
 * {@code Attribute} as an interface with static fields. Hard references to the new names throw
 * {@link NoSuchFieldError} on older runtimes — always resolve via this helper.
 */
public final class AttributeAccess {

    private static final @Nullable Attribute MAX_HEALTH =
            resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
    private static final @Nullable Attribute ATTACK_DAMAGE =
            resolveAttribute("ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");
    private static final @Nullable Attribute ATTACK_SPEED =
            resolveAttribute("ATTACK_SPEED", "GENERIC_ATTACK_SPEED");

    private AttributeAccess() {}

    public static @Nullable Attribute maxHealth() {
        return MAX_HEALTH;
    }

    public static @Nullable Attribute attackDamage() {
        return ATTACK_DAMAGE;
    }

    public static @Nullable Attribute attackSpeed() {
        return ATTACK_SPEED;
    }

    public static double maxHealthValue(LivingEntity entity) {
        Attribute attribute = MAX_HEALTH;
        if (attribute != null) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                return instance.getValue();
            }
        }
        return entity.getMaxHealth();
    }

    public static double attackDamageValue(LivingEntity entity) {
        Attribute attribute = ATTACK_DAMAGE;
        if (attribute != null) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                return instance.getValue();
            }
        }
        return 1.0D;
    }

    /** Vanilla cooldown ticks ≈ {@code ceil(20 / attackSpeed)}; sword default speed is 4.0 → 5 ticks. */
    public static int attackStrengthDelayTicks(LivingEntity entity) {
        Attribute attribute = ATTACK_SPEED;
        if (attribute != null) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null && instance.getValue() > 0.0D) {
                return Math.max(1, (int) Math.ceil(20.0D / instance.getValue()));
            }
        }
        return 5;
    }

    private static @Nullable Attribute resolveAttribute(String... names) {
        Class<?> clazz = Attribute.class;
        for (String name : names) {
            Attribute fromValueOf = tryValueOf(clazz, name);
            if (fromValueOf != null) {
                return fromValueOf;
            }
            Attribute fromField = tryStaticField(clazz, name);
            if (fromField != null) {
                return fromField;
            }
        }
        return null;
    }

    private static @Nullable Attribute tryValueOf(Class<?> clazz, String name) {
        try {
            Method valueOf = clazz.getMethod("valueOf", String.class);
            Object value = valueOf.invoke(null, name);
            if (value instanceof Attribute) {
                return (Attribute) value;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Enum constant missing, or Attribute is not an enum on this runtime.
        }
        return null;
    }

    private static @Nullable Attribute tryStaticField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getField(name);
            Object value = field.get(null);
            if (value instanceof Attribute) {
                return (Attribute) value;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Field missing on this runtime.
        }
        return null;
    }
}
