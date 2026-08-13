package com.monkey.ultimatebot.update;

public final class UpdateState {
    private final boolean updateAvailable;
    private final String currentVersion;
    private final String latestVersion;
    private final String downloadUrl;
    private final String message;

    public UpdateState(boolean updateAvailable, String currentVersion, String latestVersion, String downloadUrl, String message) {
        this.updateAvailable = updateAvailable;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
        this.message = message;
    }

    public boolean updateAvailable() {
        return updateAvailable;
    }
    public String currentVersion() {
        return currentVersion;
    }
    public String latestVersion() {
        return latestVersion;
    }
    public String downloadUrl() {
        return downloadUrl;
    }
    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UpdateState)) {
            return false;
        }
        UpdateState other = (UpdateState) obj;
        return updateAvailable == other.updateAvailable && java.util.Objects.equals(currentVersion, other.currentVersion) && java.util.Objects.equals(latestVersion, other.latestVersion) && java.util.Objects.equals(downloadUrl, other.downloadUrl) && java.util.Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(updateAvailable, currentVersion, latestVersion, downloadUrl, message);
    }

    @Override
    public String toString() {
        return "UpdateState[updateAvailable=" + updateAvailable + ", currentVersion=" + currentVersion + ", latestVersion=" + latestVersion + ", downloadUrl=" + downloadUrl + ", message=" + message + "]";
    }
}
