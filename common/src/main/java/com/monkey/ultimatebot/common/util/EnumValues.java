package com.monkey.ultimatebot.common.util;

import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Safe parsing helpers for enum values received across module boundaries. */
public final class EnumValues {
    private EnumValues() {}

    public static <E extends Enum<E>> @Nullable E parse(
            Class<E> enumType, @Nullable String value, @Nullable E fallback) {
        Objects.requireNonNull(enumType, "enumType");
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    public static <E extends Enum<E>> E parseOrDefault(Class<E> enumType, @Nullable String value, E fallback) {
        return Objects.requireNonNull(parse(enumType, value, Objects.requireNonNull(fallback, "fallback")));
    }
}
