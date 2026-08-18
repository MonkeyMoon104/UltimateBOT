package com.monkey.ultimatebot.access.item;

import java.lang.reflect.Field;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.Nullable;

public final class PotionEffectTypeAccess {

    private static final PotionEffectType SPEED = require("SPEED");
    private static final PotionEffectType STRENGTH = require("STRENGTH", "INCREASE_DAMAGE");
    private static final PotionEffectType INSTANT_HEALTH = require("INSTANT_HEALTH", "HEAL");
    private static final PotionEffectType JUMP_BOOST = require("JUMP_BOOST", "JUMP");
    private static final PotionEffectType REGENERATION = require("REGENERATION");
    private static final PotionEffectType ABSORPTION = require("ABSORPTION");

    private PotionEffectTypeAccess() {}

    public static PotionEffectType speed() {
        return SPEED;
    }

    public static PotionEffectType strength() {
        return STRENGTH;
    }

    public static PotionEffectType instantHealth() {
        return INSTANT_HEALTH;
    }

    public static PotionEffectType jumpBoost() {
        return JUMP_BOOST;
    }

    public static PotionEffectType regeneration() {
        return REGENERATION;
    }

    public static PotionEffectType absorption() {
        return ABSORPTION;
    }

    private static PotionEffectType require(String... names) {
        PotionEffectType type = resolve(names);
        if (type == null) {
            throw new IllegalStateException("Missing PotionEffectType: " + String.join("/", names));
        }
        return type;
    }

    private static @Nullable PotionEffectType resolve(String... names) {
        for (String name : names) {
            PotionEffectType byName = PotionEffectType.getByName(name);
            if (byName != null) {
                return byName;
            }
            try {
                Field field = PotionEffectType.class.getField(name);
                Object value = field.get(null);
                if (value instanceof PotionEffectType) {
                    return (PotionEffectType) value;
                }
            } catch (ReflectiveOperationException ignored) {

            }
        }
        return null;
    }
}
