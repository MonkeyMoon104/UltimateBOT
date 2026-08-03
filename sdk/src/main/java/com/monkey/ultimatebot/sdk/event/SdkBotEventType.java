package com.monkey.ultimatebot.sdk.event;

/** Event types available through the remote SSE stream. */
public enum SdkBotEventType {
    SPAWNED,
    SPAWN_ACCEPTED,
    DESPAWNED,
    DESPAWN_ACCEPTED,
    DIED,
    KILLED_ENTITY,
    TARGET_CHANGED,
    SETTINGS_CHANGE_ACCEPTED,
    ATTACK_STARTED,
    EXPLOSION_PREPARED,
    DAMAGE_ACCEPTED,
    HEAL_ACCEPTED,
    TELEPORT_ACCEPTED,
    TOTEM_USED
}
