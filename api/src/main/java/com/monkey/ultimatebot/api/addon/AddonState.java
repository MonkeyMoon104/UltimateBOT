package com.monkey.ultimatebot.api.addon;

/** Observable lifecycle state of an installed addon. */
public enum AddonState {
    DISCOVERED,
    LOADED,
    ENABLED,
    FAILED,
    DISABLED
}
