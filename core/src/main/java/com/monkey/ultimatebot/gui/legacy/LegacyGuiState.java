package com.monkey.ultimatebot.gui.legacy;

public final class LegacyGuiState {

    private int activeTab;
    private boolean closing;
    private boolean listenerRegistered;

    public LegacyGuiState(int initialActiveTab) {
        this.activeTab = initialActiveTab;
    }

    public int activeTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }

    public boolean closing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public boolean listenerRegistered() {
        return listenerRegistered;
    }

    public void setListenerRegistered(boolean listenerRegistered) {
        this.listenerRegistered = listenerRegistered;
    }
}
