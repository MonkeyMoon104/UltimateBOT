package com.monkey.ultimatebot.gui.v26_2.tab;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.item.*;

public class TargetsTab {

    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER =
            LegacyComponentSerializer.legacySection();
    private final BotGuiTabContext context;

    public TargetsTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        List<UUID> targets = resolveTargets();

        List<Item> items = new ArrayList<>();
        for (UUID targetUUID : targets) {
            items.add(new TargetHeadItem(targetUUID));
        }

        return ScrollGui.itemsBuilder()
                .setStructure(
                        "# # # # # # # #", "# t t t t t t u", "# t t t t t t #", "# t t t t t t d", "# # # # # # # #")
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .addIngredient('t', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('u', new ScrollUpItem())
                .addIngredient('d', new ScrollDownItem())
                .setBackground(createEmptyBackground())
                .setContent(items)
                .build();
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

    private List<UUID> resolveTargets() {
        BotOptions effectiveOptions = context.resolveEffectiveOptions();
        Set<UUID> targets = new LinkedHashSet<>();

        if (effectiveOptions != null) {
            for (UUID targetUUID : effectiveOptions.getTargetUUIDs()) {
                if (!isProtectedOwner(targetUUID, effectiveOptions)) {
                    targets.add(targetUUID);
                }
            }

            UUID preferredTargetUUID = effectiveOptions.getPreferredTargetUUID();
            if (preferredTargetUUID != null && !isProtectedOwner(preferredTargetUUID, effectiveOptions)) {
                targets.add(preferredTargetUUID);
            }
        }

        if (targets.isEmpty()) {
            ITrainingBot managedBot = context.resolveManagedBot();
            if (managedBot != null) {
                Player targetPlayer = managedBot.getTargetPlayer();
                if (targetPlayer == null && managedBot.getBrainController() != null) {
                    targetPlayer = managedBot.getBrainController().getTargetPlayer();
                }

                if (targetPlayer != null && !isProtectedOwner(targetPlayer.getUniqueId(), effectiveOptions)) {
                    targets.add(targetPlayer.getUniqueId());
                }
            }
        }

        return new ArrayList<>(targets);
    }

    private boolean isProtectedOwner(UUID candidateUUID, BotOptions options) {
        if (candidateUUID == null || options == null) return false;

        BotType botType = options.getBotType();
        if (botType == BotType.ALLY) {
            UUID ownerUUID = options.getOwnerUUID();
            return ownerUUID != null && ownerUUID.equals(candidateUUID);
        }
        if (botType == BotType.TEAM_ALLY) {
            return options.getTeamOwnerUUIDs().contains(candidateUUID);
        }
        return false;
    }

    private class ScrollUpItem extends AbstractScrollGuiBoundItem {

        @Override
        public ItemProvider getItemProvider(Player viewer) {
            ScrollGui<?> gui = getGui();
            String name = context.getTraining().getLangString("gui.targets-tab.scroll-up.name", "&aScroll up");
            ItemBuilder builder = new ItemBuilder(Material.ARROW).setLegacyName(ChatColorUtils.translate(name));
            if (gui.getLine() <= 0) {
                String cantScroll = context.getTraining()
                        .getLangString("gui.targets-tab.scroll-up.cant-scroll", "&7Already at the top");
                builder.addLegacyLoreLines(ChatColorUtils.translate(cantScroll));
            }
            return builder;
        }

        @Override
        public void handleClick(ClickType clickType, Player player, Click click) {
            ScrollGui<?> gui = getGui();
            if (gui.getLine() > 0) {
                gui.setLine(gui.getLine() - 1);
            }
        }
    }

    private class ScrollDownItem extends AbstractScrollGuiBoundItem {

        @Override
        public ItemProvider getItemProvider(Player viewer) {
            ScrollGui<?> gui = getGui();
            String name = context.getTraining().getLangString("gui.targets-tab.scroll-down.name", "&aScroll down");
            ItemBuilder builder = new ItemBuilder(Material.ARROW).setLegacyName(ChatColorUtils.translate(name));
            if (gui.getLine() >= gui.getMaxLine()) {
                String cantScroll = context.getTraining()
                        .getLangString("gui.targets-tab.scroll-down.cant-scroll", "&7Already at the bottom");
                builder.addLegacyLoreLines(ChatColorUtils.translate(cantScroll));
            }
            return builder;
        }

        @Override
        public void handleClick(ClickType clickType, Player player, Click click) {
            ScrollGui<?> gui = getGui();
            if (gui.getLine() < gui.getMaxLine()) {
                gui.setLine(gui.getLine() + 1);
            }
        }
    }

    private class TargetHeadItem extends AbstractItem {

        private final UUID targetUUID;

        private TargetHeadItem(UUID targetUUID) {
            this.targetUUID = targetUUID;
        }

        @Override
        public ItemProvider getItemProvider(Player viewer) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : targetUUID.toString();

            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);

                String nameTemplate = context.getTraining().getLangString("gui.targets-tab.head.name", "&c%player%");
                String name = ChatColorUtils.translate(
                        nameTemplate.replace("%player%", playerName).replace("%uuid%", targetUUID.toString()));
                meta.displayName(LEGACY_SECTION_SERIALIZER.deserialize(name));

                List<String> loreLines = context.getTraining().getLangStringList("gui.targets-tab.head.lore");
                List<? extends Component> lore = loreLines.stream()
                        .map(line -> ChatColorUtils.translate(
                                line.replace("%player%", playerName).replace("%uuid%", targetUUID.toString())))
                        .map(LEGACY_SECTION_SERIALIZER::deserialize)
                        .toList();
                meta.lore(lore);
                skull.setItemMeta(meta);
            }

            return new ItemBuilder(skull);
        }

        @Override
        public void handleClick(ClickType clickType, Player player, Click click) {}
    }
}
