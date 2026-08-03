package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.world.WorldProtectionService;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

final class CombatModeContext implements AutoCloseable {
    private final Player bot;
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
    private final RandomGenerator random;

    CombatModeContext(
            Player bot,
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
        if (!(bot.getBukkitEntity() instanceof org.bukkit.entity.Player player)) {
            throw new IllegalArgumentException("Combat bot must expose a Bukkit player entity");
        }
        this.bukkitBot = player;
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
                bot.getUUID().getMostSignificantBits() ^ bot.getUUID().getLeastSignificantBits());
        this.projectiles = new ModeProjectileService(bukkitBot, entities, random);
        this.motion = new ModeMotionService(bot, movement, rotation, this::tuning, random);
        this.actions = new ModeCombatActions(bot, bukkitBot, attack, inventory, this::tuning);
        this.signals = new ModeCombatSignals();
    }

    CombatTuning tuning() {
        return options.getCombatTuning();
    }

    BotOptions options() {
        return options;
    }

    Player bot() {
        return bot;
    }

    org.bukkit.entity.Player bukkitBot() {
        return bukkitBot;
    }

    BotInventoryController inventory() {
        return inventory;
    }

    BotMovementController movement() {
        return movement;
    }

    BotCPVPController crystal() {
        return crystal;
    }

    BotRAPVPController anchor() {
        return anchor;
    }

    ModeProjectileService projectiles() {
        return projectiles;
    }

    ModeEntityTracker entities() {
        return entities;
    }

    ModeMotionService motion() {
        return motion;
    }

    ModeCombatActions actions() {
        return actions;
    }

    ModeCombatSignals signals() {
        return signals;
    }

    RandomGenerator random() {
        return random;
    }

    void applyKit(ModeKit kit) {
        inventorySession.apply(kit, options);
    }

    void clearTransientState() {
        entities.close();
        blocks.close();
        signals.clear();
    }

    boolean placeTemporaryBlock(Location location, Material material) {
        return blocks.place(location, material);
    }

    boolean canPlaceTemporaryBlock(Location location, Material material) {
        return blocks.canPlace(location, material);
    }

    boolean canPlaceCombatBlock(Location location, Material material) {
        return worldProtection.canPlaceCombatBlock(location, material);
    }

    boolean placeCombatBlock(Location location, Material material, int inventorySlot) {
        boolean placed = worldProtection.placeCombatBlock(
                location, material, bukkitBot, () -> inventory.consumeItem(inventorySlot));
        if (placed) {
            invalidateMovementForWorldChange();
        }
        return placed;
    }

    boolean placeCombatBlock(Location location, Material material, Material placementItem, int inventorySlot) {
        boolean placed = worldProtection.placeCombatBlock(
                location, material, placementItem, bukkitBot, () -> inventory.consumeItem(inventorySlot));
        if (placed) {
            invalidateMovementForWorldChange();
        }
        return placed;
    }

    boolean breakCombatBlock(Location location, int toolSlot) {
        inventory.switchToSlot(toolSlot);
        boolean broken = worldProtection.breakCombatBlock(
                location, bukkitBot, CraftItemStack.asBukkitCopy(inventory.getItem(toolSlot)));
        if (broken) {
            actions.swingMainHand();
            invalidateMovementForWorldChange();
        }
        return broken;
    }

    void restoreCombatBlock(Location location) {
        worldProtection.restoreCombatBlock(location);
        invalidateMovementForWorldChange();
    }

    void restoreTemporaryBlock(Location location) {
        blocks.restore(location);
    }

    void legacyCombat(Player target) {
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
