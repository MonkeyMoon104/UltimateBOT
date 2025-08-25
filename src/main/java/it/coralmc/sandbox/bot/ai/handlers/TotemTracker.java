package it.coralmc.sandbox.bot.ai.handlers;

import it.coralmc.sandbox.bot.ai.TrainingBot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class TotemTracker {

    private final TrainingBot bot;
    private int totemCount = -1;
    private int previousEquippedTotems = 0;
    private boolean skipNextTotemTracking = false;

    public TotemTracker(TrainingBot bot) {
        this.bot = bot;
    }

    public void onTick() {
        ItemStack offhand = bot.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = bot.getItemBySlot(EquipmentSlot.MAINHAND);

        boolean hasOffhandTotem = offhand != null && !offhand.isEmpty() &&
                offhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
        boolean hasMainhandTotem = mainhand != null && !mainhand.isEmpty() &&
                mainhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);

        int currentEquippedTotems = (hasOffhandTotem ? 1 : 0) + (hasMainhandTotem ? 1 : 0);

        if (!skipNextTotemTracking && previousEquippedTotems > currentEquippedTotems && totemCount > 0) {
            int consumedTotems = previousEquippedTotems - currentEquippedTotems;
            if (totemCount != -1) {
                totemCount = Math.max(0, totemCount - consumedTotems);
            }
        }

        skipNextTotemTracking = false;
        previousEquippedTotems = currentEquippedTotems;

        bot.getAiController().getBotAI().manageTotem();
    }

    public int getTotemCount() { return totemCount; }
    public void setTotemCount(int count) {
        this.totemCount = count;
        this.skipNextTotemTracking = true;
    }
}
