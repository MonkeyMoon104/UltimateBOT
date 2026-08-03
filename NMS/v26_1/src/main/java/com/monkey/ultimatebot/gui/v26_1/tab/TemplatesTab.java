package com.monkey.ultimatebot.gui.v26_1.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.v26_1.impl.customization.ArmorItem;
import com.monkey.ultimatebot.gui.v26_1.impl.customization.TrimMaterialSelectorItem;
import com.monkey.ultimatebot.gui.v26_1.impl.customization.TrimPatternSelectorItem;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;

public class TemplatesTab {

    private final BotGuiTabContext context;

    public TemplatesTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();
        ArmorItem helmetItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.HEAD, requireArmor(options, EquipmentSlot.HEAD), options);
        ArmorItem chestItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.CHEST, requireArmor(options, EquipmentSlot.CHEST), options);
        ArmorItem legsItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.LEGS, requireArmor(options, EquipmentSlot.LEGS), options);
        ArmorItem bootsItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.FEET, requireArmor(options, EquipmentSlot.FEET), options);

        Gui gui = Gui.builder()
                .setStructure(
                        "# # # # # # # #", "# h a j . . . #", "# c d k . . . #", "# l e m . . . #", "# n f p . . . #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient(
                        'h',
                        new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlot.HEAD, helmetItem))
                .addIngredient(
                        'j',
                        new TrimMaterialSelectorItem(context.getTraining(), options, EquipmentSlot.HEAD, helmetItem))
                .addIngredient(
                        'c',
                        new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlot.CHEST, chestItem))
                .addIngredient(
                        'k',
                        new TrimMaterialSelectorItem(context.getTraining(), options, EquipmentSlot.CHEST, chestItem))
                .addIngredient(
                        'l', new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlot.LEGS, legsItem))
                .addIngredient(
                        'm', new TrimMaterialSelectorItem(context.getTraining(), options, EquipmentSlot.LEGS, legsItem))
                .addIngredient(
                        'n', new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlot.FEET, bootsItem))
                .addIngredient(
                        'p',
                        new TrimMaterialSelectorItem(context.getTraining(), options, EquipmentSlot.FEET, bootsItem))
                .addIngredient('a', helmetItem)
                .addIngredient('d', chestItem)
                .addIngredient('e', legsItem)
                .addIngredient('f', bootsItem)
                .build();

        return gui;
    }

    private static ItemStack requireArmor(BotOptions options, EquipmentSlot slot) {
        return Objects.requireNonNull(options.getArmor().get(slot), () -> "Missing armor item for " + slot);
    }
}
