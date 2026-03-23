package com.monkey.mcbot.gui.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OwnersTab {

    private final BotGuiTabContext context;

    public OwnersTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # #",
                        "# o o o o o o #",
                        "# o o o o o o #",
                        "# o o o o o o #",
                        "# # # # # # # #"
                )
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .build();

        for (UUID ownerUUID : resolveOwners()) {
            gui.addItems(new OwnerHeadItem(ownerUUID));
        }

        return gui;
    }

    private List<UUID> resolveOwners() {
        ITrainingBot managedBot = context.resolveManagedBot();
        if (managedBot == null || managedBot.getBrainController() == null) {
            return List.of();
        }

        BotOptions effectiveOptions = context.resolveEffectiveOptions();
        Set<UUID> owners = new LinkedHashSet<>();

        if (effectiveOptions != null) {
            owners.addAll(effectiveOptions.getTeamOwnerUUIDs());

            UUID ownerUUID = effectiveOptions.getOwnerUUID();
            if (ownerUUID != null) {
                owners.add(ownerUUID);
            }
        }

        if (owners.isEmpty()) {
            owners.add(context.resolveManagedOwnerUUID());
        }

        return new ArrayList<>(owners);
    }

    private class OwnerHeadItem extends AbstractItem {

        private final UUID ownerUUID;

        private OwnerHeadItem(UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : ownerUUID.toString();

            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);

                String nameTemplate = context.getTraining().getConfig()
                        .getString("gui.owners-tab.head.name", "&e%player%");
                meta.setDisplayName(ChatColorUtils.translate(
                        nameTemplate.replace("%player%", playerName)
                                .replace("%uuid%", ownerUUID.toString())));

                List<String> loreLines = context.getTraining().getConfig().getStringList("gui.owners-tab.head.lore");
                List<String> lore = loreLines.stream()
                        .map(line -> ChatColorUtils.translate(
                                line.replace("%player%", playerName)
                                        .replace("%uuid%", ownerUUID.toString())))
                        .toList();
                meta.setLore(lore);

                skull.setItemMeta(meta);
            }

            return new ItemBuilder(skull);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player,
                                @NotNull InventoryClickEvent event) {
        }
    }
}
