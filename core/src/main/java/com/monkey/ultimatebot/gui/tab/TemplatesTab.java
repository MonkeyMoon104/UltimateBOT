package com.monkey.ultimatebot.gui.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.gui.impl.customization.ArmorItem;
import com.monkey.ultimatebot.gui.impl.customization.TrimMaterialSelectorItem;
import com.monkey.ultimatebot.gui.impl.customization.TrimPatternSelectorItem;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class TemplatesTab {

    private final BotGuiTabContext context;
    private List<ArmorItem> armorItems = List.of();

    public TemplatesTab(BotGuiTabContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();
        ArmorItem helmetItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.HEAD, options.getArmor().get(EquipmentSlot.HEAD), options);
        ArmorItem chestItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.CHEST, options.getArmor().get(EquipmentSlot.CHEST), options);
        ArmorItem legsItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.LEGS, options.getArmor().get(EquipmentSlot.LEGS), options);
        ArmorItem bootsItem = new ArmorItem(
                context.getTraining(), EquipmentSlot.FEET, options.getArmor().get(EquipmentSlot.FEET), options);
        armorItems = List.of(helmetItem, chestItem, legsItem, bootsItem);

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # #", "# # h a j # # #", "# # c d k # # #", "# # l e m # # #", "# # n f p # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
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

    public void refreshArmorItems() {
        armorItems.forEach(ArmorItem::notifyWindows);
    }
}
