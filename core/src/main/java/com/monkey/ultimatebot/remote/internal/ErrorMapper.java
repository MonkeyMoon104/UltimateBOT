package com.monkey.ultimatebot.remote.internal;

public final class ErrorMapper {
    public MappedError map(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            String message = ex.getMessage() != null ? ex.getMessage() : "Invalid remote API request";
            return new MappedError(400, message, null);
        }
        return new MappedError(500, "Internal remote API error.", "Remote API request failed: " + ex.getMessage());
    }
}
