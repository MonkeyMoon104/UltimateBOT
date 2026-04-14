package com.monkey.mcbot.update;

public record UpdateStartupResult(
        boolean checkFailed,
        boolean updateAvailable,
        String message
) {
}
