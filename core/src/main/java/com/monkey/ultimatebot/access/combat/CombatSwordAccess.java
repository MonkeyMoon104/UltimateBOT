package com.monkey.ultimatebot.access.combat;

import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;

public final class CombatSwordAccess {

    private static final boolean NETHERITE_ERA = MinecraftVersionAccess.isAtLeast(1, 17);
    private static final Material KIT_SWORD = MaterialCatalog.optional("NETHERITE_SWORD", Material.DIAMOND_SWORD);

    private CombatSwordAccess() {}

    public static boolean isKitSword(Material type) {
        return type == KIT_SWORD;
    }

    public static boolean mustHoldKitSwordToMelee() {
        return !NETHERITE_ERA;
    }
}
