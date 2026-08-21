package com.monkey.ultimatebot.gui.v26_1;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.combat.mode.shared.CombatModeLoadoutDefaults;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import com.monkey.ultimatebot.gui.NewBotGUI;
import com.monkey.ultimatebot.gui.v26_1.impl.navigation.BotTabItem;
import com.monkey.ultimatebot.gui.v26_1.tab.BotGuiTabContext;
import com.monkey.ultimatebot.gui.v26_1.tab.CombatSettingsTab;
import com.monkey.ultimatebot.gui.v26_1.tab.KitTab;
import com.monkey.ultimatebot.gui.v26_1.tab.OwnersTab;
import com.monkey.ultimatebot.gui.v26_1.tab.TargetsTab;
import com.monkey.ultimatebot.gui.v26_1.tab.TemplatesTab;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.armor.ArmorCycle;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.monkey.ultimatebot.libs.invui.gui.Gui;
import com.monkey.ultimatebot.libs.invui.gui.Markers;
import com.monkey.ultimatebot.libs.invui.gui.TabGui;
import com.monkey.ultimatebot.libs.invui.item.Item;
import com.monkey.ultimatebot.libs.invui.item.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.window.Window;

public class NewBotGUI_v26_1 {

    private final Player player;
    private final UltimateBot training;
    private final BotType botType;

    public NewBotGUI_v26_1(Player player, UltimateBot training) {
        this(player, training, BotType.SINGLE);
    }

    public NewBotGUI_v26_1(Player player, UltimateBot training, boolean isEventBot) {
        this(player, training, isEventBot ? BotType.EVENT : BotType.SINGLE);
    }

    public NewBotGUI_v26_1(Player player, UltimateBot training, BotType botType) {
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
            options = new BotOptions(
                    training, ArmorCycle.getDefaultArmorFromConfig(training.getLanguageConfig(), training));
            CombatModeLoadoutDefaults.applyArmor(options, options.getCombatMode());
        }

        options.setBotType(botType);
        options.clampCurrentTotemCount();

        String borderMatName = training.getLangString("gui.tab-border.material", "BLACK_STAINED_GLASS_PANE");
        String borderName = training.getLangString("gui.tab-border.name", " ");
        Material borderMat = MaterialCatalog.optional(borderMatName, Material.BLACK_STAINED_GLASS_PANE);

        String translatedBorderName = ChatColorUtils.translate(borderName);
        Item borderItem = Item.simple(new ItemBuilder(borderMat).setLegacyName(translatedBorderName));

        BotGuiTabContext tabContext = new BotGuiTabContext(player, training, options, botType);
        boolean armorTrim = MaterialCatalog.feature(PlatformCapability.ARMOR_TRIM);

        TemplatesTab templatesTab = new TemplatesTab(tabContext, armorTrim);
        Runnable refreshArmor = templatesTab::refreshArmorItems;

        List<Gui> tabs = new ArrayList<>();
        tabs.add(new KitTab(tabContext, refreshArmor).build(borderMat, translatedBorderName));
        tabs.add(templatesTab.build(borderMat, translatedBorderName));
        tabs.add(new OwnersTab(tabContext).build(borderMat, translatedBorderName));
        tabs.add(new TargetsTab(tabContext).build(borderMat, translatedBorderName));
        tabs.add(new CombatSettingsTab(tabContext, refreshArmor).build(borderMat, translatedBorderName));

        var builder = TabGui.builder()
                .setStructure(
                        ". . 0 . 1 . 4 . .",
                        ". x x x x x x x x",
                        "2 x x x x x x x x",
                        ". x x x x x x x x",
                        "3 x x x x x x x x",
                        ". x x x x x x x x")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('.', borderItem);

        int invuiIndex = 0;
        builder.addIngredient('0', new BotTabItem(invuiIndex++, NewBotGUI.LANG_TAB_KIT, training));
        builder.addIngredient('1', new BotTabItem(invuiIndex++, NewBotGUI.LANG_TAB_TEMPLATES, training));
        builder.addIngredient('2', new BotTabItem(invuiIndex++, NewBotGUI.LANG_TAB_OWNERS, training));
        builder.addIngredient('3', new BotTabItem(invuiIndex++, NewBotGUI.LANG_TAB_TARGETS, training));
        builder.addIngredient('4', new BotTabItem(invuiIndex, NewBotGUI.LANG_TAB_COMBAT, training));

        Gui tabGui = builder.setTabs(tabs).build();

        BotOptions finalOptions = options;
        Window window = Window.builder()
                .setUpperGui(tabGui)
                .setViewer(player)
                .setTitle(getTitle())
                .addCloseHandler(reason -> {
                    UUID playerUUID = player.getUniqueId();
                    training.getPlayerOptions().put(playerUUID, finalOptions);
                })
                .build();

        window.open();
    }

    private String getTitle() {
        if (botType == BotType.EVENT) return "KIT ROOM EVENT";
        if (botType == BotType.ALLY) return "KIT ROOM ALLY";
        if (botType == BotType.TEAM_ALLY) return "KIT ROOM TEAM ALLY";
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
