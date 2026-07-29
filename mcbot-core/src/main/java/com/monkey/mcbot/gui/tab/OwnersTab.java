package com.monkey.mcbot.gui.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER =
            LegacyComponentSerializer.legacySection();
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
                if (!(slotElement instanceof SlotElement.ItemSlotElement itemSlot)) return false;
                return itemSlot.getItem() instanceof OwnerHeadItem;
            });
        }

        return gui;
    }

    private ItemProvider createEmptyBackground() {
        ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            glass.setItemMeta(meta);
        }
        return new ItemBuilder(glass);
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
                meta.displayName(LEGACY_SECTION_SERIALIZER.deserialize(name));

                List<String> loreLines = context.getTraining().getLangStringList("gui.owners-tab.head.lore");
                List<? extends Component> lore = loreLines.stream()
                        .map(line -> ChatColorUtils.translate(
                                line.replace("%player%", playerName).replace("%uuid%", ownerUUID.toString())))
                        .map(LEGACY_SECTION_SERIALIZER::deserialize)
                        .toList();
                meta.lore(lore);
                skull.setItemMeta(meta);
            }

            return new ItemBuilder(skull);
        }

        @Override
        public void handleClick(
                @NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {}
    }
}
