package com.monkey.ultimatebot.api.extension.brain;

import com.monkey.ultimatebot.api.extension.control.BotControl;
import com.monkey.ultimatebot.api.extension.nativeaccess.NativeBotAccess;
import java.util.UUID;
import java.util.SplittableRandom;
import org.bukkit.entity.Player;

/** Stable per-bot services supplied to a custom brain factory. */
public interface BotBrainContext {
    UUID botUUID();

    Player bot();

    BotControl control();

    NativeBotAccess nativeAccess();

    SplittableRandom random();
}
