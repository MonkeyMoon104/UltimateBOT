package com.monkey.ultimatebot.update;

public final class UpdateStartupResult {
    private final boolean checkFailed;
    private final boolean updateAvailable;
    private final String message;

    public UpdateStartupResult(boolean checkFailed, boolean updateAvailable, String message) {
        this.checkFailed = checkFailed;
        this.updateAvailable = updateAvailable;
        this.message = message;
    }

    public boolean checkFailed() {
        return checkFailed;
    }
    public boolean updateAvailable() {
        return updateAvailable;
    }
    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UpdateStartupResult)) {
            return false;
        }
        UpdateStartupResult other = (UpdateStartupResult) obj;
        return checkFailed == other.checkFailed && updateAvailable == other.updateAvailable && java.util.Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(checkFailed, updateAvailable, message);
    }

    @Override
    public String toString() {
        return "UpdateStartupResult[checkFailed=" + checkFailed + ", updateAvailable=" + updateAvailable + ", message=" + message + "]";
    }
}
