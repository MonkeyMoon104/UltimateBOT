package com.monkey.ultimatebot.combat.mode.trident;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import net.minecraft.world.item.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;

final class TridentLoadout {
    static final int RIPTIDE_SLOT = BotInventoryController.SWORD_SLOT;
    static final int LOYALTY_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    static final int SPONGE_SLOT = BotInventoryController.TOTEM_SLOT;
    static final int WATER_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    static final int WEB_SLOT = BotInventoryController.ANCHOR_SLOT;
    static final int HOE_SLOT = BotInventoryController.GLOW_SLOT;

    private TridentLoadout() {}

    static ModeKit create() {
        return ModeKit.builder()
                .slot(RIPTIDE_SLOT, enchantedTrident(Enchantment.RIPTIDE, 3))
                .slot(LOYALTY_SLOT, enchantedTrident(Enchantment.LOYALTY, 3))
                .slot(SPONGE_SLOT, Items.SPONGE, 32)
                .slot(WATER_SLOT, Items.WATER_BUCKET, 4)
                .slot(WEB_SLOT, Items.COBWEB, 16)
                .slot(HOE_SLOT, Items.NETHERITE_HOE)
                .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                .build();
    }

    private static net.minecraft.world.item.ItemStack enchantedTrident(Enchantment movementEnchantment, int level) {
        org.bukkit.inventory.ItemStack trident = new org.bukkit.inventory.ItemStack(Material.TRIDENT);
        trident.addUnsafeEnchantment(movementEnchantment, level);
        trident.addUnsafeEnchantment(Enchantment.IMPALING, 5);
        trident.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        return CraftItemStack.asNMSCopy(trident);
    }
}
