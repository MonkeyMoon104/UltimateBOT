package com.monkey.ultimatebot.api.addon;

/** Main entry point implemented by jars installed in UltimateBot's addon directory. */
public interface UltimateBotAddon {
    default void onLoad(AddonContext context) throws Exception {}

    default void onEnable() throws Exception {}

    default void onDisable() throws Exception {}
}
