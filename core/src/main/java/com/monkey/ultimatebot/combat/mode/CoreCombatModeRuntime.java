package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.api.extension.combat.CombatModeRuntime;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.extension.runtime.CoreBotControl;
import com.monkey.ultimatebot.extension.runtime.CoreNativeBotAccess;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.function.Supplier;

final class CoreCombatModeRuntime implements CombatModeRuntime {
    private final CombatModeContext context;
    private final BotOptions options;
    private final Supplier<CombatMode> mode;
    private final CoreBotControl control;
    private final CoreNativeBotAccess nativeAccess;
    private final SplittableRandom random;

    CoreCombatModeRuntime(
            CombatModeContext context,
            BotOptions options,
            Supplier<CombatMode> mode,
            CoreBotControl control,
            CoreNativeBotAccess nativeAccess,
            SplittableRandom random) {
        this.context = Objects.requireNonNull(context, "context");
        this.options = Objects.requireNonNull(options, "options");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.control = Objects.requireNonNull(control, "control");
        this.nativeAccess = Objects.requireNonNull(nativeAccess, "nativeAccess");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public org.bukkit.entity.Player bot() {
        return context.bukkitBot();
    }

    @Override
    public CombatMode mode() {
        return Objects.requireNonNull(mode.get(), "mode");
    }

    @Override
    public DifficultyTier difficulty() {
        return DifficultyTier.valueOf(options.getDifficulty().name());
    }

    @Override
    public CoreBotControl control() {
        return control;
    }

    @Override
    public CoreNativeBotAccess nativeAccess() {
        return nativeAccess;
    }

    @Override
    public SplittableRandom random() {
        return random;
    }
}
