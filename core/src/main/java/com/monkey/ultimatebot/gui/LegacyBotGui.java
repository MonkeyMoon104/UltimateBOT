package com.monkey.ultimatebot.gui;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.event.state.BotSettingKey;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.combat.mode.shared.CombatModeLoadoutDefaults;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.event.BotSettingEvents;
import com.monkey.ultimatebot.gui.combat.CombatTuningProperty;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.armor.ArmorCycle;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.Nullable;

/**
 * Bukkit chest GUI used when InvUI cannot run (Java 8 / pre-1.14). Mirrors {@link NewBotGUI} tabs
 * without InvUI: Kit, Armor, Owners, Targets, Combat. Hides capability-gated features (totem /
 * armor trim / unavailable combat modes via catalog).
 */
public final class LegacyBotGui implements InventoryHolder, Listener {

    private static final int SIZE = 54;

    private static final int TAB_KIT = 0;
    private static final int TAB_ARMOR = 1;
    private static final int TAB_OWNERS = 2;
    private static final int TAB_TARGETS = 3;
    private static final int TAB_COMBAT = 4;

    private static final int SLOT_TAB_KIT = 2;
    private static final int SLOT_TAB_ARMOR = 4;
    private static final int SLOT_TAB_COMBAT = 6;
    private static final int SLOT_TAB_OWNERS = 18;
    private static final int SLOT_TAB_TARGETS = 36;

    // Kit tab — absolute slots matching NewBotGUI TabGui + KitTab structure.
    private static final int SLOT_DIFFICULTY = 11;
    private static final int SLOT_TOTEM = 22;
    private static final int SLOT_SPAWN = 30;
    private static final int SLOT_TELEPORT = 31;
    private static final int SLOT_FOLLOW = 32;
    private static final int SLOT_COMBAT = 38;
    private static final int SLOT_COMBAT_MODE = 40;
    private static final int SLOT_TARGET_MODE = 41;

    // Armor tab — vertical column matching TemplatesTab (no-trim).
    private static final int SLOT_ARMOR_HEAD = 22;
    private static final int SLOT_ARMOR_CHEST = 31;
    private static final int SLOT_ARMOR_LEGS = 40;
    private static final int SLOT_ARMOR_FEET = 49;

    // Combat settings tab — matching CombatSettingsTab.
    private static final int SLOT_COMBAT_MODE_C = 11;
    private static final int SLOT_DIFFICULTY_C = 12;
    private static final int SLOT_RESET_TUNING = 15;
    private static final int[] SLOT_TUNING = {
        20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 37, 38
    };

    private final Player player;
    private final UltimateBot plugin;
    private final BotType botType;
    private final BotOptions options;
    private final Inventory inventory;
    private int activeTab = TAB_KIT;
    private boolean closing;
    private boolean listenerRegistered;

    public LegacyBotGui(Player player, UltimateBot plugin, BotType botType) {
        this.player = player;
        this.plugin = plugin;
        this.botType = botType == null ? BotType.SINGLE : botType;
        this.options = resolveOptions();
        this.options.setBotType(this.botType);
        this.options.clampCurrentTotemCount();
        this.inventory = Bukkit.createInventory(this, SIZE, title());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        render();
        if (!listenerRegistered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            listenerRegistered = true;
        }
        player.openInventory(inventory);
    }

    private void render() {
        inventory.clear();
        fillChrome();
        placeTabs();
        if (activeTab == TAB_KIT) {
            renderKit();
        } else if (activeTab == TAB_ARMOR) {
            renderArmor();
        } else if (activeTab == TAB_OWNERS) {
            renderOwners();
        } else if (activeTab == TAB_TARGETS) {
            renderTargets();
        } else {
            renderCombat();
        }
    }

    private void fillChrome() {
        ItemStack border = namedItem(borderStack(), " ", Collections.<String>emptyList());
        inventory.clear();
        // Match NewBotGUI TabGui chrome: border everywhere except content `x` cells.
        int[] content = contentSlots();
        boolean[] isContent = new boolean[SIZE];
        for (int slot : content) {
            isContent[slot] = true;
        }
        for (int slot = 0; slot < SIZE; slot++) {
            if (!isContent[slot]) {
                inventory.setItem(slot, border);
            }
        }
    }

    private void placeTabs() {
        inventory.setItem(SLOT_TAB_KIT, tabItem(TAB_KIT, "&aKit", new ItemStack(Material.DIAMOND_SWORD)));
        inventory.setItem(SLOT_TAB_ARMOR, tabItem(TAB_ARMOR, "&bArmor", new ItemStack(Material.IRON_CHESTPLATE)));
        inventory.setItem(
                SLOT_TAB_COMBAT,
                tabItem(TAB_COMBAT, "&6Combat", MaterialCatalog.stack("GOLDEN_SWORD", Material.IRON_SWORD)));
        inventory.setItem(
                SLOT_TAB_OWNERS, tabItem(TAB_OWNERS, "&eOwners", MaterialCatalog.stack("PLAYER_HEAD", Material.STONE)));
        inventory.setItem(
                SLOT_TAB_TARGETS,
                tabItem(TAB_TARGETS, "&cTargets", MaterialCatalog.stack("PLAYER_HEAD", Material.STONE)));
    }

