package com.monkey.mcbot.bot.ai.controllers.inventory.helper;

import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.IResourceReplenisher;
import com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter.ISlotManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;

public final class SlotManager implements ISlotManager {

    private final Player bot;
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
            Player bot, IResourceReplenisher resourceReplenisher, IEquipmentBroadcaster equipmentBroadcaster) {
        this.bot = java.util.Objects.requireNonNull(bot, "bot");
        this.resourceReplenisher = java.util.Objects.requireNonNull(resourceReplenisher, "resourceReplenisher");
        this.equipmentBroadcaster = java.util.Objects.requireNonNull(equipmentBroadcaster, "equipmentBroadcaster");
        initializeDefaultItems();
    }

    private void initializeDefaultItems() {
        hotbarSlots.put(SWORD_SLOT, createDefaultSword());
        hotbarSlots.put(ENDERPEARL_SLOT, new ItemStack(Items.ENDER_PEARL, 16));
        hotbarSlots.put(TOTEM_SLOT, new ItemStack(Items.TOTEM_OF_UNDYING));
        hotbarSlots.put(OBSIDIAN_SLOT, new ItemStack(Items.OBSIDIAN, 64));
        hotbarSlots.put(CRYSTAL_SLOT, new ItemStack(Items.END_CRYSTAL, 64));
        hotbarSlots.put(ANCHOR_SLOT, new ItemStack(Items.RESPAWN_ANCHOR, 64));
        hotbarSlots.put(GLOW_SLOT, new ItemStack(Items.GLOWSTONE, 64));
        hotbarSlots.put(GOLDEN_APPLE_SLOT, new ItemStack(Items.GOLDEN_APPLE, 64));
        hotbarSlots.put(EMPTY_SLOT, ItemStack.EMPTY);
        switchToSlot(SWORD_SLOT);
    }

    private ItemStack createDefaultSword() {
        org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.NETHERITE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
        return CraftItemStack.asNMSCopy(sword);
    }

    @Override
    public void switchToSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        if (!hotbarSlots.containsKey(slot)) return;

        resourceReplenisher.replenishItem(hotbarSlots, slot);

        currentSlot = slot;
        ItemStack item = hotbarSlots.get(slot);

        bot.setItemSlot(EquipmentSlot.MAINHAND, item);

        equipmentBroadcaster.broadcastEquipmentChange(bot);
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot <= 8) {
            hotbarSlots.put(slot, item.copy());

            if (slot == currentSlot) {
                bot.setItemSlot(EquipmentSlot.MAINHAND, item);
                equipmentBroadcaster.broadcastEquipmentChange(bot);
            }
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        return hotbarSlots.getOrDefault(slot, ItemStack.EMPTY);
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

        return current;
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
