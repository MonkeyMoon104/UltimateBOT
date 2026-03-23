package com.monkey.mcbot.gui.tab;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
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

public class TargetsTab {

    private final BotGuiTabContext context;

    public TargetsTab(BotGuiTabContext context) {
        this.context = context;
    }

    public Gui build(Material borderMaterial, String borderName) {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # #",
                        "# t t t t t t #",
                        "# t t t t t t #",
                        "# t t t t t t #",
                        "# # # # # # # #"
                )
                .addIngredient('#', context.createBorderItem(borderMaterial, borderName))
                .build();

        for (UUID targetUUID : resolveTargets()) {
            gui.addItems(new TargetHeadItem(targetUUID));
        }

        return gui;
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
            if (preferredTargetUUID != null
                    && !isProtectedOwner(preferredTargetUUID, effectiveOptions)) {
                targets.add(preferredTargetUUID);
            }
        }

        if (targets.isEmpty()) {
            ITrainingBot managedBot = context.resolveManagedBot();
            if (managedBot != null) {
                Player targetPlayer = managedBot.getTargetPlayer();
                if (targetPlayer == null
                        && managedBot.getBrainController() != null) {
                    targetPlayer = managedBot.getBrainController().getTargetPlayer();
                }

                if (targetPlayer != null
                        && !isProtectedOwner(targetPlayer.getUniqueId(), effectiveOptions)) {
                    targets.add(targetPlayer.getUniqueId());
                }
            }
        }

        return new ArrayList<>(targets);
    }

    private boolean isProtectedOwner(UUID candidateUUID, BotOptions options) {
        if (candidateUUID == null || options == null) {
            return false;
        }

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

    private class TargetHeadItem extends AbstractItem {

        private final UUID targetUUID;

        private TargetHeadItem(UUID targetUUID) {
            this.targetUUID = targetUUID;
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : targetUUID.toString();

            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);

                String nameTemplate = context.getTraining().getConfig()
                        .getString("gui.targets-tab.head.name", "&c%player%");
                meta.setDisplayName(ChatColorUtils.translate(
                        nameTemplate.replace("%player%", playerName)
                                .replace("%uuid%", targetUUID.toString())));

                List<String> loreLines = context.getTraining().getConfig().getStringList("gui.targets-tab.head.lore");
                List<String> lore = loreLines.stream()
                        .map(line -> ChatColorUtils.translate(
                                line.replace("%player%", playerName)
                                        .replace("%uuid%", targetUUID.toString())))
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
