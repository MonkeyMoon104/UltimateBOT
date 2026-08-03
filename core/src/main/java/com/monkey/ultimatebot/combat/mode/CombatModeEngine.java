package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.combat.mode.cart.CartPvPStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
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
import com.monkey.ultimatebot.world.WorldProtectionService;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class CombatModeEngine implements AutoCloseable {
    private final BotOptions options;
    private final CombatModeContext context;
    private final Map<CombatMode, CombatModeStrategy> strategies;

    private @Nullable CombatModeStrategy activeStrategy;
    private boolean suspended;
    private boolean closed;

    public CombatModeEngine(
            Player bot,
            BotOptions options,
            BotMovementController movement,
            BotRotationController rotation,
            BotAttackController attack,
            BotInventoryController inventory,
            BotCPVPController crystal,
            BotRAPVPController anchor,
            ICombatStrategyExecutor legacyCombat,
            WorldProtectionService worldProtection) {
        this.options = Objects.requireNonNull(options, "options");
        this.context = new CombatModeContext(
                bot, options, movement, rotation, attack, inventory, crystal, anchor, legacyCombat, worldProtection);
        EnumMap<CombatMode, CombatModeStrategy> registeredStrategies = new EnumMap<>(CombatMode.class);
        register(registeredStrategies, new SwordPvPStrategy());
        register(registeredStrategies, new UhcPvPStrategy());
        register(registeredStrategies, new CartPvPStrategy());
        register(registeredStrategies, new CrystalPvPStrategy());
        register(registeredStrategies, new MacePvPStrategy());
        register(registeredStrategies, new WaterPvPStrategy());
        register(registeredStrategies, new AxeShieldPvPStrategy());
        register(registeredStrategies, new NetheritePotPvPStrategy());
        register(registeredStrategies, new SmpPvPStrategy());
        register(registeredStrategies, new TridentPvPStrategy());
        this.strategies = Map.copyOf(registeredStrategies);
        if (strategies.size() != CombatMode.values().length) {
            throw new IllegalStateException("Every combat mode must have exactly one strategy");
        }
    }

    public void tick(LivingEntity target) {
        Objects.requireNonNull(target, "target");
        if (closed) {
            throw new IllegalStateException("Combat mode engine is already closed");
        }
        CombatModeStrategy selectedStrategy = selectedStrategy();
        if (!Objects.equals(activeStrategy, selectedStrategy)) {
            transitionTo(selectedStrategy);
        }
        suspended = false;
        selectedStrategy.tick(context, target);
    }

    public boolean controlsNavigation(LivingEntity target) {
        Objects.requireNonNull(target, "target");
        if (closed) {
            return false;
        }
        return selectedStrategy().controlsNavigation(context, target);
    }

    public void suspend() {
        if (activeStrategy == null || suspended) {
            return;
        }
        activeStrategy.exit(context);
        suspended = true;
    }

    public void recordShieldImpact(UUID attackerUUID) {
        Objects.requireNonNull(attackerUUID, "attackerUUID");
        if (!closed) {
            context.signals().recordShieldImpact(attackerUUID);
        }
    }

    public void deactivate() {
        if (activeStrategy == null) {
            return;
        }
        if (!suspended) {
            activeStrategy.exit(context);
        }
        activeStrategy = null;
        suspended = false;
    }

    private void transitionTo(CombatModeStrategy selectedStrategy) {
        deactivate();
        selectedStrategy.enter(context);
        activeStrategy = selectedStrategy;
    }

    private CombatModeStrategy selectedStrategy() {
        CombatModeStrategy selectedStrategy = strategies.get(options.getCombatMode());
        if (selectedStrategy == null) {
            throw new IllegalStateException("No strategy registered for " + options.getCombatMode());
        }
        return selectedStrategy;
    }

    private static void register(EnumMap<CombatMode, CombatModeStrategy> strategies, CombatModeStrategy strategy) {
        CombatModeStrategy previous = strategies.put(strategy.mode(), strategy);
        if (previous != null) {
            throw new IllegalStateException("Duplicate strategy for " + strategy.mode());
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        deactivate();
        context.close();
        closed = true;
    }
}
