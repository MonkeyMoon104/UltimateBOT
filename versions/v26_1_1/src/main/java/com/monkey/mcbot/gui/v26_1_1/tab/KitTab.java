package com.monkey.mcbot.gui.v26_1_1.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.gui.v26_1_1.impl.ArmorItem;
import com.monkey.mcbot.gui.v26_1_1.impl.CombatItem;
import com.monkey.mcbot.gui.v26_1_1.impl.FollowItem;
import com.monkey.mcbot.gui.v26_1_1.impl.RankItem;
import com.monkey.mcbot.gui.v26_1_1.impl.SpawnItem;
import com.monkey.mcbot.gui.v26_1_1.impl.TeleportItem;
import com.monkey.mcbot.gui.v26_1_1.impl.TotemItem;
import com.monkey.mcbot.utils.equipment.EquipmentConverter;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;

public class KitTab {

    private final BotGuiTabContext context;

    public KitTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        BotOptions options = context.getOptions();
        CombatItem combatItem = new CombatItem(context.getTraining(), options);
        FollowItem followItem = new FollowItem(context.getTraining(), options, combatItem);

        Gui gui = Gui.builder()
                .setStructure(
                        "# r . . . . a #",
                        "# . . t . . a #",
                        "# . s g f . a #",
                        "# c . . . . a #",
                        "# # # # # # # #"
                )
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('.', Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('r', new RankItem(context.getTraining(), options))
                .addIngredient('t', new TotemItem(options, context.getTraining()))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(context.getTraining(), context.getViewer(), options))
                .addIngredient('g', context.hasManagedBotSpawned()
                        ? new TeleportItem(context.getTraining())
                        : Item.simple(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .build();

        for (EquipmentSlot slot : EquipmentConverter.getArmorSlots()) {
            ItemStack piece = options.getArmor().get(slot);
            gui.addItems(new ArmorItem(context.getTraining(), slot, piece, options));
        }

        return gui;
    }
}