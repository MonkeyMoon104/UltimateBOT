package com.monkey.ultimatebot.common.model;

/** Closed set of bot subsystems that a custom brain may own. */
public enum BrainCapability {
    TARGETING,
    NAVIGATION,
    MOVEMENT,
    ROTATION,
    COMBAT,
    INVENTORY,
    WORLD_INTERACTION,
    FULL_CONTROL
}
