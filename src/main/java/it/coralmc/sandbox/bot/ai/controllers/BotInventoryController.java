package it.coralmc.sandbox.bot.ai.controllers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.*;

public class BotInventoryController {

    private final Player bot;
    private final Map<Integer, ItemStack> hotbarSlots = new HashMap<>();
    private int currentSlot = 0;

    public static final int SWORD_SLOT = 0;
    public static final int ENDERPEARL_SLOT = 1;
    public static final int TOTEM_SLOT = 2;
    public static final int OBSIDIAN_SLOT = 3;
    public static final int CRYSTAL_SLOT = 4;
    public static final int EMPTY_SLOT = 8;

    public BotInventoryController(Player bot) {
        this.bot = bot;
        initializeDefaultItems();
    }

    private void initializeDefaultItems() {
        hotbarSlots.put(SWORD_SLOT, new ItemStack(Items.NETHERITE_SWORD));

        hotbarSlots.put(ENDERPEARL_SLOT, new ItemStack(Items.ENDER_PEARL, 16));

        hotbarSlots.put(TOTEM_SLOT, new ItemStack(Items.TOTEM_OF_UNDYING));

        hotbarSlots.put(OBSIDIAN_SLOT, new ItemStack(Items.OBSIDIAN, 64));

        hotbarSlots.put(CRYSTAL_SLOT, new ItemStack(Items.END_CRYSTAL, 64));

        hotbarSlots.put(EMPTY_SLOT, ItemStack.EMPTY);

        switchToSlot(SWORD_SLOT);
    }

    public void switchToSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        if (!hotbarSlots.containsKey(slot)) return;
        if (currentSlot == slot) return;

        currentSlot = slot;
        ItemStack item = hotbarSlots.get(slot);

        bot.setItemSlot(EquipmentSlot.MAINHAND, item);

        broadcastEquipmentChange();
    }

    public void switchToSword() {
        switchToSlot(SWORD_SLOT);
    }

    public void switchToEnderpearl() {
        switchToSlot(ENDERPEARL_SLOT);
    }

    public void switchToTotem() {
        switchToSlot(TOTEM_SLOT);
    }

    public void switchToCrystal() {
        switchToSlot(CRYSTAL_SLOT);
    }

    public void switchToObs() {
        switchToSlot(OBSIDIAN_SLOT);
    }

    public void switchToEmptySlot() {
        switchToSlot(EMPTY_SLOT);
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public ItemStack getCurrentItem() {
        return hotbarSlots.get(currentSlot);
    }

    public boolean hasEnderpearls() {
        ItemStack enderpearlStack = hotbarSlots.get(ENDERPEARL_SLOT);
        return enderpearlStack != null && !enderpearlStack.isEmpty() && enderpearlStack.getCount() > 0;
    }

    public void addEnderpearls(int count) {
        ItemStack enderpearlStack = hotbarSlots.get(ENDERPEARL_SLOT);
        if (enderpearlStack == null || enderpearlStack.isEmpty()) {
            hotbarSlots.put(ENDERPEARL_SLOT, new ItemStack(Items.ENDER_PEARL, count));
        } else {
            enderpearlStack.grow(count);
        }
    }

    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot <= 8) {
            hotbarSlots.put(slot, item.copy());

            if (slot == currentSlot) {
                bot.setItemSlot(EquipmentSlot.MAINHAND, item);
                broadcastEquipmentChange();
            }
        }
    }

    public ItemStack getItem(int slot) {
        return hotbarSlots.getOrDefault(slot, ItemStack.EMPTY);
    }

    public boolean isHoldingSword() {
        return currentSlot == SWORD_SLOT && getCurrentItem().getItem() == Items.NETHERITE_SWORD;
    }

    public boolean isHoldingEnderpearl() {
        return currentSlot == ENDERPEARL_SLOT && getCurrentItem().getItem() == Items.ENDER_PEARL;
    }

    public boolean isHoldingObsidian() {
        return currentSlot == OBSIDIAN_SLOT && getCurrentItem().getItem() == Items.OBSIDIAN;
    }

    public boolean isHoldingCrystal() {
        return currentSlot == CRYSTAL_SLOT && getCurrentItem().getItem() == Items.END_CRYSTAL;
    }

    public void updateTotemSlot(ItemStack totemStack) {
        hotbarSlots.put(TOTEM_SLOT, totemStack);
    }

    private void broadcastEquipmentChange() {
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
        equipmentList.add(Pair.of(EquipmentSlot.MAINHAND, bot.getItemBySlot(EquipmentSlot.MAINHAND)));

        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(
                bot.getId(), equipmentList
        );

        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer handle = ((CraftPlayer) online).getHandle();
            handle.connection.send(equipmentPacket);
        }
    }

    public int getItemCount(net.minecraft.world.item.Item item) {
        for (ItemStack stack : hotbarSlots.values()) {
            if (stack.getItem() == item) {
                return stack.getCount();
            }
        }
        return 0;
    }

    public boolean hasItem(net.minecraft.world.item.Item item) {
        return getItemCount(item) > 0;
    }
}