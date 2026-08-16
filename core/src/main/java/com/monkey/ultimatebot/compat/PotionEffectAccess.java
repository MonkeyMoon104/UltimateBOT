package com.monkey.ultimatebot.compat;

import java.lang.reflect.Constructor;
import java.util.Objects;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.Nullable;

/**
 * Builds {@link PotionEffect} across Bukkit revisions.
 *
 * <p>The 6-arg ctor {@code (type, duration, amplifier, ambient, particles, icon)} is missing on
 * 1.12 and earlier — linking it causes {@link NoSuchMethodError}. Prefer the 5-arg ctor when
 * present; fall back to 4-arg / 3-arg via reflection only when needed.
 */
public final class PotionEffectAccess {

    private static final @Nullable Constructor<PotionEffect> WITH_ICON =
            ctor(PotionEffectType.class, int.class, int.class, boolean.class, boolean.class, boolean.class);
    private static final @Nullable Constructor<PotionEffect> WITH_PARTICLES =
            ctor(PotionEffectType.class, int.class, int.class, boolean.class, boolean.class);
    private static final @Nullable Constructor<PotionEffect> WITH_AMBIENT =
            ctor(PotionEffectType.class, int.class, int.class, boolean.class);

    private PotionEffectAccess() {}

    public static PotionEffect of(PotionEffectType type, int duration, int amplifier) {
        return of(type, duration, amplifier, false, false, false);
    }

    public static PotionEffect of(
            PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        return of(type, duration, amplifier, ambient, particles, false);
    }

    public static PotionEffect of(
            PotionEffectType type,
            int duration,
            int amplifier,
            boolean ambient,
            boolean particles,
            boolean icon) {
        Objects.requireNonNull(type, "type");
        if (WITH_ICON != null) {
            return newInstance(WITH_ICON, type, duration, amplifier, ambient, particles, icon);
        }
        if (WITH_PARTICLES != null) {
            return newInstance(WITH_PARTICLES, type, duration, amplifier, ambient, particles);
        }
        if (WITH_AMBIENT != null) {
            return newInstance(WITH_AMBIENT, type, duration, amplifier, ambient);
        }
        return new PotionEffect(type, duration, amplifier);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Constructor<PotionEffect> ctor(Class<?>... params) {
        try {
            return (Constructor<PotionEffect>) PotionEffect.class.getConstructor(params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static PotionEffect newInstance(Constructor<PotionEffect> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to construct PotionEffect", ex);
        }
    }
}
