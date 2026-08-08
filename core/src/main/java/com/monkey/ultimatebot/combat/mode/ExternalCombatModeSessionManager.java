package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.api.extension.combat.CombatModeSession;
import com.monkey.ultimatebot.api.extension.combat.CombatModeTick;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.extension.runtime.CoreBotControl;
import com.monkey.ultimatebot.extension.runtime.CoreNativeBotAccess;
import com.monkey.ultimatebot.extension.runtime.ModeKitAdapter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.random.RandomGenerator;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

final class ExternalCombatModeSessionManager implements AutoCloseable {
    private static final int MAX_FAILURES = 3;

    private final UltimateBot plugin;
    private final BotOptions options;
    private final CombatModeContext context;
    private final CoreBotControl control;
    private final CoreNativeBotAccess nativeAccess;
    private final RandomGenerator random;
    private @Nullable CombatMode mode;
    private @Nullable CombatModeSession session;
    private boolean suspended;
    private boolean disabled;
    private int failures;
    private long sequence;

    ExternalCombatModeSessionManager(
            UltimateBot plugin,
            BotOptions options,
            CombatModeContext context,
            CoreBotControl control,
            CoreNativeBotAccess nativeAccess,
            RandomGenerator random) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.options = Objects.requireNonNull(options, "options");
        this.context = Objects.requireNonNull(context, "context");
        this.control = Objects.requireNonNull(control, "control");
        this.nativeAccess = Objects.requireNonNull(nativeAccess, "nativeAccess");
        this.random = Objects.requireNonNull(random, "random");
    }

    void start(CombatMode selectedMode, CombatModeProvider provider) {
        close();
        mode = Objects.requireNonNull(selectedMode, "selectedMode");
        CombatModeSession created = null;
        try {
            context.applyKit(ModeKitAdapter.toInternal(provider.descriptor().kit()));
            created = Objects.requireNonNull(
                    provider.create(new CoreCombatModeRuntime(
                            context,
                            options,
                            () -> Objects.requireNonNull(mode, "mode"),
                            control,
                            nativeAccess,
                            random)),
                    "combat mode provider returned null session");
            session = created;
            created.onEnter();
        } catch (RuntimeException | LinkageError error) {
            disabled = true;
            if (created != null) {
                closeSession();
            }
            plugin.getLogger().log(Level.WARNING, "Unable to start custom combat mode " + mode, error);
            options.setCombatMode(CombatMode.SWORD);
        }
    }

    boolean tick(LivingEntity target) {
        CombatModeSession active = session;
        if (active == null || disabled) {
            return false;
        }
        nativeAccess.target(Objects.requireNonNull(target, "target"));
        try {
            active.tick(new CombatModeTick(sequence++, target));
            failures = 0;
        } catch (RuntimeException | LinkageError error) {
            fail(error);
        }
        return true;
    }

    boolean controlsNavigation() {
        CombatModeSession active = session;
        if (active == null || disabled) {
            return false;
        }
        try {
            return active.controlsNavigation();
        } catch (RuntimeException | LinkageError error) {
            fail(error);
            return false;
        }
    }

    boolean isActive() {
        return session != null && !disabled;
    }

    void suspend() {
        CombatModeSession active = session;
        if (suspended || active == null) {
            return;
        }
        try {
            active.onExit();
        } catch (RuntimeException | LinkageError error) {
            fail(error);
        }
        suspended = true;
    }

    void resume() {
        CombatModeSession active = session;
        if (!suspended || active == null || disabled) {
            return;
        }
        suspended = false;
        try {
            active.onEnter();
        } catch (RuntimeException | LinkageError error) {
            fail(error);
        }
    }

    private void fail(Throwable error) {
        failures++;
        plugin.getLogger()
                .log(
                        Level.WARNING,
                        "Custom combat mode " + mode + " failed (" + failures + '/' + MAX_FAILURES + ')',
                        error);
        if (failures >= MAX_FAILURES) {
            disabled = true;
            closeSession();
            control.stopMovement();
            options.setCombatMode(CombatMode.SWORD);
            mode = null;
        }
    }

    private void closeSession() {
        CombatModeSession active = session;
        session = null;
        nativeAccess.clearTarget();
        if (active == null) {
            return;
        }
        if (!suspended) {
            try {
                active.onExit();
            } catch (RuntimeException | LinkageError error) {
                plugin.getLogger().log(Level.WARNING, "Unable to exit custom combat mode " + mode, error);
            }
        }
        try {
            active.close();
        } catch (Exception | LinkageError error) {
            plugin.getLogger().log(Level.WARNING, "Unable to close custom combat mode " + mode, error);
        }
    }

    @Override
    public void close() {
        closeSession();
        mode = null;
        suspended = false;
        disabled = false;
        failures = 0;
        sequence = 0L;
    }
}
