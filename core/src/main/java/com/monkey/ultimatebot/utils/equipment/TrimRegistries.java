package com.monkey.ultimatebot.utils.equipment;

import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

interface TrimRegistries {

    Registry<TrimPattern> patterns();

    Registry<TrimMaterial> materials();
}
