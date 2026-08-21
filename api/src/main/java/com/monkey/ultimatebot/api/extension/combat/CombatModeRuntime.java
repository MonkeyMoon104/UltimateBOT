package com.monkey.ultimatebot.api.extension.combat;

import com.monkey.ultimatebot.api.extension.control.BotControl;
import com.monkey.ultimatebot.api.extension.nativeaccess.NativeBotAccess;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import java.util.SplittableRandom;
import org.bukkit.entity.Player;

/** Per-bot services supplied when a custom combat-mode session is created. */
public interface CombatModeRuntime {
    Player bot();

    CombatMode mode();

    DifficultyTier difficulty();

    BotControl control();

    NativeBotAccess nativeAccess();

    SplittableRandom random();
}
