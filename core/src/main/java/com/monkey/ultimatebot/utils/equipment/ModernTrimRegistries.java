package com.monkey.ultimatebot.utils.equipment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

/**
 * Trim registries via Paper {@link RegistryAccess} (1.20.5+). Loaded only when that API is present
 * so older servers never link this class.
 */
final class ModernTrimRegistries implements TrimRegistries {

    @Override
    public Registry<TrimPattern> patterns() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
    }

    @Override
    public Registry<TrimMaterial> materials() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL);
    }
}