    private ItemStack tabItem(int tab, String name, ItemStack icon) {
        boolean selected = activeTab == tab;
        List<String> lore = Collections.singletonList(selected ? "&aSelected" : "&7Click to open");
        return namedItem(icon, (selected ? "&l" : "") + name, lore);
    }

    private void renderKit() {
        ItemStack border = namedItem(borderStack(), " ", Collections.<String>emptyList());
        // KitTab nested borders (`#` in structure).
        int[] kitBorders = {
            10, 16, 17, 19, 25, 26, 28, 34, 35, 37, 43, 44, 46, 47, 48, 49, 50, 51, 52, 53
        };
        for (int slot : kitBorders) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(SLOT_DIFFICULTY, difficultyItem());
        if (MaterialCatalog.feature(PlatformCapability.TOTEM)) {
            inventory.setItem(SLOT_TOTEM, totemItem());
        }
        inventory.setItem(SLOT_SPAWN, spawnItem());
        if (isManagedBotSpawned()) {
            inventory.setItem(SLOT_TELEPORT, teleportItem());
        }
        inventory.setItem(SLOT_FOLLOW, followItem());
        inventory.setItem(SLOT_COMBAT, combatToggleItem());
        inventory.setItem(SLOT_COMBAT_MODE, combatModeItem());
        inventory.setItem(SLOT_TARGET_MODE, targetModeItem());
    }

    private void renderArmor() {
        ItemStack border = namedItem(borderStack(), " ", Collections.<String>emptyList());
        for (int slot : contentSlots()) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(SLOT_ARMOR_HEAD, armorPieceItem(EquipmentSlot.HEAD));
        inventory.setItem(SLOT_ARMOR_CHEST, armorPieceItem(EquipmentSlot.CHEST));
        inventory.setItem(SLOT_ARMOR_LEGS, armorPieceItem(EquipmentSlot.LEGS));
        inventory.setItem(SLOT_ARMOR_FEET, armorPieceItem(EquipmentSlot.FEET));
        // Trim UI is 1.19.4+ (ARMOR_TRIM) — intentionally omitted here.
    }

    private void renderOwners() {
        List<UUID> owners = resolveOwners();
        int[] slots = contentSlots();
        int index = 0;
        for (UUID ownerUUID : owners) {
            if (index >= slots.length) {
                break;
            }
            inventory.setItem(slots[index++], headItem(ownerUUID, "&e", "Owner"));
        }
        if (owners.isEmpty()) {
            inventory.setItem(
                    slots[0],
                    namedItem(
                            MaterialCatalog.stack("BARRIER", Material.REDSTONE_BLOCK),
                            "&cNo owners",
                            Collections.singletonList("&7Spawn a bot first")));
        }
    }

    private void renderTargets() {
        List<UUID> targets = resolveTargets();
        int[] slots = contentSlots();
        int index = 0;
        for (UUID targetUUID : targets) {
            if (index >= slots.length) {
                break;
            }
            inventory.setItem(slots[index++], headItem(targetUUID, "&c", "Target"));
        }
        if (targets.isEmpty()) {
            inventory.setItem(
                    slots[0],
                    namedItem(
                            MaterialCatalog.stack("BARRIER", Material.REDSTONE_BLOCK),
                            "&cNo targets",
                            Collections.singletonList("&7Targets appear when the bot has one")));
        }
    }

    private void renderCombat() {
        ItemStack border = namedItem(borderStack(), " ", Collections.<String>emptyList());
        int[] combatBorders = {10, 17, 19, 26, 28, 35, 37, 44, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : combatBorders) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(SLOT_COMBAT_MODE_C, combatModeItem());
        inventory.setItem(SLOT_DIFFICULTY_C, difficultyItem());
        inventory.setItem(SLOT_RESET_TUNING, resetTuningItem());
        CombatTuningProperty[] properties = CombatTuningProperty.values();
        for (int i = 0; i < SLOT_TUNING.length && i < properties.length; i++) {
            inventory.setItem(SLOT_TUNING[i], tuningItem(properties[i]));
        }
    }

