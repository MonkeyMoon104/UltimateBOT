package com.monkey.ultimatebot.combat.mode.shared;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.utils.armor.ArmorTier;
import java.util.Objects;

public final class CombatModeLoadoutDefaults {
    private CombatModeLoadoutDefaults() {}

    public static void applyArmor(BotOptions options, CombatMode mode) {
        Objects.requireNonNull(options, "options").setArmorType(armorTier(mode));
    }

    public static ArmorTier armorTier(CombatMode mode) {
        return switch (Objects.requireNonNull(mode, "mode")) {
            case SWORD, UHC, WATER, TRIDENT -> ArmorTier.DIAMOND;
            case CART, CRYSTAL, MACE, AXE_SHIELD, NETHERITE_POT, SMP -> ArmorTier.NETHERITE;
        };
    }
}
