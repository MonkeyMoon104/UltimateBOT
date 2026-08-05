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
        CombatMode checked = Objects.requireNonNull(mode, "mode");
        return checked.equals(CombatMode.SWORD)
                        || checked.equals(CombatMode.UHC)
                        || checked.equals(CombatMode.WATER)
                        || checked.equals(CombatMode.TRIDENT)
                ? ArmorTier.DIAMOND
                : ArmorTier.NETHERITE;
    }
}
