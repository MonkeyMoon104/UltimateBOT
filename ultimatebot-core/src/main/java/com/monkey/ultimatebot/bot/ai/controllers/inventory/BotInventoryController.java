package com.monkey.ultimatebot.bot.ai.controllers.inventory;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.EquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.ItemChecker;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.ItemManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.ResourceReplenisher;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.SlotManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IItemChecker;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IItemManager;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BotInventoryController {

    private final Player bot;
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

    public BotInventoryController(Player bot) {
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

    public ItemStack getEquipment(EquipmentSlot slot) {
        return bot.getItemBySlot(java.util.Objects.requireNonNull(slot, "slot"));
    }

    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        EquipmentSlot checkedSlot = Objects.requireNonNull(slot, "slot");
        ItemStack checkedItem = Objects.requireNonNull(item, "item");
        if (ItemStack.matches(bot.getItemBySlot(checkedSlot), checkedItem)) {
            return;
        }
        bot.setItemSlot(checkedSlot, checkedItem.copy());
        equipmentBroadcaster.broadcastEquipmentChange(bot);
    }

    public void applyHotbarLoadout(Map<Integer, ItemStack> loadout, int selectedSlot) {
        slotManager.applyLoadout(Objects.requireNonNull(loadout, "loadout"), selectedSlot);
    }

    public void applyEquipmentLoadout(Map<EquipmentSlot, ItemStack> loadout) {
        Map<EquipmentSlot, ItemStack> checkedLoadout = Objects.requireNonNull(loadout, "loadout");
        boolean changed = false;
        for (Map.Entry<EquipmentSlot, ItemStack> entry : checkedLoadout.entrySet()) {
            EquipmentSlot slot = Objects.requireNonNull(entry.getKey(), "equipment slot");
            ItemStack item = Objects.requireNonNull(entry.getValue(), "equipment item");
            if (!ItemStack.matches(bot.getItemBySlot(slot), item)) {
                bot.setItemSlot(slot, item.copy());
                changed = true;
            }
        }
        if (changed) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    public void startUsingItem(InteractionHand hand) {
        InteractionHand checkedHand = Objects.requireNonNull(hand, "hand");
        if (bot.isUsingItem() && bot.getUsedItemHand() == checkedHand) {
            return;
        }
        if (bot.isUsingItem()) {
            bot.releaseUsingItem();
        }
        bot.startUsingItem(checkedHand);
        equipmentBroadcaster.broadcastMetadataChange(bot);
    }

    public void releaseUsingItem() {
        if (!bot.isUsingItem()) {
            return;
        }
        bot.releaseUsingItem();
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
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!resourceReplenisher.hasInfiniteResources()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                setItem(slot, ItemStack.EMPTY);
            } else {
                setItem(slot, stack);
            }
        }
        return true;
    }

    public int getItemCount(Item item) {
        return itemChecker.getItemCount(slotManager.getHotbarSlots(), item, resourceReplenisher.hasInfiniteResources());
    }

    public boolean hasItem(Item item) {
        return itemChecker.hasItem(slotManager.getHotbarSlots(), item, resourceReplenisher.hasInfiniteResources());
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
