package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.combat.mode.cart.CartPvPStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.strategy.AxeShieldPvPStrategy;
import com.monkey.ultimatebot.combat.mode.strategy.CrystalPvPStrategy;
import com.monkey.ultimatebot.combat.mode.strategy.MacePvPStrategy;
import com.monkey.ultimatebot.combat.mode.strategy.NetheritePotPvPStrategy;
import com.monkey.ultimatebot.combat.mode.strategy.SmpPvPStrategy;
import com.monkey.ultimatebot.combat.mode.strategy.SwordPvPStrategy;
import com.monkey.ultimatebot.combat.mode.trident.TridentPvPStrategy;
import com.monkey.ultimatebot.combat.mode.uhc.UhcPvPStrategy;
import com.monkey.ultimatebot.combat.mode.water.WaterPvPStrategy;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.LinkedHashMap;
import java.util.Map;

final class BuiltInCombatModeStrategies {
    private BuiltInCombatModeStrategies() {}

    static Map<CombatMode, CombatModeStrategy> create() {
        Map<CombatMode, CombatModeStrategy> strategies = new LinkedHashMap<>();
        register(strategies, new SwordPvPStrategy());
        register(strategies, new UhcPvPStrategy());
        register(strategies, new CartPvPStrategy());
        register(strategies, new CrystalPvPStrategy());
        register(strategies, new MacePvPStrategy());
        register(strategies, new WaterPvPStrategy());
        register(strategies, new AxeShieldPvPStrategy());
        register(strategies, new NetheritePotPvPStrategy());
        register(strategies, new SmpPvPStrategy());
        register(strategies, new TridentPvPStrategy());
        if (strategies.size() != CombatMode.values().length) {
            throw new IllegalStateException("Every built-in combat mode must have exactly one strategy");
        }
        return Map.copyOf(strategies);
    }

    private static void register(Map<CombatMode, CombatModeStrategy> strategies, CombatModeStrategy strategy) {
        CombatModeStrategy previous = strategies.put(strategy.mode(), strategy);
        if (previous != null) {
            throw new IllegalStateException("Duplicate strategy for " + strategy.mode());
        }
    }
}
