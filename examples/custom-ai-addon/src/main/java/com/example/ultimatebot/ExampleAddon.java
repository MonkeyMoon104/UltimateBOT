package com.example.ultimatebot;

import com.monkey.ultimatebot.api.addon.AddonContext;
import com.monkey.ultimatebot.api.addon.UltimateBotAddon;
import java.util.Objects;

public final class ExampleAddon implements UltimateBotAddon {
    @Override
    public void onLoad(AddonContext context) {
        AddonContext checkedContext = Objects.requireNonNull(context, "context");
        checkedContext.registerBrain(new ExampleBrainProvider());
        checkedContext.registerCombatMode(new ExampleModeProvider());
    }
}
