package com.monkey.mcbot.update;

public record UpdateState(
        boolean updateAvailable,
        String currentVersion,
        String latestVersion,
        String downloadUrl,
        String message
) {
}
