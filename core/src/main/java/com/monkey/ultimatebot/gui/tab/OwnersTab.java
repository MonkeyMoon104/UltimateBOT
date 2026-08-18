package com.monkey.ultimatebot.gui.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.access.item.ItemMetaAccess;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.animation.impl.SplitSequentialAnimation;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem;

public class OwnersTab {

    private final BotGuiTabContext context;

    public OwnersTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        List<UUID> owners = resolveOwners();

        List<Item> items = new ArrayList<>();
        for (UUID ownerUUID : owners) {
            items.add(new OwnerHeadItem(ownerUUID));
        }

        ScrollGui<Item> gui = ScrollGui.items()
                .setStructure(
                        "# # # # # # # #", "# o o o o o o u", "# o o o o o o #", "# o o o o o o d", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('o', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('u', new ScrollUpItem())
                .addIngredient('d', new ScrollDownItem())
                .setBackground(createEmptyBackground())
                .setContent(items)
                .build();

        boolean foliaDetected =
                context.getTraining().getWrapperManager().capabilities().foliaDetected();

        if (!items.isEmpty() && !foliaDetected) {
            gui.playAnimation(new SplitSequentialAnimation(3, false), slotElement -> {
                if (!(slotElement instanceof SlotElement.ItemSlotElement)) return false;
                SlotElement.ItemSlotElement itemSlot = (SlotElement.ItemSlotElement) slotElement;
                return itemSlot.getItem() instanceof OwnerHeadItem;
            });
        }

        return gui;
    }

    private ItemProvider createEmptyBackground() {
        ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            ItemMetaAccess.setDisplayName(meta, " ");
            glass.setItemMeta(meta);
        }
        return new ItemBuilder(glass);
    }

    private List<UUID> resolveOwners() {
        ITrainingBot managedBot = context.resolveManagedBot();
        if (managedBot == null || managedBot.getBrainController() == null) {
            return new ArrayList<>();
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

    private class ScrollUpItem extends ScrollItem {

        ScrollUpItem() {
            super(-1);
        }

        @Override
        public ItemProvider getItemProvider(ScrollGui<?> gui) {
            String name = context.getTraining().getLangString("gui.owners-tab.scroll-up.name", "&aScroll up");
            ItemBuilder builder = new ItemBuilder(Material.ARROW).setDisplayName(ChatColorUtils.translate(name));
            if (!gui.canScroll(-1)) {
                String cantScroll = context.getTraining()
                        .getLangString("gui.owners-tab.scroll-up.cant-scroll", "&7Already at the top");
                builder.addLoreLines(ChatColorUtils.translate(cantScroll));
            }
            return builder;
        }
    }

    private class ScrollDownItem extends ScrollItem {

        ScrollDownItem() {
            super(1);
        }

        @Override
        public ItemProvider getItemProvider(ScrollGui<?> gui) {
            String name = context.getTraining().getLangString("gui.owners-tab.scroll-down.name", "&aScroll down");
            ItemBuilder builder = new ItemBuilder(Material.ARROW).setDisplayName(ChatColorUtils.translate(name));
            if (!gui.canScroll(1)) {
                String cantScroll = context.getTraining()
                        .getLangString("gui.owners-tab.scroll-down.cant-scroll", "&7Already at the bottom");
                builder.addLoreLines(ChatColorUtils.translate(cantScroll));
            }
            return builder;
        }
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

                String nameTemplate = context.getTraining().getLangString("gui.owners-tab.head.name", "&e%player%");
                String name = ChatColorUtils.translate(
                        nameTemplate.replace("%player%", playerName).replace("%uuid%", ownerUUID.toString()));
                ItemMetaAccess.setDisplayName(meta, name);

                List<String> loreLines = context.getTraining().getLangStringList("gui.owners-tab.head.lore");
                List<String> lore = new ArrayList<>(loreLines.size());
                for (String line : loreLines) {
                    lore.add(ChatColorUtils.translate(
                            line.replace("%player%", playerName).replace("%uuid%", ownerUUID.toString())));
                }
                ItemMetaAccess.setLore(meta, lore);
                skull.setItemMeta(meta);
            }

            return new ItemBuilder(skull);
        }

        @Override
        public void handleClick(
                @NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {}
    }
}
