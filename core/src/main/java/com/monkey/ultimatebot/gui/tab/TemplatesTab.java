package com.monkey.ultimatebot.gui.tab;


import java.util.Collections;
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
    private final boolean armorTrim;
    private List<ArmorItem> armorItems = Collections.emptyList();

    public TemplatesTab(BotGuiTabContext context) {
        this(context, true);
    }

    public TemplatesTab(BotGuiTabContext context, boolean armorTrim) {
        this.context = Objects.requireNonNull(context, "context");
        this.armorTrim = armorTrim;
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
        armorItems = Collections.unmodifiableList(java.util.Arrays.asList(helmetItem, chestItem, legsItem, bootsItem));

        if (!armorTrim) {
            // Pre-1.20: no trim columns — border sits flush against armor (same armor column as modern).
            return Gui.normal()
                    .setStructure(
                            "# # # # # # # #",
                            "# # # a # # # #",
                            "# # # d # # # #",
                            "# # # e # # # #",
                            "# # # f # # # #")
                    .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                    .addIngredient('a', helmetItem)
                    .addIngredient('d', chestItem)
                    .addIngredient('e', legsItem)
                    .addIngredient('f', bootsItem)
                    .build();
        }

        return Gui.normal()
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
    }

    public void refreshArmorItems() {
        armorItems.forEach(ArmorItem::notifyWindows);
    }

    private static ItemStack requireArmor(BotOptions options, EquipmentSlot slot) {
        return Objects.requireNonNull(options.getArmor().get(slot), () -> "Missing armor item for " + slot);
    }
}
