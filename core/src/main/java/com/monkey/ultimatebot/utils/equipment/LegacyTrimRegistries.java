package com.monkey.ultimatebot.utils.equipment;

import java.util.Objects;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

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
