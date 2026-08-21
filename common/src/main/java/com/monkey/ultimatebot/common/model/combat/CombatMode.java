package com.monkey.ultimatebot.common.model.combat;

import com.monkey.ultimatebot.common.model.platform.PlatformCapability;

import com.monkey.ultimatebot.common.util.ImmutableCollections;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class CombatMode implements Comparable<CombatMode> {
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

    private static final List<CombatMode> BUILTIN_MODES = Collections.unmodifiableList(
            Arrays.asList(SWORD, UHC, CART, CRYSTAL, MACE, WATER, AXE_SHIELD, NETHERITE_POT, SMP, TRIDENT));

    private final String namespace;
    private final String value;

    public CombatMode(String namespace, String value) {
        this.namespace = validatePart(namespace, "namespace");
        this.value = validatePart(value, "value");
    }

    public static CombatMode of(String namespace, String value) {
        return new CombatMode(namespace, value);
    }

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

    public static CombatMode valueOf(String name) {
        return parse(Objects.requireNonNull(name, "name").replace('_', '-'));
    }

    public static CombatMode[] values() {
        return BUILTIN_MODES.toArray(new CombatMode[0]);
    }

    public String namespace() {
        return namespace;
    }

    public String value() {
        return value;
    }

    public boolean builtIn() {
        return BUILTIN_NAMESPACE.equals(namespace) && BUILTIN_MODES.contains(this);
    }

    public String key() {
        return namespace + ':' + value;
    }

    public String name() {
        return builtIn() ? value.toUpperCase(Locale.ROOT).replace('-', '_') : key();
    }

    public String displayName() {
        return builtIn() ? BuiltInCombatModeCatalog.displayName(value) : humanize(value);
    }

    public Set<CombatCapability> capabilities() {
        return builtIn() ? BuiltInCombatModeCatalog.capabilities(value) : ImmutableCollections.emptySet();
    }

    public boolean supports(CombatCapability capability) {
        return capabilities().contains(Objects.requireNonNull(capability, "capability"));
    }

    public Set<PlatformCapability> requiredPlatformCapabilities() {
        return builtIn()
                ? BuiltInCombatModeCatalog.requiredPlatformCapabilities(value)
                : ImmutableCollections.emptySet();
    }

    public boolean supportedBy(Set<PlatformCapability> available) {
        return Objects.requireNonNull(available, "available").containsAll(requiredPlatformCapabilities());
    }

    @Override
    public int compareTo(CombatMode other) {
        CombatMode checked = Objects.requireNonNull(other, "other");
        int builtinComparison = Integer.compare(orderOf(this), orderOf(checked));
        return builtinComparison != 0 ? builtinComparison : key().compareTo(checked.key());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CombatMode)) {
            return false;
        }
        CombatMode other = (CombatMode) obj;
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
                if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
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
