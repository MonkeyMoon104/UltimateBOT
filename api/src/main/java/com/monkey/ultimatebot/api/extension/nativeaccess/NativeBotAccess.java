package com.monkey.ultimatebot.api.extension.nativeaccess;

import java.util.Optional;

/** Explicit version-bound escape hatch for expert addons that require native server objects. */
public interface NativeBotAccess {
    String minecraftVersion();

    <T> T requireBotHandle(Class<T> type);

    <T> Optional<T> targetHandle(Class<T> type);

    <T> T requireLevelHandle(Class<T> type);

    <T> T requireBridge(Class<T> type);
}
