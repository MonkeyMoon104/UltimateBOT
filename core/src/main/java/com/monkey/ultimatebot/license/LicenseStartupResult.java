package com.monkey.ultimatebot.license;

import org.jspecify.annotations.Nullable;

public final class LicenseStartupResult {
    private final boolean allowed;
    private final boolean graceMode;
    private final @Nullable String reasonCode;
    private final String message;

    public LicenseStartupResult(boolean allowed, boolean graceMode, @Nullable String reasonCode, String message) {
        this.allowed = allowed;
        this.graceMode = graceMode;
        this.reasonCode = reasonCode;
        this.message = message;
    }

    public boolean allowed() {
        return allowed;
    }
    public boolean graceMode() {
        return graceMode;
    }
    public @Nullable String reasonCode() {
        return reasonCode;
    }
    public String message() {
        return message;
    }

    public static LicenseStartupResult allowed(boolean graceMode, String message) {
        return new LicenseStartupResult(true, graceMode, null, message);
    }

    public static LicenseStartupResult denied(String reasonCode, String message) {
        return new LicenseStartupResult(false, false, reasonCode, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LicenseStartupResult)) {
            return false;
        }
        LicenseStartupResult other = (LicenseStartupResult) obj;
        return allowed == other.allowed && graceMode == other.graceMode && java.util.Objects.equals(reasonCode, other.reasonCode) && java.util.Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(allowed, graceMode, reasonCode, message);
    }

    @Override
    public String toString() {
        return "LicenseStartupResult[allowed=" + allowed + ", graceMode=" + graceMode + ", reasonCode=" + reasonCode + ", message=" + message + "]";
    }
}
