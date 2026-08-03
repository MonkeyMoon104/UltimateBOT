package com.monkey.ultimatebot.license;

import org.jspecify.annotations.Nullable;

public record LicenseStartupResult(
        boolean allowed, boolean graceMode, @Nullable String reasonCode, String message) {

    public static LicenseStartupResult allowed(boolean graceMode, String message) {
        return new LicenseStartupResult(true, graceMode, null, message);
    }

    public static LicenseStartupResult denied(String reasonCode, String message) {
        return new LicenseStartupResult(false, false, reasonCode, message);
    }
}
