package com.monkey.ultimatebot.wrapper;

public record WrapperCapabilities(
        boolean foliaDetected, boolean globalRegionScheduler, boolean asyncScheduler, boolean entityScheduler) {

    public String summary() {
        return "folia=" + foliaDetected
                + ", global=" + globalRegionScheduler
                + ", async=" + asyncScheduler
                + ", entity=" + entityScheduler;
    }
}
