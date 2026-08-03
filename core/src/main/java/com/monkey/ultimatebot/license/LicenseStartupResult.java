package com.monkey.ultimatebot.license;

public record LicenseStartupResult(boolean allowed, boolean graceMode, String reasonCode, String message) {

    public static LicenseStartupResult allowed(boolean graceMode, String message) {
        return new LicenseStartupResult(true, graceMode, null, message);
    }

    public static LicenseStartupResult denied(String reasonCode, String message) {
        return new LicenseStartupResult(false, false, reasonCode, message);
    }
}
