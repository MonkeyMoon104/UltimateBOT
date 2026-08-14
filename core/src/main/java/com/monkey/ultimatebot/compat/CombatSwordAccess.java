package com.monkey.ultimatebot.compat;

import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;

/**
 * CPvP kit-sword identity and 1.16 melee/hotbar dual-path.
 *
 * <p>Netherite exists from 1.16. {@link MaterialCatalog} only falls back to diamond on 1.15 and
 * below. Dual-path below is not the sword material: 1.17+ keeps original “attack / switch-to-sword
 * while CPvP ticks” timing. 1.16 must not steal crystal/obsidian/anchor from the hand or hug inside
 * {@code minCrystalDistance}, or CPvP collapses into a sword-strafe loop.
 */
public final class CombatSwordAccess {

    private static final boolean NETHERITE_ERA = MinecraftVersionAccess.isAtLeast(1, 17);
    private static final Material KIT_SWORD =
            MaterialCatalog.optional("NETHERITE_SWORD", Material.DIAMOND_SWORD);

    private CombatSwordAccess() {}

    public static boolean isKitSword(Material type) {
        return type == KIT_SWORD;
    }

    /**
     * Pre-1.17 CPvP: do not punch the player with crystal items and do not switch back to the sword
     * in the same tick CPvP just selected them.
     */
    public static boolean mustHoldKitSwordToMelee() {
        return !NETHERITE_ERA;
    }
}
