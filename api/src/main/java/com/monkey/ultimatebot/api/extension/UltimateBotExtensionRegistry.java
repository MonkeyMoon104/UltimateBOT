package com.monkey.ultimatebot.api.extension;

import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.common.model.brain.BrainKey;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import java.util.List;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

/** Public registry used by Paper plugins to install custom modes and bot brains. */
public interface UltimateBotExtensionRegistry {
    ExtensionRegistration registerCombatMode(Plugin owner, CombatModeProvider provider);

    ExtensionRegistration registerBrain(Plugin owner, BotBrainProvider provider);

    Optional<CombatModeProvider> combatMode(CombatMode mode);

    Optional<BotBrainProvider> brain(BrainKey key);

    List<CombatModeProvider> combatModes();

    List<BotBrainProvider> brains();
}
