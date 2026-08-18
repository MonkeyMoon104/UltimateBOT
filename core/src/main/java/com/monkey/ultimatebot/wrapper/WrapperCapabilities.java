package com.monkey.ultimatebot.wrapper;

public final class WrapperCapabilities {
    private final boolean foliaDetected;
    private final boolean globalRegionScheduler;
    private final boolean asyncScheduler;
    private final boolean entityScheduler;

    public WrapperCapabilities(
            boolean foliaDetected, boolean globalRegionScheduler, boolean asyncScheduler, boolean entityScheduler) {
        this.foliaDetected = foliaDetected;
        this.globalRegionScheduler = globalRegionScheduler;
        this.asyncScheduler = asyncScheduler;
        this.entityScheduler = entityScheduler;
    }

    public boolean foliaDetected() {
        return foliaDetected;
    }

    public boolean globalRegionScheduler() {
        return globalRegionScheduler;
    }

    public boolean asyncScheduler() {
        return asyncScheduler;
    }

    public boolean entityScheduler() {
        return entityScheduler;
    }

    public String summary() {
        return "folia=" + foliaDetected
                + ", global=" + globalRegionScheduler
                + ", async=" + asyncScheduler
                + ", entity=" + entityScheduler;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WrapperCapabilities)) {
            return false;
        }
        WrapperCapabilities other = (WrapperCapabilities) obj;
        return foliaDetected == other.foliaDetected
                && globalRegionScheduler == other.globalRegionScheduler
                && asyncScheduler == other.asyncScheduler
                && entityScheduler == other.entityScheduler;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(foliaDetected, globalRegionScheduler, asyncScheduler, entityScheduler);
    }

    @Override
    public String toString() {
        return "WrapperCapabilities[foliaDetected=" + foliaDetected + ", globalRegionScheduler=" + globalRegionScheduler
                + ", asyncScheduler=" + asyncScheduler + ", entityScheduler=" + entityScheduler + "]";
    }
}
