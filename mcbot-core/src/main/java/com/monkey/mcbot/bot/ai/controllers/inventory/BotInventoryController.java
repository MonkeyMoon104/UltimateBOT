package com.monkey.mcbot.bot.ai.controllers.inventory;

import com.monkey.mcbot.bot.ai.controllers.inventory.helper.*;
import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BotInventoryController {

    private final Player bot;
    private final ISlotManager slotManager;
    private final IResourceReplenisher resourceReplenisher;
    private final IEquipmentBroadcaster equipmentBroadcaster;
    private final IItemChecker itemChecker;
    private final IItemManager itemManager;

    public static final int SWORD_SLOT = 0;
    public static final int ENDERPEARL_SLOT = 1;
    public static final int TOTEM_SLOT = 2;
    public static final int OBSIDIAN_SLOT = 3;
    public static final int CRYSTAL_SLOT = 4;
    public static final int ANCHOR_SLOT = 5;
    public static final int GLOW_SLOT = 6;
    public static final int GOLDEN_APPLE_SLOT = 7;
    public static final int EMPTY_SLOT = 8;

    public BotInventoryController(Player bot) {
        this.bot = bot;
        this.resourceReplenisher = new ResourceReplenisher();
        this.equipmentBroadcaster = new EquipmentBroadcaster();
        this.slotManager = new SlotManager(bot, resourceReplenisher, equipmentBroadcaster);
        this.itemChecker = new ItemChecker();
        this.itemManager = new ItemManager(bot, ((SlotManager) slotManager).getHotbarSlots(), resourceReplenisher, equipmentBroadcaster, slotManager.getCurrentSlot());
    }

    public void switchToSlot(int slot) {
        slotManager.switchToSlot(slot);
    }

    public void switchToSword() {
        slotManager.switchToSword();
    }

    public void switchToEnderpearl() {
        slotManager.switchToEnderpearl();
    }

    public void switchToTotem() {
        slotManager.switchToTotem();
    }

    public void switchToCrystal() {
        slotManager.switchToCrystal();
    }

    public void switchToObs() {
        slotManager.switchToObs();
    }

    public void switchToAnchor() {
        slotManager.switchToAnchor();
    }

    public void switchToGlow() {
        slotManager.switchToGlow();
    }

    public void switchToGoldenApple() {
        slotManager.switchToGoldenApple();
    }

    public void switchToEmptySlot() {
        slotManager.switchToEmptySlot();
    }

    public int getCurrentSlot() {
        return slotManager.getCurrentSlot();
    }

    public ItemStack getCurrentItem() {
        return slotManager.getCurrentItem();
    }

    public boolean hasEnderpearls() {
        return itemChecker.hasEnderpearls(((SlotManager) slotManager).getHotbarSlots(), resourceReplenisher.hasInfiniteResources());
    }

    public void addEnderpearls(int count) {
        itemManager.addEnderpearls(count);
    }

    public void setItem(int slot, ItemStack item) {
        slotManager.setItem(slot, item);
    }

    public ItemStack getItem(int slot) {
        return slotManager.getItem(slot);
    }

    public boolean isHoldingSword() {
        return itemChecker.isHoldingSword(slotManager.getCurrentSlot(), ((SlotManager) slotManager).getHotbarSlots());
    }

    public boolean isHoldingEnderpearl() {
        return itemChecker.isHoldingEnderpearl(slotManager.getCurrentSlot(), ((SlotManager) slotManager).getHotbarSlots());
    }

    public boolean isHoldingObsidian() {
        return itemChecker.isHoldingObsidian(slotManager.getCurrentSlot(), ((SlotManager) slotManager).getHotbarSlots());
    }

    public boolean isHoldingCrystal() {
        return itemChecker.isHoldingCrystal(slotManager.getCurrentSlot(), ((SlotManager) slotManager).getHotbarSlots());
    }

    public boolean isHoldingAnchor() {
        return itemChecker.isHoldingAnchor(slotManager.getCurrentSlot(), ((SlotManager) slotManager).getHotbarSlots());
    }

    public boolean isHoldingGlow() {
        return itemChecker.isHoldingGlow(slotManager.getCurrentSlot(), ((SlotManager) slotManager).getHotbarSlots());
    }

    public void updateTotemSlot(ItemStack totemStack) {
        itemManager.updateTotemSlot(totemStack);
    }

    public void onItemUsed(int slot) {
        itemManager.onItemUsed(slot);
    }

    public int getItemCount(Item item) {
        return itemChecker.getItemCount(((SlotManager) slotManager).getHotbarSlots(), item, resourceReplenisher.hasInfiniteResources());
    }

    public boolean hasItem(Item item) {
        return itemChecker.hasItem(((SlotManager) slotManager).getHotbarSlots(), item, resourceReplenisher.hasInfiniteResources());
    }

    public void setInfiniteResources(boolean infinite) {
        resourceReplenisher.setInfiniteResources(infinite);
        if (infinite) {
            resourceReplenisher.replenishAllItems(((SlotManager) slotManager).getHotbarSlots());
        }
    }

    public boolean hasInfiniteResources() {
        return resourceReplenisher.hasInfiniteResources();
    }

    public void tick() {
        if (resourceReplenisher.hasInfiniteResources()) {
            resourceReplenisher.replenishAllItems(((SlotManager) slotManager).getHotbarSlots());
        }
    }
}