    /**
     * Content `x` cells from NewBotGUI TabGui structure (cols 1–8 under the tab chrome).
     */
    private static int[] contentSlots() {
        return new int[] {
            10, 11, 12, 13, 14, 15, 16, 17,
            19, 20, 21, 22, 23, 24, 25, 26,
            28, 29, 30, 31, 32, 33, 34, 35,
            37, 38, 39, 40, 41, 42, 43, 44,
            46, 47, 48, 49, 50, 51, 52, 53
        };
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!isOurInventory(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
        if (!player.equals(event.getWhoClicked())) {
            return;
        }
        if (event.getClickedInventory() == null || !isOurInventory(event.getClickedInventory())) {
            return;
        }
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        if (slot == SLOT_TAB_KIT) {
            activeTab = TAB_KIT;
            render();
            return;
        }
        if (slot == SLOT_TAB_ARMOR) {
            activeTab = TAB_ARMOR;
            render();
            return;
        }
        if (slot == SLOT_TAB_OWNERS) {
            activeTab = TAB_OWNERS;
            render();
            return;
        }
        if (slot == SLOT_TAB_TARGETS) {
            activeTab = TAB_TARGETS;
            render();
            return;
        }
        if (slot == SLOT_TAB_COMBAT) {
            activeTab = TAB_COMBAT;
            render();
            return;
        }

        if (activeTab == TAB_KIT) {
            handleKitClick(slot, click);
        } else if (activeTab == TAB_ARMOR) {
            handleArmorClick(slot, click);
        } else if (activeTab == TAB_COMBAT) {
            handleCombatClick(slot, click);
        }
    }

    private void handleKitClick(int slot, ClickType click) {
        if (slot == SLOT_COMBAT_MODE) {
            cycleCombatMode(click);
        } else if (slot == SLOT_DIFFICULTY) {
            cycleDifficulty(click);
        } else if (slot == SLOT_COMBAT) {
            toggleCombat();
        } else if (slot == SLOT_FOLLOW) {
            toggleFollow();
        } else if (slot == SLOT_TOTEM && MaterialCatalog.feature(PlatformCapability.TOTEM)) {
            cycleTotem(click);
        } else if (slot == SLOT_TARGET_MODE) {
            cycleTargetMode();
        } else if (slot == SLOT_SPAWN) {
            handleSpawnOrDespawn();
        } else if (slot == SLOT_TELEPORT) {
            handleTeleport();
        }
    }

    private void handleArmorClick(int slot, ClickType click) {
        EquipmentSlot piece = null;
        if (slot == SLOT_ARMOR_HEAD) {
            piece = EquipmentSlot.HEAD;
        } else if (slot == SLOT_ARMOR_CHEST) {
            piece = EquipmentSlot.CHEST;
        } else if (slot == SLOT_ARMOR_LEGS) {
            piece = EquipmentSlot.LEGS;
        } else if (slot == SLOT_ARMOR_FEET) {
            piece = EquipmentSlot.FEET;
        }
        if (piece == null) {
            return;
        }
        if (click.isLeftClick()) {
            cycleArmor(piece);
        } else if (click.isRightClick()) {
            toggleBlast(piece);
        }
    }

    private void handleCombatClick(int slot, ClickType click) {
        if (slot == SLOT_COMBAT_MODE_C) {
            cycleCombatMode(click);
            return;
        }
        if (slot == SLOT_DIFFICULTY_C) {
            cycleDifficulty(click);
            return;
        }
        if (slot == SLOT_RESET_TUNING) {
            options.resetCombatTuning();
            player.sendMessage(ChatColorUtils.translate("&aCombat profile reset to defaults."));
            render();
            return;
        }
        CombatTuningProperty[] properties = CombatTuningProperty.values();
        for (int i = 0; i < SLOT_TUNING.length && i < properties.length; i++) {
            if (SLOT_TUNING[i] == slot) {
                adjustTuning(properties[i], click);
                return;
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (isOurInventory(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!isOurInventory(event.getInventory()) || !player.equals(event.getPlayer())) {
            return;
        }
        if (!closing) {
            plugin.getPlayerOptions().put(player.getUniqueId(), options);
        }
        unregisterListener();
    }

    private void cycleCombatMode(ClickType click) {
        if (!options.isChangeableCombatMode()) {
            player.sendMessage(ChatColorUtils.translate("&cThe combat mode is locked for this bot."));
            return;
        }
        if (!click.isLeftClick() && !click.isRightClick()) {
            return;
        }
        CombatMode current = options.getCombatMode();
        CombatMode next = options.nextCombatMode(player, click.isLeftClick());
        Optional<CombatMode> proposed = BotSettingEvents.propose(
                plugin,
                player.getUniqueId(),
                BotEventSource.GUI,
                BotSettingKey.COMBAT_MODE,
                current,
                next,
                CombatMode.class);
        if (!proposed.isPresent()) {
            return;
        }
        options.setCombatMode(proposed.get());
        if (options.getCombatMode().builtIn()) {
            CombatModeLoadoutDefaults.applyArmor(options, options.getCombatMode());
        }
        plugin.getBotManager().updateArmor(resolveManagedOwnerUUID(), options.getArmor(), options.getBlast());
        player.sendMessage(ChatColorUtils.translate("&aCombat mode set to &e" + options.getCombatModeDisplayName()));
        render();
    }

    private void cycleDifficulty(ClickType click) {
        if (!options.isChangeableDifficulty()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.difficulty-locked", "&cDifficulty is locked: it cannot be modified for this bot.")));
            return;
        }
        DifficultyLevel current = options.getDifficulty();
        DifficultyLevel next = current;
        if (click == ClickType.LEFT || click.isLeftClick()) {
            next = options.nextAllowedDifficulty(current, true);
        } else if (click == ClickType.RIGHT || click.isRightClick()) {
            next = options.nextAllowedDifficulty(current, false);
        } else {
            return;
        }
        UUID owner = resolveManagedOwnerUUID();
        Optional<DifficultyLevel> proposed = BotSettingEvents.propose(
                plugin, owner, BotEventSource.GUI, BotSettingKey.DIFFICULTY, current, next, DifficultyLevel.class);
        if (!proposed.isPresent()) {
            return;
        }
        next = proposed.get();
        options.setDifficulty(next);
        plugin.getBotManager().setDifficultyLevel(owner, next);
        player.sendMessage(ChatColorUtils.translate("&aDifficulty set to &e" + next.getSelectedName()));
        render();
    }

    private void toggleCombat() {
        if (!options.isChangeableCombat()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.combat-locked", "&cCombat is locked: it cannot be modified for this bot.")));
            return;
        }
        boolean oldStatus = options.isCombat();
        boolean newStatus = !oldStatus;
        if (newStatus && !options.isFollow()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.combat-need-follow", "&cFollow must be ON to enable bot combat")));
            return;
        }
        UUID owner = resolveManagedOwnerUUID();
        Optional<Boolean> proposed = BotSettingEvents.propose(
                plugin, owner, BotEventSource.GUI, BotSettingKey.COMBAT, oldStatus, newStatus, Boolean.class);
        if (!proposed.isPresent()) {
            return;
        }
        newStatus = proposed.get().booleanValue();
        options.setCombat(newStatus);
        plugin.getBotManager().updateCombat(owner, newStatus);
        player.sendMessage(ChatColorUtils.translate("&eCombat " + (newStatus ? "&aON" : "&cOFF")));
        render();
    }

    private void toggleFollow() {
        if (!options.isChangeableFollow()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.follow-locked", "&cFollow is locked: it cannot be modified for this bot.")));
            return;
        }
        boolean oldFollow = options.isFollow();
        boolean combatOn = options.isCombat();
        boolean newFollow = !oldFollow;
        if (!newFollow && combatOn && !options.isChangeableCombat()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.follow-lock-combat", "&cYou cannot disable follow: combat is locked to ON.")));
            return;
        }
        UUID owner = resolveManagedOwnerUUID();
        Optional<Boolean> proposedFollow = BotSettingEvents.propose(
                plugin, owner, BotEventSource.GUI, BotSettingKey.FOLLOW, oldFollow, newFollow, Boolean.class);
        if (!proposedFollow.isPresent()) {
            return;
        }
        newFollow = proposedFollow.get().booleanValue();
        Optional<Boolean> proposedCombat = Optional.empty();
        if (!newFollow && combatOn) {
            proposedCombat = BotSettingEvents.propose(
                    plugin, owner, BotEventSource.GUI, BotSettingKey.COMBAT, true, false, Boolean.class);
            if (!proposedCombat.isPresent()) {
                return;
            }
        }
        options.setFollow(newFollow);
        plugin.getBotManager().updateFollow(owner, newFollow);
        if (!newFollow && combatOn) {
            boolean combatOff = proposedCombat.get().booleanValue();
            options.setCombat(combatOff);
            plugin.getBotManager().updateCombat(owner, combatOff);
        }
        render();
    }

    private void cycleTotem(ClickType click) {
        if (!options.isChangeableTotem()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.totem-locked", "&cTotems are locked: they cannot be modified for this bot.")));
            return;
        }
        int maxTotem = options.getMaxTotemCount();
        int minTotem = options.getMinTotemCount();
        int current = options.getTotems();
        int next = current;
        if (click.isLeftClick() && current < maxTotem) {
            next++;
        }
        if (click.isRightClick() && current > minTotem) {
            next--;
        }
        if (next == current) {
            return;
        }
        UUID owner = resolveManagedOwnerUUID();
        Optional<Integer> proposed = BotSettingEvents.propose(
                plugin, owner, BotEventSource.GUI, BotSettingKey.TOTEM_COUNT, current, next, Integer.class);
        if (plugin.getBotRegistry().getBot(owner) != null && !proposed.isPresent()) {
            return;
        }
        if (proposed.isPresent()) {
            next = proposed.get().intValue();
        }
        options.setTotems(next);
        plugin.getBotManager().updateTotem(owner, next);
        render();
    }

    private void cycleTargetMode() {
        BotTargetMode next = options.getTargetMode().next();
        UUID ownerUUID = resolveManagedOwnerUUID();
        Optional<BotTargetMode> proposed = BotSettingEvents.propose(
                plugin,
                ownerUUID,
                BotEventSource.GUI,
                BotSettingKey.TARGET_MODE,
                options.getTargetMode(),
                next,
                BotTargetMode.class);
        if (plugin.getBotRegistry().getBot(ownerUUID) != null && !proposed.isPresent()) {
            return;
        }
        if (proposed.isPresent()) {
            next = proposed.get();
        }
        options.setTargetMode(next);
        ITrainingBot bot = plugin.getBotManager().getBotSafe(ownerUUID);
        if (bot != null && bot.getBotAI() != null) {
            bot.getBotAI().getMovementController().clearPath();
            bot.getBotAI().clearActivePathfinding();
        }
        player.sendMessage(ChatColorUtils.translate("&aAttack mode: &e" + targetModeLabel(next)));
        render();
    }

    private void cycleArmor(EquipmentSlot slot) {
        if (!options.isChangeableArmor()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.armor-locked", "&cArmor is locked: it cannot be modified for this bot.")));
            return;
        }
        ItemStack current = options.getArmor().get(slot);
        Material currentMat = current == null ? Material.AIR : current.getType();
        Material next =
                ArmorCycle.getNextArmor(currentMat, slot, options.getMinArmorTier(), options.getMaxArmorTier());
        boolean blast = options.getBlast().getOrDefault(slot, Boolean.FALSE).booleanValue();
        ItemStack updated = new ItemStack(next);
        BotEquipmentUtils.applyArmorEnchants(updated, blast);
        options.getArmor().put(slot, updated);
        plugin.getBotManager().updateArmor(resolveManagedOwnerUUID(), options.getArmor(), options.getBlast());
        render();
    }

    private void toggleBlast(EquipmentSlot slot) {
        if (!options.isChangeableBlast()) {
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.blast-locked",
                    "&cBlast protection is locked: it cannot be modified for this bot.")));
            return;
        }
        boolean next = !options.getBlast().getOrDefault(slot, Boolean.FALSE).booleanValue();
        options.getBlast().put(slot, Boolean.valueOf(next));
        ItemStack current = options.getArmor().get(slot);
        if (current != null) {
            ItemStack updated = new ItemStack(current.getType());
            BotEquipmentUtils.applyArmorEnchants(updated, next);
            options.getArmor().put(slot, updated);
        }
        plugin.getBotManager().updateArmor(resolveManagedOwnerUUID(), options.getArmor(), options.getBlast());
        render();
    }

    private void adjustTuning(CombatTuningProperty property, ClickType click) {
        if (!click.isLeftClick() && !click.isRightClick()) {
            return;
        }
        CombatTuning current = options.getCombatTuning();
        CombatTuning adjusted = property.adjust(current, click.isLeftClick(), click.isShiftClick());
        options.setCustomCombatTuning(adjusted);
        player.sendMessage(ChatColorUtils.translate(
                "&e" + property.displayName() + ": &f" + property.formattedValue(adjusted)));
        render();
    }

    private void handleSpawnOrDespawn() {
        if (isManagedBotSpawned()) {
            UUID managedOwnerUUID = resolveManagedOwnerUUID();
            if (!plugin.getBotManager().despawnByOwnerUUID(managedOwnerUUID)) {
                return;
            }
            clearCachedOptionsAfterDespawn(managedOwnerUUID);
            player.sendMessage(ChatColorUtils.translate(
                    plugin.getLangString("messages.despawn-bot", "&cBot removed!")));
            closeQuietly(false);
            return;
        }
        if (options.getBotType() == BotType.EVENT) {
            if (isBotEventActive()) {
                player.sendMessage(ChatColorUtils.translate(
                        plugin.getLangString("messages.event-bot-already-active")));
                return;
            }
            plugin.getBotManager().despawnAll();
            player.sendMessage(ChatColorUtils.translate(plugin.getLangString(
                    "messages.all-normal-bots-despawned",
                    "&eAll normal bots have been despawned for the event")));
        } else if (isBotEventActive()) {
            player.sendMessage(ChatColorUtils.translate(
                    plugin.getLangString("messages.cannot-spawn-normal-during-event")));
            return;
        }

        plugin.getPlayerOptions().put(player.getUniqueId(), options);
        closeQuietly();
        boolean spawned = plugin.getBotManager()
                .spawn(player, options.getArmor(), options.getBlast(), options.isFollow(), options.getTotems(), options);
        if (!spawned) {
            return;
        }
        player.sendMessage(ChatColorUtils.translate(
                plugin.getLangString("messages.spawn-bot", "&aBot spawned with the selected settings!")));
        if (options.getBotType() == BotType.TEAM_ALLY) {
            notifyTeamOwners();
        }
    }

    private void handleTeleport() {
        UUID botOwnerUUID = resolveManagedOwnerUUID();
        if (!plugin.getBotManager().isBotSpawned(botOwnerUUID)) {
            return;
        }
        ITrainingBot bot = plugin.getBotManager().getBot(botOwnerUUID);
        if (bot == null) {
            return;
        }
        Location loc = player.getLocation();
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        NMSBridgeManager.get().moveBot(bot.asBukkitPlayer(), loc.getX(), loc.getY(), loc.getZ());
    }

    private ItemStack combatModeItem() {
        Material mat = MaterialCatalog.optional(options.getCombatModeIconMaterial(), Material.DIAMOND_SWORD);
        List<String> lore = Arrays.asList(
                "&7Difficulty: &f" + options.getDifficulty().name(),
                "&eLeft click: &7next mode",
                "&eRight click: &7previous mode");
        return namedItem(mat, "&6Combat mode: &e" + options.getCombatModeDisplayName(), lore);
    }

    private ItemStack difficultyItem() {
        Material mat = difficultyMaterial(options.getDifficulty());
        List<String> lore = new ArrayList<String>();
        lore.add("&7Current: &e" + options.getDifficulty().getSelectedName());
        lore.add("&eLeft click: &7next");
        lore.add("&eRight click: &7previous");
        for (DifficultyLevel level : options.getAllowedDifficulties()) {
            String line = level == options.getDifficulty() ? level.getSelectedName() : level.getDisplayName();
            lore.add("- " + line);
        }
        return namedItem(mat, plugin.getLangString("gui.difficulty-button.name", "&eDifficulty"), lore);
    }

    private ItemStack combatToggleItem() {
        boolean on = options.isCombat();
        List<String> lore = Arrays.asList("&7Status: " + (on ? "&aON" : "&cOFF"), "&eClick: &7toggle");
        return namedItem(
                Material.DIAMOND_SWORD, plugin.getLangString("gui.combat-button.name", "&cCombat"), lore);
    }

    private ItemStack followItem() {
        boolean on = options.isFollow();
        ItemStack icon = MaterialCatalog.stack("LEAD", Material.STRING);
        List<String> lore = Arrays.asList("&7Status: " + (on ? "&aON" : "&cOFF"), "&eClick: &7toggle");
        return namedItem(icon, plugin.getLangString("gui.follow-button.name", "&aFollow"), lore);
    }

    private ItemStack totemItem() {
        // Spigot 1.12.2: TOTEM (not TOTEM_OF_UNDYING); GOLDEN_APPLE only pre-1.11 fallback.
        ItemStack icon = MaterialCatalog.stack("TOTEM_OF_UNDYING", Material.GOLDEN_APPLE);
        String unlimited = plugin.getLangString("gui.totem-button.unlimited-text", "Unlimited");
        String count = options.getTotems() == -1 ? unlimited : String.valueOf(options.getTotems());
        List<String> lore =
                Arrays.asList("&7Count: &e" + count, "&eLeft click: &7+", "&eRight click: &7-");
        return namedItem(icon, plugin.getLangString("gui.totem-button.name", "&6Totems"), lore);
    }

    private ItemStack targetModeItem() {
        BotTargetMode mode = options.getTargetMode();
        List<String> lore = Arrays.asList(
                plugin.getLangString("gui.target-mode-button.current", "&7Current: &e%mode%")
                        .replace("%mode%", targetModeLabel(mode)),
                plugin.getLangString("gui.target-mode-button.click", "&aClick to change"));
        return namedItem(
                targetModeStack(mode),
                plugin.getLangString("gui.target-mode-button.name", "&bAttack mode"),
                lore);
    }

    private ItemStack spawnItem() {
        boolean spawned = isManagedBotSpawned();
        ItemStack icon = spawned
                ? MaterialCatalog.stack("BARRIER", Material.REDSTONE_BLOCK)
                : MaterialCatalog.stack("PLAYER_HEAD", Material.STONE);
        String name = spawned
                ? plugin.getLangString("gui.despawn-button.name", "&cDespawn bot")
                : plugin.getLangString("gui.spawn-button.name", "&aSpawn bot");
        List<String> lore = spawned
                ? plugin.getLangStringList("gui.despawn-button.lore")
                : plugin.getLangStringList("gui.spawn-button.lore");
        if (lore == null || lore.isEmpty()) {
            lore = Collections.singletonList(spawned ? "&7Click to remove the bot" : "&7Click to spawn the bot");
        }
        return namedItem(icon, name, lore);
    }

    private ItemStack teleportItem() {
        Material mat = MaterialCatalog.optional("ENDER_PEARL", Material.ENDER_PEARL);
        List<String> lore = plugin.getLangStringList("gui.teleport-button.lore");
        if (lore == null || lore.isEmpty()) {
            lore = Collections.singletonList("&7Teleport the bot to you");
        }
        return namedItem(mat, plugin.getLangString("gui.teleport-button.name", "&bTeleport bot"), lore);
    }

    private ItemStack armorPieceItem(EquipmentSlot slot) {
        ItemStack piece = options.getArmor().get(slot);
        Material mat = piece == null ? Material.IRON_CHESTPLATE : piece.getType();
        boolean blast = options.getBlast().getOrDefault(slot, Boolean.FALSE).booleanValue();
        ItemStack display = new ItemStack(mat);
        BotEquipmentUtils.applyArmorEnchants(display, blast);
        List<String> lore = Arrays.asList(
                "&7Type: &f" + formatMaterialName(mat),
                "&7Blast: " + (blast ? "&aON" : "&cOFF"),
                "&eLeft click: &7cycle armor",
                "&eRight click: &7toggle blast");
        ItemStack named = namedItem(mat, "&b" + slot.name(), lore);
        named.addUnsafeEnchantments(display.getEnchantments());
        return named;
    }

    private ItemStack resetTuningItem() {
        return namedItem(
                MaterialCatalog.stack("BARRIER", Material.REDSTONE_BLOCK),
                "&cReset combat profile",
                Collections.singletonList("&7Restore default tuning for this mode/difficulty"));
    }

    private ItemStack tuningItem(CombatTuningProperty property) {
        CombatTuning tuning = options.getCombatTuning();
        List<String> lore = Arrays.asList(
                "&7Value: &f" + property.formattedValue(tuning),
                "&eLeft click: &7+",
                "&eRight click: &7-",
                "&eShift: &7x5 step");
        return namedItem(property.material(), "&e" + property.displayName(), lore);
    }

    private ItemStack headItem(UUID uuid, String colorPrefix, String role) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName() != null ? offline.getName() : uuid.toString();
        // Spigot 1.12.2: SKULL_ITEM + durability 3 = player head (not skeleton = 0).
        ItemStack skull = MaterialCatalog.stack("PLAYER_HEAD", Material.STONE);
        ItemMeta meta = skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColorUtils.translate(colorPrefix + name));
            meta.setLore(Arrays.asList(
                    ChatColorUtils.translate("&7" + role),
                    ChatColorUtils.translate("&8" + uuid.toString())));
            if (meta instanceof SkullMeta) {
                applySkullOwner((SkullMeta) meta, offline, name);
            }
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private static void applySkullOwner(SkullMeta meta, OfflinePlayer offline, String name) {
        try {
            meta.setOwningPlayer(offline);
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // Pre-1.12 SkullMeta: setOwner(String).
            try {
                SkullMeta.class.getMethod("setOwner", String.class).invoke(meta, name);
            } catch (ReflectiveOperationException ignoredAgain) {
                // Leave default skull texture.
            }
        }
    }

