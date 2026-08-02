package com.monkey.ultimatebot.update;

public record UpdateStartupResult(boolean checkFailed, boolean updateAvailable, String message) {}
