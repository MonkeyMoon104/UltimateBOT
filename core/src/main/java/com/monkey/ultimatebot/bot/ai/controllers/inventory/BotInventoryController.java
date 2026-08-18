package com.monkey.ultimatebot.bot.ai.controllers.inventory;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.EquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.ItemChecker;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.ItemManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.ResourceReplenisher;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.SlotManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IItemChecker;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IItemManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BotInventoryController {

    private final ITrainingBot bot;
    private final SlotManager slotManager;
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

    public BotInventoryController(ITrainingBot bot) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.resourceReplenisher = new ResourceReplenisher();
        this.equipmentBroadcaster = new EquipmentBroadcaster();
        this.slotManager = new SlotManager(bot, resourceReplenisher, equipmentBroadcaster);
        this.itemChecker = new ItemChecker();
        this.itemManager = new ItemManager(
                bot, slotManager.getHotbarSlots(), resourceReplenisher, equipmentBroadcaster, slotManager);
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
        return itemChecker.hasEnderpearls(slotManager.getHotbarSlots(), resourceReplenisher.hasInfiniteResources());
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

    public ItemStack getBukkitItem(int slot) {
        return getItem(slot);
    }

    public ItemStack getEquipment(EquipmentSlotKind slot) {
        return bot.getItem(java.util.Objects.requireNonNull(slot, "slot"));
    }

    public ItemStack getBukkitEquipment(EquipmentSlotKind slot) {
        return getEquipment(slot);
    }

    public void setEquipment(EquipmentSlotKind slot, ItemStack item) {
        EquipmentSlotKind checkedSlot = Objects.requireNonNull(slot, "slot");
        ItemStack checkedItem = Objects.requireNonNull(item, "item");
        ItemStack current = bot.getItem(checkedSlot);
        if (current.isSimilar(checkedItem) && current.getAmount() == checkedItem.getAmount()) {
            return;
        }
        bot.setItem(checkedSlot, checkedItem.clone());
        equipmentBroadcaster.broadcastEquipmentChange(bot);
    }

    public void applyHotbarLoadout(Map<Integer, ItemStack> loadout, int selectedSlot) {
        slotManager.applyLoadout(Objects.requireNonNull(loadout, "loadout"), selectedSlot);
    }

    public void applyEquipmentLoadout(Map<EquipmentSlotKind, ItemStack> loadout) {
        Map<EquipmentSlotKind, ItemStack> checkedLoadout = Objects.requireNonNull(loadout, "loadout");
        boolean changed = false;
        for (Map.Entry<EquipmentSlotKind, ItemStack> entry : checkedLoadout.entrySet()) {
            EquipmentSlotKind slot = Objects.requireNonNull(entry.getKey(), "equipment slot");
            ItemStack item = Objects.requireNonNull(entry.getValue(), "equipment item");
            ItemStack current = bot.getItem(slot);
            if (!current.isSimilar(item) || current.getAmount() != item.getAmount()) {
                bot.setItem(slot, item.clone());
                changed = true;
            }
        }
        if (changed) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    public void applyBukkitHotbarLoadout(Map<Integer, ItemStack> loadout, int selectedSlot) {
        applyHotbarLoadout(loadout, selectedSlot);
    }

    public void applyBukkitEquipmentLoadout(Map<EquipmentSlotKind, ItemStack> loadout) {
        applyEquipmentLoadout(loadout);
    }

    public void startUsingItem(EquipmentSlotKind hand) {
        EquipmentSlotKind checkedHand = Objects.requireNonNull(hand, "hand");
        ItemStack heldItem = bot.getItem(checkedHand);
        if (bot.isUsingItem() && bot.activeItemStack().isSimilar(heldItem)) {
            return;
        }
        if (bot.isUsingItem()) {
            bot.stopUsingItem();
        }
        bot.beginUsingItem(checkedHand);
        equipmentBroadcaster.broadcastMetadataChange(bot);
    }

    public void startUsingMainHand() {
        startUsingItem(EquipmentSlotKind.HAND);
    }

    public void startUsingOffHand() {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand != null) {
            startUsingItem(offHand);
        }
    }

    public void releaseUsingItem() {
        if (!bot.isUsingItem()) {
            return;
        }
        bot.stopUsingItem();
        equipmentBroadcaster.broadcastMetadataChange(bot);
    }

    public boolean isHoldingSword() {
        return itemChecker.isHoldingSword(slotManager.getCurrentSlot(), slotManager.getHotbarSlots());
    }

    public boolean isHoldingEnderpearl() {
        return itemChecker.isHoldingEnderpearl(slotManager.getCurrentSlot(), slotManager.getHotbarSlots());
    }

    public boolean isHoldingObsidian() {
        return itemChecker.isHoldingObsidian(slotManager.getCurrentSlot(), slotManager.getHotbarSlots());
    }

    public boolean isHoldingCrystal() {
        return itemChecker.isHoldingCrystal(slotManager.getCurrentSlot(), slotManager.getHotbarSlots());
    }

    public boolean isHoldingAnchor() {
        return itemChecker.isHoldingAnchor(slotManager.getCurrentSlot(), slotManager.getHotbarSlots());
    }

    public boolean isHoldingGlow() {
        return itemChecker.isHoldingGlow(slotManager.getCurrentSlot(), slotManager.getHotbarSlots());
    }

    public void updateTotemSlot(ItemStack totemStack) {
        itemManager.updateTotemSlot(totemStack);
    }

    public void onItemUsed(int slot) {
        itemManager.onItemUsed(slot);
    }

    public boolean consumeItem(int slot) {
        ItemStack stack = getItem(slot);
        if (stack == null || ItemStackAccess.isEmpty(stack)) {
            return false;
        }
        if (!resourceReplenisher.hasInfiniteResources()) {
            stack.setAmount(stack.getAmount() - 1);
            if (ItemStackAccess.isEmpty(stack)) {
                setItem(slot, ItemStackAccess.empty());
            } else {
                setItem(slot, stack);
            }
        }
        return true;
    }

    public int getItemCount(Material material) {
        return itemChecker.getItemCount(
                slotManager.getHotbarSlots(), material, resourceReplenisher.hasInfiniteResources());
    }

    public boolean hasItem(Material material) {
        return itemChecker.hasItem(slotManager.getHotbarSlots(), material, resourceReplenisher.hasInfiniteResources());
    }

    public void setInfiniteResources(boolean infinite) {
        resourceReplenisher.setInfiniteResources(infinite);
        if (infinite) {
            resourceReplenisher.replenishAllItems(slotManager.getHotbarSlots());
        }
    }

    public boolean hasInfiniteResources() {
        return resourceReplenisher.hasInfiniteResources();
    }

    public void tick() {
        if (resourceReplenisher.hasInfiniteResources()) {
            resourceReplenisher.replenishAllItems(slotManager.getHotbarSlots());
        }
    }
}