    private List<UUID> resolveOwners() {
        Set<UUID> owners = new LinkedHashSet<UUID>();
        owners.addAll(options.getTeamOwnerUUIDs());
        if (options.getOwnerUUID() != null) {
            owners.add(options.getOwnerUUID());
        }
        if (owners.isEmpty()) {
            owners.add(resolveManagedOwnerUUID());
        }
        return new ArrayList<UUID>(owners);
    }

    private List<UUID> resolveTargets() {
        Set<UUID> targets = new LinkedHashSet<UUID>();
        for (UUID targetUUID : options.getTargetUUIDs()) {
            if (!isProtectedOwner(targetUUID)) {
                targets.add(targetUUID);
            }
        }
        if (options.getPreferredTargetUUID() != null && !isProtectedOwner(options.getPreferredTargetUUID())) {
            targets.add(options.getPreferredTargetUUID());
        }
        if (targets.isEmpty()) {
            ITrainingBot managedBot = plugin.getBotManager().getBotSafe(resolveManagedOwnerUUID());
            if (managedBot != null) {
                Player targetPlayer = managedBot.getTargetPlayer();
                if (targetPlayer == null && managedBot.getBrainController() != null) {
                    targetPlayer = managedBot.getBrainController().getTargetPlayer();
                }
                if (targetPlayer != null && !isProtectedOwner(targetPlayer.getUniqueId())) {
                    targets.add(targetPlayer.getUniqueId());
                }
            }
        }
        return new ArrayList<UUID>(targets);
    }

