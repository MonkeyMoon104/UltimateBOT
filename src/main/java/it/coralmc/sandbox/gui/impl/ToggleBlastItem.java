package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class ToggleBlastItem extends AbstractItem {

    private final SandboxTraining training;
    private final BotOptions options;
    private final EquipmentSlot slot;
    private final ArmorItem display;

    public ToggleBlastItem(SandboxTraining training, BotOptions options, EquipmentSlot slot, ArmorItem display) {
        this.training = training;
        this.options = options;
        this.slot = slot;
        this.display = display;
    }

    @Override
    public ItemProvider getItemProvider() {
        boolean status = options.getBlast().getOrDefault(slot, false);

        Material mat = status
                ? Material.valueOf(training.getConfig().getString("gui.enchant-button.enabled-material"))
                : Material.valueOf(training.getConfig().getString("gui.enchant-button.disabled-material"));

        String name = status
                ? training.getConfig().getString("gui.enchant-button.enabled-name")
                : training.getConfig().getString("gui.enchant-button.disabled-name");

        var lore = status
                ? training.getConfig().getStringList("gui.enchant-button.enabled-lore")
                : training.getConfig().getStringList("gui.enchant-button.disabled-lore");

        ItemBuilder builder = new ItemBuilder(mat);
        builder.setDisplayName(ChatColorUtils.translate(name));
        for (String line : lore) {
            builder.addLoreLines(ChatColorUtils.translate(line));
        }
        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        boolean status = options.getBlast().getOrDefault(slot, false);
        options.getBlast().put(slot, !status);

        ItemStack piece = options.getArmor().get(slot);
        if (piece == null) return;

        ItemMeta meta = piece.getItemMeta();
        if (meta == null) return;

        if (status) {
            meta.removeEnchant(Enchantment.BLAST_PROTECTION);
            meta.removeEnchant(Enchantment.PROTECTION);
            meta.addEnchant(Enchantment.PROTECTION, 4, false);
        } else {
            meta.removeEnchant(Enchantment.PROTECTION);
            meta.removeEnchant(Enchantment.BLAST_PROTECTION);
            meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
        }

        piece.setItemMeta(meta);

        training.getBotManager().updateArmor(player.getUniqueId(), options.getArmor(), options.getBlast());

        display.setPiece(piece);
        display.notifyWindows();
        notifyWindows();
    }
}
