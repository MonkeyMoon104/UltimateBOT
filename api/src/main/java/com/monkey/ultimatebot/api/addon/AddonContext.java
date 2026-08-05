package com.monkey.ultimatebot.api.addon;

import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.monkey.ultimatebot.api.extension.ExtensionRegistration;
import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import java.nio.file.Path;
import java.util.logging.Logger;

/** Scoped services supplied to one externally loaded addon. */
public interface AddonContext {
    String addonId();

    UltimateBotAPI api();

    Path dataDirectory();

    Logger logger();

    AddonResourceScope resources();

    ExtensionRegistration registerCombatMode(CombatModeProvider provider);

    ExtensionRegistration registerBrain(BotBrainProvider provider);
}
