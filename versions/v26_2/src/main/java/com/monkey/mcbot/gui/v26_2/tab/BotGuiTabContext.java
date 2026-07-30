package com.monkey.mcbot.gui.v26_2.tab;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;

public class BotGuiTabContext {

    private final Player viewer;
    private final MinecraftBot training;
    private final BotOptions options;
    private final BotType botType;

    public BotGuiTabContext(Player viewer, MinecraftBot training, BotOptions options, BotType botType) {
        this.viewer = viewer;
        this.training = training;
        this.options = options;
        this.botType = botType == null ? BotType.SINGLE : botType;
    }

    public Player getViewer() {
        return viewer;
    }

    public MinecraftBot getTraining() {
        return training;
    }

    public BotOptions getOptions() {
        return options;
    }

    public BotType getBotType() {
        return botType;
    }

    public UUID resolveManagedOwnerUUID() {
        if (botType != BotType.TEAM_ALLY) {
            return viewer.getUniqueId();
        }

        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(viewer.getUniqueId());
        return teamOwnerUUID == null ? viewer.getUniqueId() : teamOwnerUUID;
    }

    public boolean hasManagedBotSpawned() {
        if (botType == BotType.TEAM_ALLY) {
            return training.getBotManager().hasActiveTeamAlly(viewer.getUniqueId());
        }
        return training.getBotManager().isBotSpawned(viewer.getUniqueId());
    }

    public ITrainingBot resolveManagedBot() {
        return training.getBotManager().getBotSafe(resolveManagedOwnerUUID());
    }

    public BotOptions resolveEffectiveOptions() {
        ITrainingBot managedBot = resolveManagedBot();
        if (managedBot != null
                && managedBot.getBrainController() != null
                && managedBot.getBrainController().getBotOptions() != null) {
            return managedBot.getBrainController().getBotOptions();
        }

        return options;
    }

    public Item createBorderItem(Material borderMaterial, String borderName) {
        return Item.simple(new ItemBuilder(borderMaterial).setLegacyName(borderName));
    }
}
