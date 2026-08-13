package com.monkey.ultimatebot.common.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Stable namespaced identifier for a bot brain implementation. */
public final class BrainKey implements Comparable<BrainKey> {
    private static final Pattern PART_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]{0,63}");

    private final String namespace;
    private final String value;

    public BrainKey(String namespace, String value) {
        this.namespace = validate(namespace, "namespace");
        this.value = validate(value, "value");
    }

    public static BrainKey of(String namespace, String value) {
        return new BrainKey(namespace, value);
    }

    public static BrainKey parse(String input) {
        String checked = Objects.requireNonNull(input, "input").trim().toLowerCase(Locale.ROOT);
        int separator = checked.indexOf(':');
        if (separator <= 0 || separator == checked.length() - 1) {
            throw new IllegalArgumentException("brain key must use namespace:value");
        }
        return of(checked.substring(0, separator), checked.substring(separator + 1));
    }

    public String namespace() {
        return namespace;
    }

    public String value() {
        return value;
    }

    public String key() {
        return namespace + ':' + value;
    }

    @Override
    public int compareTo(BrainKey other) {
        return key().compareTo(Objects.requireNonNull(other, "other").key());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainKey)) {
            return false;
        }
        BrainKey other = (BrainKey) obj;
        return namespace.equals(other.namespace) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, value);
    }

    @Override
    public String toString() {
        return key();
    }

    private static String validate(String input, String name) {
        String checked = Objects.requireNonNull(input, name).trim().toLowerCase(Locale.ROOT);
        if (!PART_PATTERN.matcher(checked).matches()) {
            throw new IllegalArgumentException(name + " must match " + PART_PATTERN.pattern());
        }
        return checked;
    }
}
