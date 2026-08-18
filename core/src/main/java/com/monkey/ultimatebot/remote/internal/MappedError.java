package com.monkey.ultimatebot.remote.internal;

import org.jspecify.annotations.Nullable;

public final class MappedError {
    private final int status;
    private final String message;
    private final @Nullable String logMessage;

    public MappedError(int status, String message, @Nullable String logMessage) {
        this.status = status;
        this.message = message;
        this.logMessage = logMessage;
    }

    public int status() {
        return status;
    }

    public String message() {
        return message;
    }

    public @Nullable String logMessage() {
        return logMessage;
    }
}