    private boolean isProtectedOwner(UUID candidateUUID) {
        if (candidateUUID == null) {
            return false;
        }
        if (botType == BotType.ALLY) {
            UUID ownerUUID = options.getOwnerUUID();
            return ownerUUID != null && ownerUUID.equals(candidateUUID);
        }
        if (botType == BotType.TEAM_ALLY) {
            return options.getTeamOwnerUUIDs().contains(candidateUUID);
        }
        return false;
    }

    private Material difficultyMaterial(DifficultyLevel difficulty) {
        String configPath =
                "gui.difficulty-button.difficulties-mat." + difficulty.name().toLowerCase(Locale.ROOT);
        String materialName = plugin.getLangString(configPath);
        if (materialName != null && !materialName.trim().isEmpty()) {
            Material matched = MaterialCatalog.optional(materialName, Material.AIR);
            if (matched != Material.AIR) {
                return matched;
            }
        }
        return MaterialCatalog.optional(
                plugin.getLangString("gui.difficulty-button.material", "DIAMOND_SWORD"), Material.DIAMOND_SWORD);
    }

    private ItemStack borderStack() {
        String configured =
                plugin.getLangString("gui.tab-border.material", "BLACK_STAINED_GLASS_PANE");
        // Spigot 1.12.2: STAINED_GLASS_PANE + data 15 = black (data 0 = white).
        return MaterialCatalog.stack(configured, Material.STONE);
    }

