package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.ISlotManager;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
        hotbarSlots.put(ENDERPEARL_SLOT, MaterialCatalog.stack("ENDER_PEARL", Material.ENDER_PEARL, 16));
        hotbarSlots.put(TOTEM_SLOT, MaterialCatalog.stack("TOTEM_OF_UNDYING", Material.GOLDEN_APPLE));
        hotbarSlots.put(OBSIDIAN_SLOT, MaterialCatalog.stack("OBSIDIAN", Material.OBSIDIAN, 64));
        hotbarSlots.put(CRYSTAL_SLOT, MaterialCatalog.stack("END_CRYSTAL", Material.GHAST_TEAR, 64));
        hotbarSlots.put(ANCHOR_SLOT, MaterialCatalog.stack("RESPAWN_ANCHOR", Material.OBSIDIAN, 64));
        hotbarSlots.put(GLOW_SLOT, MaterialCatalog.stack("GLOWSTONE", Material.GLOWSTONE, 64));
        hotbarSlots.put(GOLDEN_APPLE_SLOT, MaterialCatalog.stack("GOLDEN_APPLE", Material.GOLDEN_APPLE, 64));
        hotbarSlots.put(EMPTY_SLOT, new ItemStack(Material.AIR));
        switchToSlot(SWORD_SLOT);
    }

    private ItemStack createDefaultSword() {
        ItemStack sword = new ItemStack(MaterialCatalog.optional("NETHERITE_SWORD", Material.DIAMOND_SWORD));
        Enchantment fireAspect = BotEquipmentUtils.resolveEnchantmentByKeyMinecraft("fire_aspect");
        if (fireAspect != null) sword.addUnsafeEnchantment(fireAspect, 2);
        return sword;
    }

    @Override
    public void switchToSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        if (!hotbarSlots.containsKey(slot)) return;

        resourceReplenisher.replenishItem(hotbarSlots, slot);
        ItemStack item = hotbarSlots.get(slot);
        if (currentSlot == slot) {
            ItemStack hand = bot.getItem(EquipmentSlotKind.HAND);
            if (hand.getType() != item.getType() || hand.getAmount() != item.getAmount()) {
                bot.setItem(EquipmentSlotKind.HAND, item);
            }
            return;
        }

        currentSlot = slot;
        bot.setItem(EquipmentSlotKind.HAND, item);
        equipmentBroadcaster.broadcastHandChange(bot);
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot <= 8) {
            ItemStack current = hotbarSlots.getOrDefault(slot, new ItemStack(Material.AIR));
            if (current.isSimilar(item) && current.getAmount() == item.getAmount()) {
                return;
            }
            hotbarSlots.put(slot, item.clone());

            if (slot == currentSlot) {
                bot.setItem(EquipmentSlotKind.HAND, item);
                equipmentBroadcaster.broadcastHandChange(bot);
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
                    checkedLoadout.getOrDefault(slot, new ItemStack(Material.AIR)), "loadout item at slot " + slot);
            hotbarSlots.put(slot, item.clone());
        }

        ItemStack selectedItem =
                java.util.Objects.requireNonNull(hotbarSlots.get(selectedSlot), "selected hotbar item");
        ItemStack currentMainHand = bot.getItem(EquipmentSlotKind.HAND);
        boolean visualChange =
                !currentMainHand.isSimilar(selectedItem) || currentMainHand.getAmount() != selectedItem.getAmount();
        currentSlot = selectedSlot;
        if (visualChange) {
            bot.setItem(EquipmentSlotKind.HAND, selectedItem);
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        return hotbarSlots.getOrDefault(slot, new ItemStack(Material.AIR));
    }

    @Override
    public int getCurrentSlot() {
        return currentSlot;
    }

    @Override
    public ItemStack getCurrentItem() {
        ItemStack current = hotbarSlots.get(currentSlot);

        if (current != null && !ItemStackAccess.isEmpty(current)) {
            resourceReplenisher.replenishItem(hotbarSlots, currentSlot);
        }

        return current == null ? new ItemStack(Material.AIR) : current;
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
