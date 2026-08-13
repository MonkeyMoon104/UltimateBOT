package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.compat.BlockDamageAccess;
import com.monkey.ultimatebot.world.WorldProtectionService;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

public final class CombatModeContext implements AutoCloseable {
    private final ITrainingBot bot;
    private final org.bukkit.entity.Player bukkitBot;
    private final BotOptions options;
    private final BotMovementController movement;
    private final BotInventoryController inventory;
    private final BotCPVPController crystal;
    private final BotRAPVPController anchor;
    private final ICombatStrategyExecutor legacyCombat;
    private final WorldProtectionService worldProtection;
    private final ModeInventorySession inventorySession;
    private final ModeEntityTracker entities;
    private final ModeBlockTracker blocks;
    private final ModeProjectileService projectiles;
    private final ModeMotionService motion;
    private final ModeCombatActions actions;
    private final ModeCombatSignals signals;
    private final SplittableRandom random;

    public CombatModeContext(
            ITrainingBot bot,
            BotOptions options,
            BotMovementController movement,
            BotRotationController rotation,
            BotAttackController attack,
            BotInventoryController inventory,
            BotCPVPController crystal,
            BotRAPVPController anchor,
            ICombatStrategyExecutor legacyCombat,
            WorldProtectionService worldProtection) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.bukkitBot = bot.asBukkitPlayer();
        this.options = Objects.requireNonNull(options, "options");
        this.movement = Objects.requireNonNull(movement, "movement");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.crystal = Objects.requireNonNull(crystal, "crystal");
        this.anchor = Objects.requireNonNull(anchor, "anchor");
        this.legacyCombat = Objects.requireNonNull(legacyCombat, "legacyCombat");
        this.worldProtection = Objects.requireNonNull(worldProtection, "worldProtection");
        this.inventorySession = new ModeInventorySession(inventory);
        this.entities = new ModeEntityTracker();
        this.blocks = new ModeBlockTracker();
        this.random = new SplittableRandom(
                bot.getUniqueId().getMostSignificantBits() ^ bot.getUniqueId().getLeastSignificantBits());
        this.projectiles = new ModeProjectileService(bukkitBot, entities, random);
        this.motion = new ModeMotionService(bot, bukkitBot, movement, rotation, this::tuning, random);
        this.actions = new ModeCombatActions(bot, bukkitBot, attack, inventory, this::tuning);
        this.signals = new ModeCombatSignals();
    }

    public CombatTuning tuning() {
        return options.getCombatTuning();
    }

    public BotOptions options() {
        return options;
    }

    public ITrainingBot bot() {
        return bot;
    }

    public org.bukkit.entity.Player bukkitBot() {
        return bukkitBot;
    }

    public BotInventoryController inventory() {
        return inventory;
    }

    public BotMovementController movement() {
        return movement;
    }

    public BotCPVPController crystal() {
        return crystal;
    }

    public BotRAPVPController anchor() {
        return anchor;
    }

    public ModeProjectileService projectiles() {
        return projectiles;
    }

    public ModeEntityTracker entities() {
        return entities;
    }

    public ModeMotionService motion() {
        return motion;
    }

    public ModeCombatActions actions() {
        return actions;
    }

    public ModeCombatSignals signals() {
        return signals;
    }

    public SplittableRandom random() {
        return random;
    }

    public void applyKit(ModeKit kit) {
        inventorySession.apply(kit, options);
    }

    public void clearTransientState() {
        entities.close();
        blocks.close();
        signals.clear();
    }

    public boolean placeTemporaryBlock(Location location, Material material) {
        return blocks.place(location, material);
    }

    public boolean canPlaceTemporaryBlock(Location location, Material material) {
        return blocks.canPlace(location, material);
    }

    public boolean canPlaceCombatBlock(Location location, Material material) {
        return worldProtection.canPlaceCombatBlock(location, material);
    }

    public boolean placeCombatBlock(Location location, Material material, int inventorySlot) {
        boolean placed = worldProtection.placeCombatBlock(
                location, material, bukkitBot, () -> inventory.consumeItem(inventorySlot));
        if (placed) {
            invalidateMovementForWorldChange();
        }
        return placed;
    }

    public boolean placeCombatBlock(Location location, Material material, Material placementItem, int inventorySlot) {
        boolean placed = worldProtection.placeCombatBlock(
                location, material, placementItem, bukkitBot, () -> inventory.consumeItem(inventorySlot));
        if (placed) {
            invalidateMovementForWorldChange();
        }
        return placed;
    }

    public boolean trackCombatEntity(Entity entity, int inventorySlot) {
        return worldProtection.trackCombatEntity(
                entity, options.canExplosionDamageBlocks(), () -> inventory.consumeItem(inventorySlot));
    }

    /** Tracks a combat entity without consuming inventory (caller already consumed / best-effort). */
    public boolean trackCombatEntity(Entity entity) {
        return worldProtection.trackCombatEntity(
                entity, options.canExplosionDamageBlocks(), () -> true);
    }

    public boolean breakCombatBlock(Location location, int toolSlot) {
        inventory.switchToSlot(toolSlot);
        boolean broken = worldProtection.breakCombatBlock(
                location, bukkitBot, inventory.getItem(toolSlot));
        if (broken) {
            actions.swingMainHand();
            invalidateMovementForWorldChange();
        }
        return broken;
    }

    public void showBlockBreakProgress(Location location, float progress) {
        Location checkedLocation = Objects.requireNonNull(location, "location");
        int sourceId = bukkitBot.getEntityId();
        for (org.bukkit.entity.Player viewer : checkedLocation.getWorld().getPlayers()) {
            BlockDamageAccess.send(viewer, checkedLocation, progress, sourceId);
        }
    }

    public void restoreCombatBlock(Location location) {
        worldProtection.restoreCombatBlock(location);
        invalidateMovementForWorldChange();
    }

    public void restoreTemporaryBlock(Location location) {
        blocks.restore(location);
    }

    public void legacyCombat(org.bukkit.entity.Player target) {
        legacyCombat.executeCombatStrategy(target);
    }

    private void invalidateMovementForWorldChange() {
        movement.clearCache();
        movement.clearPath();
    }

    @Override
    public void close() {
        actions.releaseUseItem();
        anchor.disable();
        crystal.setEnabled(false);
        entities.close();
        blocks.close();
        signals.clear();
        inventorySession.close();
        movement.resetCombatState();
        motion.setSwimming(false);
    }
}
