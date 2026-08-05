package com.monkey.ultimatebot.addon.runtime;

public final class AddonLoadException extends Exception {
    private static final long serialVersionUID = 1L;

    public AddonLoadException(String message) {
        super(message);
    }

    public AddonLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