    private static ItemStack targetModeStack(BotTargetMode mode) {
        switch (mode) {
            case PLAYERS:
                return MaterialCatalog.stack("PLAYER_HEAD", Material.STONE);
            case MOBS:
                return MaterialCatalog.stack("ZOMBIE_HEAD", Material.ROTTEN_FLESH);
            case PLAYERS_AND_MOBS:
            default:
                return new ItemStack(Material.IRON_SWORD);
        }
    }

    private static String targetModeLabel(BotTargetMode mode) {
        switch (mode) {
            case PLAYERS:
                return "Players";
            case MOBS:
                return "Mobs";
            case PLAYERS_AND_MOBS:
            default:
                return "Players + Mobs";
        }
    }

    private static String formatMaterialName(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_", -1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        return namedItem(new ItemStack(material == null ? Material.STONE : material), name, lore);
    }

    private ItemStack namedItem(ItemStack base, String name, List<String> lore) {
        ItemStack stack = base == null ? new ItemStack(Material.STONE) : base.clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColorUtils.translate(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> colored = new ArrayList<String>(lore.size());
                for (String line : lore) {
                    colored.add(ChatColorUtils.translate(line));
                }
                meta.setLore(colored);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private BotOptions resolveOptions() {
        BotOptions existing = plugin.getPlayerOptions().getOptions(player.getUniqueId());
        if (existing == null) {
            UUID managedOwner = resolveManagedOwnerUUIDForLookup();
            ITrainingBot activeBot = plugin.getBotManager().getBotSafe(managedOwner);
            if (activeBot != null
                    && activeBot.getBrainController() != null
                    && activeBot.getBrainController().getBotOptions() != null) {
                existing = activeBot.getBrainController().getBotOptions();
            }
        }
        if (existing == null) {
            existing = new BotOptions(
                    plugin, ArmorCycle.getDefaultArmorFromConfig(plugin.getLanguageConfig(), plugin));
            CombatModeLoadoutDefaults.applyArmor(existing, existing.getCombatMode());
        }
        return existing;
    }

    private UUID resolveManagedOwnerUUIDForLookup() {
        if (botType != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = plugin.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }

    private UUID resolveManagedOwnerUUID() {
        if (options.getBotType() != BotType.TEAM_ALLY) {
            return player.getUniqueId();
        }
        UUID teamOwnerUUID = plugin.getBotManager().findTeamAllyPrimaryOwner(player.getUniqueId());
        return teamOwnerUUID == null ? player.getUniqueId() : teamOwnerUUID;
    }

    private boolean isManagedBotSpawned() {
        if (options.getBotType() == BotType.TEAM_ALLY) {
            return plugin.getBotManager().hasActiveTeamAlly(player.getUniqueId());
        }
        return plugin.getBotManager().isBotSpawned(player.getUniqueId());
    }

    private void clearCachedOptionsAfterDespawn(UUID managedOwnerUUID) {
        plugin.getPlayerOptions().remove(player.getUniqueId());
        plugin.getPlayerOptions().remove(managedOwnerUUID);
        for (UUID teamOwnerUUID : options.getTeamOwnerUUIDs()) {
            plugin.getPlayerOptions().remove(teamOwnerUUID);
        }
    }

    private void notifyTeamOwners() {
        String template = plugin.getLangString(
                "messages.team-ally.team-spawned-notify",
                "&aAllied bot spawned by %playerowner% for the team with these owners: %playerlist%");
        List<String> ownerNames = new ArrayList<String>();
        for (UUID ownerUUID : options.getTeamOwnerUUIDs()) {
            String name = Bukkit.getOfflinePlayer(ownerUUID).getName();
            ownerNames.add(name == null ? ownerUUID.toString() : name);
        }
        String message = template
                .replace("%playerowner%", player.getName())
                .replace("%playerlist%", String.join(", ", ownerNames));
        for (UUID ownerUUID : options.getTeamOwnerUUIDs()) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(ChatColorUtils.translate(message));
            }
        }
    }

    private boolean isBotEventActive() {
        for (UUID ownerUUID : plugin.getBotRegistry().getAllBots().keySet()) {
            ITrainingBot bot = plugin.getBotManager().getBotSafe(ownerUUID);
            if (bot != null && bot.getBrainController() != null) {
                BotOptions botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private String title() {
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

    private boolean isOurInventory(@Nullable Inventory other) {
        return other != null && other.getHolder() == this;
    }

    private void closeQuietly() {
        closeQuietly(true);
    }

    private void closeQuietly(boolean persistDraft) {
        closing = true;
        if (persistDraft) {
            plugin.getPlayerOptions().put(player.getUniqueId(), options);
        }
        player.closeInventory();
        unregisterListener();
    }

    private void unregisterListener() {
        if (listenerRegistered) {
            HandlerList.unregisterAll(this);
            listenerRegistered = false;
        }
    }
}
