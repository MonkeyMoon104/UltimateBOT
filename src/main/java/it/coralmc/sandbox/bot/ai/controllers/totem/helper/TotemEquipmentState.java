package it.coralmc.sandbox.bot.ai.controllers.totem.helper;

public class TotemEquipmentState {
    private final boolean hasOffhandTotem;
    private final boolean hasMainhandTotem;
    private final int equippedTotems;

    public TotemEquipmentState(boolean hasOffhandTotem, boolean hasMainhandTotem, int equippedTotems) {
        this.hasOffhandTotem = hasOffhandTotem;
        this.hasMainhandTotem = hasMainhandTotem;
        this.equippedTotems = equippedTotems;
    }

    public boolean hasOffhandTotem() {
        return hasOffhandTotem;
    }

    public boolean hasMainhandTotem() {
        return hasMainhandTotem;
    }

    public int getEquippedTotems() {
        return equippedTotems;
    }
}