package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeStrategy;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.extension.runtime.CoreBotControl;
import com.monkey.ultimatebot.extension.runtime.CoreNativeBotAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.world.WorldProtectionService;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class CombatModeEngine implements AutoCloseable {
    private final UltimateBot plugin;
    private final BotOptions options;
    private final CombatModeContext context;
    private final Map<CombatMode, CombatModeStrategy> strategies;
    private final ExternalCombatModeSessionManager external;

    private @Nullable CombatMode activeMode;
    private @Nullable CombatModeStrategy activeStrategy;
    private boolean suspended;
    private boolean closed;

    public CombatModeEngine(
            UltimateBot plugin,
            ITrainingBot bot,
            BotOptions options,
            BotMovementController movement,
            BotRotationController rotation,
            BotAttackController attack,
            BotInventoryController inventory,
            BotCPVPController crystal,
            BotRAPVPController anchor,
            ICombatStrategyExecutor legacyCombat,
            WorldProtectionService worldProtection,
            CoreBotControl control,
            CoreNativeBotAccess nativeAccess) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.options = Objects.requireNonNull(options, "options");
        this.context = new CombatModeContext(
                bot, options, movement, rotation, attack, inventory, crystal, anchor, legacyCombat, worldProtection);
        this.external = new ExternalCombatModeSessionManager(
                plugin,
                options,
                context,
                Objects.requireNonNull(control, "control"),
                Objects.requireNonNull(nativeAccess, "nativeAccess"),
                new SplittableRandom(bot.getUniqueId().getMostSignificantBits()
                        ^ bot.getUniqueId().getLeastSignificantBits()));
        this.strategies = BuiltInCombatModeStrategies.create(NMSBridgeManager.capabilities());
    }

    public void tick(LivingEntity target) {
        ensureOpen();
        Objects.requireNonNull(target, "target");
        transitionIfRequired();
        resumeIfRequired();
        if (external.tick(target)) {
            return;
        }
        CombatModeStrategy builtIn = activeStrategy;
        if (builtIn != null) {
            builtIn.tick(context, target);
        }
    }

    public boolean controlsNavigation(LivingEntity target) {
        Objects.requireNonNull(target, "target");
        if (closed) {
            return false;
        }
        transitionIfRequired();
        if (external.isActive()) {
            return external.controlsNavigation();
        }
        CombatModeStrategy builtIn = activeStrategy;
        return builtIn != null && builtIn.controlsNavigation(context, target);
    }

    public void suspend() {
        if (suspended) {
            return;
        }
        CombatModeStrategy builtIn = activeStrategy;
        if (builtIn != null) {
            builtIn.exit(context);
        }
        external.suspend();
        suspended = true;
    }

    public void recordShieldImpact(UUID attackerUUID) {
        Objects.requireNonNull(attackerUUID, "attackerUUID");
        if (!closed) {
            context.signals().recordShieldImpact(attackerUUID);
        }
    }

    public void refreshRegistration() {
        if (closed || !external.isActive() || activeMode == null) {
            return;
        }
        if (plugin.getExtensionRegistry().combatMode(activeMode).isPresent()) {
            return;
        }
        deactivate();
        options.setCombatMode(CombatMode.SWORD);
        transitionIfRequired();
    }

    public void deactivate() {
        CombatModeStrategy builtIn = activeStrategy;
        activeStrategy = null;
        if (builtIn != null && !suspended) {
            builtIn.exit(context);
        }
        external.close();
        activeMode = null;
        suspended = false;
    }

    private void transitionIfRequired() {
        CombatMode selected = resolveSupportedMode(options.getCombatMode());
        if (!selected.equals(options.getCombatMode())) {
            options.setCombatMode(selected);
        }
        if (external.isActive()
                && Objects.equals(activeMode, selected)
                && !plugin.getExtensionRegistry().combatMode(selected).isPresent()) {
            deactivate();
            selected = resolveSupportedMode(CombatMode.SWORD);
            options.setCombatMode(selected);
        }
        if (Objects.equals(activeMode, selected)) {
            return;
        }
        deactivate();
        activeMode = selected;
        CombatModeProvider externalProvider =
                plugin.getExtensionRegistry().combatMode(selected).orElse(null);
        if (externalProvider != null) {
            external.start(selected, externalProvider);
            return;
        }
        CombatModeStrategy selectedStrategy = strategies.get(selected);
        if (selectedStrategy == null) {
            selected = CombatMode.SWORD;
            options.setCombatMode(selected);
            activeMode = selected;
            selectedStrategy = strategies.get(CombatMode.SWORD);
        }
        if (selectedStrategy == null) {
            throw new IllegalStateException("No strategy registered for " + selected);
        }
        selectedStrategy.enter(context);
        activeStrategy = selectedStrategy;
    }

    private CombatMode resolveSupportedMode(CombatMode selected) {
        CombatMode checked = Objects.requireNonNull(selected, "selected");
        if (!checked.builtIn()) {
            return checked;
        }
        if (NMSBridgeManager.supportsCombatMode(checked) && strategies.containsKey(checked)) {
            return checked;
        }
        return CombatMode.SWORD;
    }

    private void resumeIfRequired() {
        if (!suspended) {
            return;
        }
        suspended = false;
        CombatModeStrategy builtIn = activeStrategy;
        if (builtIn != null) {
            builtIn.enter(context);
        }
        external.resume();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("combat mode engine is closed");
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
