package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.ISlotManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class SlotManager implements ISlotManager {

    private final ITrainingBot bot;
    private final Map<Integer, ItemStack> hotbarSlots = new HashMap<>();
    private int currentSlot = -1;

    public static final int SWORD_SLOT = 0;
    public static final int ENDERPEARL_SLOT = 1;
    public static final int TOTEM_SLOT = 2;
    public static final int OBSIDIAN_SLOT = 3;
    public static final int CRYSTAL_SLOT = 4;
    public static final int ANCHOR_SLOT = 5;
    public static final int GLOW_SLOT = 6;
    public static final int GOLDEN_APPLE_SLOT = 7;
    public static final int EMPTY_SLOT = 8;

    private final IResourceReplenisher resourceReplenisher;
    private final IEquipmentBroadcaster equipmentBroadcaster;

    public SlotManager(
            ITrainingBot bot, IResourceReplenisher resourceReplenisher, IEquipmentBroadcaster equipmentBroadcaster) {
        this.bot = java.util.Objects.requireNonNull(bot, "bot");
        this.resourceReplenisher = java.util.Objects.requireNonNull(resourceReplenisher, "resourceReplenisher");
        this.equipmentBroadcaster = java.util.Objects.requireNonNull(equipmentBroadcaster, "equipmentBroadcaster");
        initializeDefaultItems();
    }

    private void initializeDefaultItems() {
        hotbarSlots.put(SWORD_SLOT, createDefaultSword());
        hotbarSlots.put(ENDERPEARL_SLOT, new ItemStack(Material.ENDER_PEARL, 16));
        hotbarSlots.put(TOTEM_SLOT, new ItemStack(Material.TOTEM_OF_UNDYING));
        hotbarSlots.put(OBSIDIAN_SLOT, new ItemStack(Material.OBSIDIAN, 64));
        hotbarSlots.put(CRYSTAL_SLOT, new ItemStack(Material.END_CRYSTAL, 64));
        hotbarSlots.put(ANCHOR_SLOT, new ItemStack(Material.RESPAWN_ANCHOR, 64));
        hotbarSlots.put(GLOW_SLOT, new ItemStack(Material.GLOWSTONE, 64));
        hotbarSlots.put(GOLDEN_APPLE_SLOT, new ItemStack(Material.GOLDEN_APPLE, 64));
        hotbarSlots.put(EMPTY_SLOT, ItemStack.empty());
        switchToSlot(SWORD_SLOT);
    }

    private ItemStack createDefaultSword() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
        return sword;
    }

    @Override
    public void switchToSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        if (!hotbarSlots.containsKey(slot)) return;

        resourceReplenisher.replenishItem(hotbarSlots, slot);
        ItemStack item = hotbarSlots.get(slot);
        if (currentSlot == slot && bot.getItem(EquipmentSlot.HAND).isSimilar(item)) {
            return;
        }

        currentSlot = slot;
        bot.setItem(EquipmentSlot.HAND, item);

        equipmentBroadcaster.broadcastEquipmentChange(bot);
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot <= 8) {
            ItemStack current = hotbarSlots.getOrDefault(slot, ItemStack.empty());
            if (current.isSimilar(item) && current.getAmount() == item.getAmount()) {
                return;
            }
            hotbarSlots.put(slot, item.clone());

            if (slot == currentSlot) {
                bot.setItem(EquipmentSlot.HAND, item);
                equipmentBroadcaster.broadcastEquipmentChange(bot);
            }
        }
    }

    public void applyLoadout(Map<Integer, ItemStack> loadout, int selectedSlot) {
        Map<Integer, ItemStack> checkedLoadout = java.util.Objects.requireNonNull(loadout, "loadout");
        if (selectedSlot < 0 || selectedSlot > 8) {
            throw new IllegalArgumentException("selectedSlot must be between 0 and 8");
        }

        for (int slot = 0; slot <= 8; slot++) {
            ItemStack item = java.util.Objects.requireNonNull(
                    checkedLoadout.getOrDefault(slot, ItemStack.empty()), "loadout item at slot " + slot);
            hotbarSlots.put(slot, item.clone());
        }

        ItemStack selectedItem =
                java.util.Objects.requireNonNull(hotbarSlots.get(selectedSlot), "selected hotbar item");
        ItemStack currentMainHand = bot.getItem(EquipmentSlot.HAND);
        boolean visualChange = !currentMainHand.isSimilar(selectedItem)
                || currentMainHand.getAmount() != selectedItem.getAmount();
        currentSlot = selectedSlot;
        if (visualChange) {
            bot.setItem(EquipmentSlot.HAND, selectedItem);
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        return hotbarSlots.getOrDefault(slot, ItemStack.empty());
    }

    @Override
    public int getCurrentSlot() {
        return currentSlot;
    }

    @Override
    public ItemStack getCurrentItem() {
        ItemStack current = hotbarSlots.get(currentSlot);

        if (current != null && !current.isEmpty()) {
            resourceReplenisher.replenishItem(hotbarSlots, currentSlot);
        }

        return current == null ? ItemStack.empty() : current;
    }

    @Override
    public void switchToSword() {
        switchToSlot(SWORD_SLOT);
    }

    @Override
    public void switchToEnderpearl() {
        switchToSlot(ENDERPEARL_SLOT);
    }

    @Override
    public void switchToTotem() {
        switchToSlot(TOTEM_SLOT);
    }

    @Override
    public void switchToCrystal() {
        switchToSlot(CRYSTAL_SLOT);
    }

    @Override
    public void switchToObs() {
        switchToSlot(OBSIDIAN_SLOT);
    }

    @Override
    public void switchToAnchor() {
        switchToSlot(ANCHOR_SLOT);
    }

    @Override
    public void switchToGlow() {
        switchToSlot(GLOW_SLOT);
    }

    @Override
    public void switchToGoldenApple() {
        switchToSlot(GOLDEN_APPLE_SLOT);
    }

    @Override
    public void switchToEmptySlot() {
        switchToSlot(EMPTY_SLOT);
    }

    public Map<Integer, ItemStack> getHotbarSlots() {
        return hotbarSlots;
    }
}
