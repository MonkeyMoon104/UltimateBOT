package com.monkey.ultimatebot.gui;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.combat.mode.shared.CombatModeLoadoutDefaults;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import com.monkey.ultimatebot.gui.impl.navigation.BotTabItem;
import com.monkey.ultimatebot.gui.tab.BotGuiTabContext;
import com.monkey.ultimatebot.gui.tab.CombatSettingsTab;
import com.monkey.ultimatebot.gui.tab.KitTab;
import com.monkey.ultimatebot.gui.tab.OwnersTab;
import com.monkey.ultimatebot.gui.tab.TargetsTab;
import com.monkey.ultimatebot.gui.tab.TemplatesTab;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.armor.ArmorCycle;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.monkey.ultimatebot.libs.invui.gui.Gui;
import com.monkey.ultimatebot.libs.invui.gui.TabGui;
import com.monkey.ultimatebot.libs.invui.gui.structure.Markers;
import com.monkey.ultimatebot.libs.invui.item.builder.ItemBuilder;
import com.monkey.ultimatebot.libs.invui.item.impl.SimpleItem;
import com.monkey.ultimatebot.libs.invui.window.Window;

public class NewBotGUI {

    public static final int LANG_TAB_KIT = 0;

    public static final int LANG_TAB_TEMPLATES = 1;

    public static final int LANG_TAB_OWNERS = 2;

    public static final int LANG_TAB_TARGETS = 3;

    public static final int LANG_TAB_COMBAT = 4;

    private final Player player;
    private final UltimateBot training;
    private final BotType botType;

    public NewBotGUI(Player player, UltimateBot training) {
        this(player, training, BotType.SINGLE);
    }

    public NewBotGUI(Player player, UltimateBot training, boolean isEventBot) {
        this(player, training, isEventBot ? BotType.EVENT : BotType.SINGLE);
    }

    public NewBotGUI(Player player, UltimateBot training, BotType botType) {
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
        SimpleItem borderItem = new SimpleItem(new ItemBuilder(borderMat)
                .setDisplayName(ChatColorUtils.translate(borderName))
                .get(String.valueOf(1)));

        BotGuiTabContext tabContext = new BotGuiTabContext(player, training, options, botType);
        String translatedBorderName = ChatColorUtils.translate(borderName);
        boolean armorTrim = MaterialCatalog.feature(PlatformCapability.ARMOR_TRIM);

        TemplatesTab templatesTab = new TemplatesTab(tabContext, armorTrim);
        Runnable refreshArmor = templatesTab::refreshArmorItems;

        List<Gui> tabs = new ArrayList<>();
        tabs.add(new KitTab(tabContext, refreshArmor).build(borderMat, translatedBorderName));
        tabs.add(templatesTab.build(borderMat, translatedBorderName));
        tabs.add(new OwnersTab(tabContext).build(borderMat, translatedBorderName));
        tabs.add(new TargetsTab(tabContext).build(borderMat, translatedBorderName));
        tabs.add(new CombatSettingsTab(tabContext, refreshArmor).build(borderMat, translatedBorderName));

        TabGui.Builder tabGuiBuilder = TabGui.normal()
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
        tabGuiBuilder.addIngredient('0', new BotTabItem(invuiIndex++, LANG_TAB_KIT, training));
        tabGuiBuilder.addIngredient('1', new BotTabItem(invuiIndex++, LANG_TAB_TEMPLATES, training));
        tabGuiBuilder.addIngredient('2', new BotTabItem(invuiIndex++, LANG_TAB_OWNERS, training));
        tabGuiBuilder.addIngredient('3', new BotTabItem(invuiIndex++, LANG_TAB_TARGETS, training));
        tabGuiBuilder.addIngredient('4', new BotTabItem(invuiIndex, LANG_TAB_COMBAT, training));

        Gui tabGui = tabGuiBuilder.setTabs(tabs).build();

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
