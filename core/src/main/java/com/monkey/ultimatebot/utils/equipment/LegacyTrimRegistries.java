package com.monkey.ultimatebot.utils.equipment;

import java.util.Objects;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

/**
 * Trim registries via Bukkit static fields ({@code Registry.TRIM_*}), available since armor trims
 * landed on 1.20 — including Paper 1.20.4 where {@code RegistryAccess} does not exist.
 */
final class LegacyTrimRegistries implements TrimRegistries {

    @Override
    public Registry<TrimPattern> patterns() {
        return Objects.requireNonNull(Registry.TRIM_PATTERN, "Registry.TRIM_PATTERN");
    }

    @Override
    public Registry<TrimMaterial> materials() {
        return Objects.requireNonNull(Registry.TRIM_MATERIAL, "Registry.TRIM_MATERIAL");
    }
}
