package com.monkey.ultimatebot.gui.v26_3.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.gui.v26_3.impl.customization.ArmorItem;
import com.monkey.ultimatebot.gui.v26_3.impl.customization.TrimMaterialSelectorItem;
import com.monkey.ultimatebot.gui.v26_3.impl.customization.TrimPatternSelectorItem;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.monkey.ultimatebot.libs.invui.gui.Gui;
import com.monkey.ultimatebot.libs.invui.item.Item;

public class TemplatesTab {

    private final BotGuiTabContext context;
    private final boolean armorTrim;
    private List<ArmorItem> armorItems = List.of();

    public TemplatesTab(BotGuiTabContext context) {
        this(context, true);
    }

    public TemplatesTab(BotGuiTabContext context, boolean armorTrim) {
        this.context = context;
        this.armorTrim = armorTrim;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();
        ArmorItem helmetItem = new ArmorItem(
                context.getTraining(), EquipmentSlotKind.HEAD, requireArmor(options, EquipmentSlotKind.HEAD), options);
        ArmorItem chestItem = new ArmorItem(
                context.getTraining(),
                EquipmentSlotKind.CHEST,
                requireArmor(options, EquipmentSlotKind.CHEST),
                options);
        ArmorItem legsItem = new ArmorItem(
                context.getTraining(), EquipmentSlotKind.LEGS, requireArmor(options, EquipmentSlotKind.LEGS), options);
        ArmorItem bootsItem = new ArmorItem(
                context.getTraining(), EquipmentSlotKind.FEET, requireArmor(options, EquipmentSlotKind.FEET), options);
        armorItems = List.of(helmetItem, chestItem, legsItem, bootsItem);

        if (!armorTrim) {
            return Gui.builder()
                    .setStructure(
                            "# # # # # # # #",
                            "# # . a . # # #",
                            "# # . d . # # #",
                            "# # . e . # # #",
                            "# # . f . # # #")
                    .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                    .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                    .addIngredient('a', helmetItem)
                    .addIngredient('d', chestItem)
                    .addIngredient('e', legsItem)
                    .addIngredient('f', bootsItem)
                    .build();
        }

        return Gui.builder()
                .setStructure(
                        "# # # # # # # #", "# # h a j # # #", "# # c d k # # #", "# # l e m # # #", "# # n f p # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient(
                        'h',
                        new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlotKind.HEAD, helmetItem))
                .addIngredient(
                        'j',
                        new TrimMaterialSelectorItem(
                                context.getTraining(), options, EquipmentSlotKind.HEAD, helmetItem))
                .addIngredient(
                        'c',
                        new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlotKind.CHEST, chestItem))
                .addIngredient(
                        'k',
                        new TrimMaterialSelectorItem(
                                context.getTraining(), options, EquipmentSlotKind.CHEST, chestItem))
                .addIngredient(
                        'l',
                        new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlotKind.LEGS, legsItem))
                .addIngredient(
                        'm',
                        new TrimMaterialSelectorItem(context.getTraining(), options, EquipmentSlotKind.LEGS, legsItem))
                .addIngredient(
                        'n',
                        new TrimPatternSelectorItem(context.getTraining(), options, EquipmentSlotKind.FEET, bootsItem))
                .addIngredient(
                        'p',
                        new TrimMaterialSelectorItem(context.getTraining(), options, EquipmentSlotKind.FEET, bootsItem))
                .addIngredient('a', helmetItem)
                .addIngredient('d', chestItem)
                .addIngredient('e', legsItem)
                .addIngredient('f', bootsItem)
                .build();
    }

    public void refreshArmorItems() {
        armorItems.forEach(ArmorItem::notifyWindows);
    }

    private static ItemStack requireArmor(BotOptions options, EquipmentSlotKind slot) {
        return Objects.requireNonNull(options.getArmor().get(slot), () -> "Missing armor item for " + slot);
    }
}
