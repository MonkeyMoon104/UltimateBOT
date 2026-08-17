package com.monkey.ultimatebot.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Resolves Bukkit attribute constants across Paper/Spigot versions without hard-linking {@code
 * org.bukkit.attribute.Attribute}.
 *
 * <p>That package does not exist on 1.8. A field typed {@code Attribute} in this class would fail
 * class init ({@code NoClassDefFoundError} / {@code Could not initialize class}). Pre-~1.21.2 names
 * are {@code GENERIC_MAX_HEALTH}; newer APIs use {@code MAX_HEALTH}.
 */
public final class AttributeAccess {

    private static final @Nullable Object MAX_HEALTH;
    private static final @Nullable Object ATTACK_DAMAGE;
    private static final @Nullable Object ATTACK_SPEED;
    private static final @Nullable Method GET_ATTRIBUTE;
    private static final @Nullable Method INSTANCE_GET_VALUE;

    static {
        Object maxHealth = null;
        Object attackDamage = null;
        Object attackSpeed = null;
        Method getAttribute = null;
        Method instanceGetValue = null;
        try {
            Class<?> attributeClass = Class.forName("org.bukkit.attribute.Attribute");
            maxHealth = resolveAttribute(attributeClass, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
            attackDamage = resolveAttribute(attributeClass, "ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");
            attackSpeed = resolveAttribute(attributeClass, "ATTACK_SPEED", "GENERIC_ATTACK_SPEED");
            getAttribute = LivingEntity.class.getMethod("getAttribute", attributeClass);
            Class<?> instanceClass = Class.forName("org.bukkit.attribute.AttributeInstance");
            instanceGetValue = instanceClass.getMethod("getValue");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // 1.8: no org.bukkit.attribute package.
        } catch (Throwable ignored) {
            // Keep getMaxHealth fallback; do not fail class init.
        }
        MAX_HEALTH = maxHealth;
        ATTACK_DAMAGE = attackDamage;
        ATTACK_SPEED = attackSpeed;
        GET_ATTRIBUTE = getAttribute;
        INSTANCE_GET_VALUE = instanceGetValue;
    }

    private AttributeAccess() {}

    public static double maxHealthValue(LivingEntity entity) {
        Double fromAttribute = attributeValue(entity, MAX_HEALTH);
        if (fromAttribute != null) {
            return fromAttribute.doubleValue();
        }
        return entity.getMaxHealth();
    }

    public static double attackDamageValue(LivingEntity entity) {
        Double fromAttribute = attributeValue(entity, ATTACK_DAMAGE);
        if (fromAttribute != null) {
            return fromAttribute.doubleValue();
        }
        return 1.0D;
    }

    /** Vanilla cooldown ticks ≈ {@code ceil(20 / attackSpeed)}; sword default speed is 4.0 → 5 ticks. */
    public static int attackStrengthDelayTicks(LivingEntity entity) {
        Double speed = attributeValue(entity, ATTACK_SPEED);
        if (speed != null && speed.doubleValue() > 0.0D) {
            return Math.max(1, (int) Math.ceil(20.0D / speed.doubleValue()));
        }
        return 5;
    }

    private static @Nullable Double attributeValue(LivingEntity entity, @Nullable Object attribute) {
        if (attribute == null || GET_ATTRIBUTE == null || INSTANCE_GET_VALUE == null) {
            return null;
        }
        try {
            Object instance = GET_ATTRIBUTE.invoke(entity, attribute);
            if (instance == null) {
                return null;
            }
            Object value = INSTANCE_GET_VALUE.invoke(instance);
            if (value instanceof Double) {
                return (Double) value;
            }
            if (value instanceof Number) {
                return Double.valueOf(((Number) value).doubleValue());
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through to Bukkit getMaxHealth / defaults
        }
        return null;
    }

    private static @Nullable Object resolveAttribute(Class<?> clazz, String... names) {
        for (int i = 0; i < names.length; i++) {
            Object fromValueOf = tryValueOf(clazz, names[i]);
            if (fromValueOf != null) {
                return fromValueOf;
            }
            Object fromField = tryStaticField(clazz, names[i]);
            if (fromField != null) {
                return fromField;
            }
        }
        return null;
    }

    private static @Nullable Object tryValueOf(Class<?> clazz, String name) {
        try {
            Method valueOf = clazz.getMethod("valueOf", String.class);
            return valueOf.invoke(null, name);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static @Nullable Object tryStaticField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getField(name);
            return field.get(null);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
