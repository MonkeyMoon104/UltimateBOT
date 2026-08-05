package com.monkey.ultimatebot.common.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** Stable namespaced identifier for built-in and addon-provided combat modes. */
public record CombatMode(String namespace, String value) implements Comparable<CombatMode> {
    private static final Pattern PART_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]{0,63}");
    private static final String BUILTIN_NAMESPACE = "ultimatebot";

    public static final CombatMode SWORD = builtin("sword");
    public static final CombatMode UHC = builtin("uhc");
    public static final CombatMode CART = builtin("cart");
    public static final CombatMode CRYSTAL = builtin("crystal");
    public static final CombatMode MACE = builtin("mace");
    public static final CombatMode WATER = builtin("water");
    public static final CombatMode AXE_SHIELD = builtin("axe-shield");
    public static final CombatMode NETHERITE_POT = builtin("netherite-pot");
    public static final CombatMode SMP = builtin("smp");
    public static final CombatMode TRIDENT = builtin("trident");

    private static final List<CombatMode> BUILTIN_MODES =
            List.of(SWORD, UHC, CART, CRYSTAL, MACE, WATER, AXE_SHIELD, NETHERITE_POT, SMP, TRIDENT);

    public CombatMode {
        namespace = validatePart(namespace, "namespace");
        value = validatePart(value, "value");
    }

    /** Creates a validated namespaced combat-mode identifier. */
    public static CombatMode of(String namespace, String value) {
        return new CombatMode(namespace, value);
    }

    /** Parses {@code namespace:value}; unqualified values use the UltimateBot namespace. */
    public static CombatMode parse(String input) {
        String checked = Objects.requireNonNull(input, "input").trim().toLowerCase(Locale.ROOT);
        if (checked.isEmpty()) {
            throw new IllegalArgumentException("combat mode cannot be blank");
        }
        int separator = checked.indexOf(':');
        return separator < 0
                ? builtin(checked.replace('_', '-'))
                : of(checked.substring(0, separator), checked.substring(separator + 1));
    }

    /** Resolves the legacy enum-like name of a built-in mode. */
    public static CombatMode valueOf(String name) {
        return parse(Objects.requireNonNull(name, "name").replace('_', '-'));
    }

    /** Returns the built-in modes in their canonical GUI order. */
    public static CombatMode[] values() {
        return BUILTIN_MODES.toArray(CombatMode[]::new);
    }

    /** Returns whether this identifier belongs to UltimateBot itself. */
    public boolean builtIn() {
        return BUILTIN_NAMESPACE.equals(namespace) && BUILTIN_MODES.contains(this);
    }

    /** Returns the canonical {@code namespace:value} representation. */
    public String key() {
        return namespace + ':' + value;
    }

    /** Returns the built-in configuration name or the namespaced custom key. */
    public String name() {
        return builtIn() ? value.toUpperCase(Locale.ROOT).replace('-', '_') : key();
    }

    /** Returns the default display name; registered descriptors may override it. */
    public String displayName() {
        return builtIn() ? BuiltInCombatModeCatalog.displayName(value) : humanize(value);
    }

    /** Returns the default capabilities of a built-in mode. */
    public Set<CombatCapability> capabilities() {
        return builtIn() ? BuiltInCombatModeCatalog.capabilities(value) : Set.of();
    }

    /** Returns whether the built-in defaults include a capability. */
    public boolean supports(CombatCapability capability) {
        return capabilities().contains(Objects.requireNonNull(capability, "capability"));
    }

    @Override
    public int compareTo(CombatMode other) {
        CombatMode checked = Objects.requireNonNull(other, "other");
        int builtinComparison = Integer.compare(orderOf(this), orderOf(checked));
        return builtinComparison != 0 ? builtinComparison : key().compareTo(checked.key());
    }

    @Override
    public String toString() {
        return key();
    }

    private static CombatMode builtin(String value) {
        return new CombatMode(BUILTIN_NAMESPACE, value);
    }

    private static int orderOf(CombatMode mode) {
        int index = BUILTIN_MODES.indexOf(mode);
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private static String validatePart(String input, String name) {
        String checked = Objects.requireNonNull(input, name).trim().toLowerCase(Locale.ROOT);
        if (!PART_PATTERN.matcher(checked).matches()) {
            throw new IllegalArgumentException(name + " must match " + PART_PATTERN.pattern());
        }
        return checked;
    }

    private static String humanize(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean capitalize = true;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '-' || character == '_' || character == '.') {
                if (!result.isEmpty() && result.charAt(result.length() - 1) != ' ') {
                    result.append(' ');
                }
                capitalize = true;
                continue;
            }
            result.append(capitalize ? Character.toUpperCase(character) : character);
            capitalize = false;
        }
        return result.toString();
    }
}
