package com.monkey.mcbot.common.util;

import org.jspecify.annotations.Nullable;

/** Null-safe text normalization shared by public and runtime models. */
public final class TextValues {
    private TextValues() {}

    public static @Nullable String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String orElseIfBlank(@Nullable String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
