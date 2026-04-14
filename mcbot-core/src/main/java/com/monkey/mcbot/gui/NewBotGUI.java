package com.monkey.mcbot.gui;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.gui.impl.BotTabItem;
import com.monkey.mcbot.gui.tab.*;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;
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
            options = new BotOptions(training, ArmorCycle.getDefaultArmorFromConfig(training.getLanguageConfig(), training));
        }

        options.setBotType(botType);
        options.clampCurrentTotemCount();

        String borderMatName = training.getLangString("gui.tab-border.material", "BLACK_STAINED_GLASS_PANE");
        String borderName    = training.getLangString("gui.tab-border.name", " ");
        Material borderMat;
        try {
            borderMat = Material.valueOf(borderMatName.toUpperCase());
        } catch (IllegalArgumentException e) {
            borderMat = Material.BLACK_STAINED_GLASS_PANE;
        }
        SimpleItem borderItem = new SimpleItem(
                new ItemBuilder(borderMat)
                        .setDisplayName(ChatColorUtils.translate(borderName))
                        .get(String.valueOf(1)));

        BotGuiTabContext tabContext = new BotGuiTabContext(player, training, options, botType);
        String translatedBorderName = ChatColorUtils.translate(borderName);

        Gui tab0 = new KitTab(tabContext).build(borderMat, translatedBorderName);
        Gui tab1 = new RankTab(tabContext).build(borderMat, translatedBorderName);
        Gui tab2 = new OwnersTab(tabContext).build(borderMat, translatedBorderName);
        Gui tab3 = new TargetsTab(tabContext).build(borderMat, translatedBorderName);

        Gui tabGui = TabGui.normal()
                .setStructure(
                        ". . . 0 . 1 . . .",
                        ". x x x x x x x x",
                        "2 x x x x x x x x",
                        ". x x x x x x x x",
                        "3 x x x x x x x x",
                        ". x x x x x x x x"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('.', borderItem)
                .addIngredient('0', new BotTabItem(0, training))
                .addIngredient('1', new BotTabItem(1, training))
                .addIngredient('2', new BotTabItem(2, training))
                .addIngredient('3', new BotTabItem(3, training))
                .setTabs(Arrays.asList(tab0, tab1, tab2, tab3))
                .build();

        BotOptions finalOptions = options;
        Window window = Window.single()
                .setGui(tabGui)
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
        if (botType == BotType.EVENT)      return "KIT ROOM EVENT";
        if (botType == BotType.ALLY)       return "KIT ROOM ALLY";
        if (botType == BotType.TEAM_ALLY)  return "KIT ROOM TEAM ALLY";
        return "KIT ROOM";
    }

    private UUID resolveManagedOwnerUUID() {
        if (botType != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = training.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }
}
