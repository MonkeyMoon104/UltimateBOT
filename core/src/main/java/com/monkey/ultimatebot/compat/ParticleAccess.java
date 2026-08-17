package com.monkey.ultimatebot.compat;

import java.lang.reflect.Field;
import org.bukkit.Particle;
import org.jspecify.annotations.Nullable;

/**
 * Resolves {@link Particle} constants across Paper renames (1.20.5 particle cleanup).
 *
 * <p>Hard references to newer names (e.g. {@code WITCH}) throw {@link NoSuchFieldError} on 1.20.4
 * and earlier — always resolve via this helper for cross-version FX. Version-gated particles
 * (totem, 1.11+) must include a 1.9-era alias so class init does not fail on 1.9–1.10.
 */
public final class ParticleAccess {

    private static final Particle HEART = require("HEART");
    private static final Particle LAVA = require("LAVA");
    private static final Particle CRIT = require("CRIT");
    private static final Particle CLOUD = require("CLOUD");
    private static final Particle WITCH = require("WITCH", "SPELL_WITCH");
    private static final Particle ENCHANTED_HIT = require("ENCHANTED_HIT", "CRIT_MAGIC");
    private static final Particle EFFECT = require("EFFECT", "SPELL");
    private static final Particle INSTANT_EFFECT = require("INSTANT_EFFECT", "SPELL_INSTANT");
    private static final Particle ENTITY_EFFECT = require("ENTITY_EFFECT", "SPELL_MOB");
    private static final Particle ENCHANT = require("ENCHANT", "ENCHANTMENT_TABLE");
    private static final Particle SMOKE = require("SMOKE", "SMOKE_NORMAL");
    private static final Particle LARGE_SMOKE = require("LARGE_SMOKE", "SMOKE_LARGE");
    private static final Particle EXPLOSION = require("EXPLOSION", "EXPLOSION_LARGE", "EXPLOSION_NORMAL");
    private static final Particle EXPLOSION_EMITTER = require("EXPLOSION_EMITTER", "EXPLOSION_HUGE");
    private static final Particle FIREWORK = require("FIREWORK", "FIREWORKS_SPARK");
    // Totem burst exists from 1.11. 1.9–1.10 have Particle but not TOTEM — keep clinit alive.
    private static final Particle TOTEM =
            require("TOTEM_OF_UNDYING", "TOTEM", "SPELL_WITCH", "CRIT_MAGIC");
    private static final Particle HAPPY_VILLAGER = require("HAPPY_VILLAGER", "VILLAGER_HAPPY");
    private static final Particle ANGRY_VILLAGER = require("ANGRY_VILLAGER", "VILLAGER_ANGRY");
    private static final Particle POOF = require("POOF", "EXPLOSION_NORMAL", "CLOUD");

    private ParticleAccess() {}

    /** Prefer modern name first, then legacy aliases. */
    public static Particle of(String... names) {
        return require(names);
    }

    public static Particle heart() {
        return HEART;
    }

    public static Particle lava() {
        return LAVA;
    }

    public static Particle crit() {
        return CRIT;
    }

    public static Particle cloud() {
        return CLOUD;
    }

    public static Particle witch() {
        return WITCH;
    }

    public static Particle enchantedHit() {
        return ENCHANTED_HIT;
    }

    public static Particle effect() {
        return EFFECT;
    }

    public static Particle instantEffect() {
        return INSTANT_EFFECT;
    }

    public static Particle entityEffect() {
        return ENTITY_EFFECT;
    }

    public static Particle enchant() {
        return ENCHANT;
    }

    public static Particle smoke() {
        return SMOKE;
    }

    public static Particle largeSmoke() {
        return LARGE_SMOKE;
    }

    public static Particle explosion() {
        return EXPLOSION;
    }

    public static Particle explosionEmitter() {
        return EXPLOSION_EMITTER;
    }

    public static Particle firework() {
        return FIREWORK;
    }

    public static Particle totem() {
        return TOTEM;
    }

    public static Particle happyVillager() {
        return HAPPY_VILLAGER;
    }

    public static Particle angryVillager() {
        return ANGRY_VILLAGER;
    }

    public static Particle poof() {
        return POOF;
    }

    private static Particle require(String... names) {
        Particle particle = resolve(names);
        if (particle == null) {
            throw new IllegalStateException("Missing Particle: " + String.join("/", names));
        }
        return particle;
    }

    private static @Nullable Particle resolve(String... names) {
        for (String name : names) {
            try {
                Field field = Particle.class.getField(name);
                Object value = field.get(null);
                if (value instanceof Particle) {
                    return (Particle) value;
                }
            } catch (ReflectiveOperationException ignored) {
                // try next alias
            }
            try {
                return Particle.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // try next alias
            }
        }
        return null;
    }
}
