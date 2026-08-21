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
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

final class BuiltInCombatModeStrategies {
    private BuiltInCombatModeStrategies() {}

    static Map<CombatMode, CombatModeStrategy> create(Set<PlatformCapability> platformCapabilities) {
        Set<PlatformCapability> capabilities = Objects.requireNonNull(platformCapabilities, "platformCapabilities");
        Map<CombatMode, CombatModeStrategy> strategies = new LinkedHashMap<>();
        registerIfSupported(strategies, capabilities, CombatMode.SWORD, SwordPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.UHC, UhcPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.CART, CartPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.CRYSTAL, CrystalPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.MACE, MacePvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.WATER, WaterPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.AXE_SHIELD, AxeShieldPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.NETHERITE_POT, NetheritePotPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.SMP, SmpPvPStrategy::new);
        registerIfSupported(strategies, capabilities, CombatMode.TRIDENT, TridentPvPStrategy::new);
        if (!strategies.containsKey(CombatMode.SWORD)) {
            throw new IllegalStateException("Sword PvP strategy is required on every platform");
        }
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(strategies);
    }

    private static void registerIfSupported(
            Map<CombatMode, CombatModeStrategy> strategies,
            Set<PlatformCapability> capabilities,
            CombatMode mode,
            Supplier<CombatModeStrategy> factory) {
        if (!mode.supportedBy(capabilities)) {
            return;
        }
        CombatModeStrategy strategy = factory.get();
        if (!strategy.mode().equals(mode)) {
            throw new IllegalStateException("Strategy mode mismatch for " + mode);
        }
        CombatModeStrategy previous = strategies.put(strategy.mode(), strategy);
        if (previous != null) {
            throw new IllegalStateException("Duplicate strategy for " + strategy.mode());
        }
    }
}
