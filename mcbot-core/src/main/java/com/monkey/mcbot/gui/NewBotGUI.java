package com.monkey.mcbot.gui;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.gui.impl.*;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.UUID;

public class NewBotGUI {

    private final Player player;
    private final MinecraftBot training;
    private final BotType botType;

    public NewBotGUI(Player player, MinecraftBot training) {
        this(player, training, BotType.SINGLE);
    }

    public NewBotGUI(Player player, MinecraftBot training, boolean isEventBot) {
        this(player, training, isEventBot ? BotType.EVENT : BotType.SINGLE);
    }

    public NewBotGUI(Player player, MinecraftBot training, BotType botType) {
        this.player = player;
        this.training = training;
        this.botType = botType == null ? BotType.SINGLE : botType;
    }

    public void open() {
        BotOptions options = training.getPlayerOptions().getOptions(player.getUniqueId());
        if (options == null) {
            UUID managedOwner = resolveManagedOwnerUUID();
            ITrainingBot activeBot = training.getBotManager().getBotSafe(managedOwner);
            if (activeBot != null
                    && activeBot.getBrainController() != null
                    && activeBot.getBrainController().getBotOptions() != null) {
                options = activeBot.getBrainController().getBotOptions();
            }
        }
        if (options == null) {
            options = new BotOptions(training, ArmorCycle.getDefaultArmorFromConfig(training.getConfig(), training));
        }

        options.setBotType(botType);

        clampTotemCount(options);

        CombatItem combatItem = new CombatItem(training, options);
        FollowItem followItem = new FollowItem(training, options, combatItem);

        RankItem rankItem = new RankItem(training, options);

        boolean hasManagedBotSpawned = botType == BotType.TEAM_ALLY
                ? training.getBotManager().hasActiveTeamAlly(player.getUniqueId())
                : training.getBotManager().isBotSpawned(player.getUniqueId());

        Gui gui = Gui.normal()
                .setStructure(
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . a a t a a . .",
                        ". . . . . . . . .",
                        ". . . s g f . . .",
                        "r . . . . . . . c"
                )
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('t', new TotemItem(options, training))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(training, player, options))
                .addIngredient('g', hasManagedBotSpawned
                    ? new TeleportItem(training) : new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .addIngredient('r', rankItem)
                .build();

        for (EquipmentSlot value : com.monkey.mcbot.utils.equipment.EquipmentConverter.getArmorSlots()) {
            ItemStack piece = options.getArmor().get(value);
            gui.addItems(new ArmorItem(training, value, piece, options));
        }

        BotOptions finalOptions = options;
        Window window = Window.single()
                .setGui(gui)
                .setViewer(player)
                .setTitle(getTitle())
                .addCloseHandler(() -> {
                    UUID playerUUID = player.getUniqueId();
                    training.getPlayerOptions().put(playerUUID, finalOptions);
                })
                .build();

        window.open();
    }

    private String getTitle() {
        if (botType == BotType.EVENT) {
            return "KIT ROOM EVENT";
        }
        if (botType == BotType.ALLY) {
            return "KIT ROOM ALLY";
        }
        if (botType == BotType.TEAM_ALLY) {
            return "KIT ROOM TEAM ALLY";
        }
        return "KIT ROOM";
    }

    private void clampTotemCount(BotOptions options) {
        options.clampCurrentTotemCount();
    }

    private UUID resolveManagedOwnerUUID() {
        if (botType != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }

        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
