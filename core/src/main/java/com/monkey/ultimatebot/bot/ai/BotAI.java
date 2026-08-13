package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.compat.ItemStackAccess;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.behavior.FollowBehaviorController;
import com.monkey.ultimatebot.bot.ai.behavior.IdleBehaviorController;
import com.monkey.ultimatebot.bot.ai.behavior.SustainFoodController;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.CombatDataManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.CombatStateManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.CombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.PathfindingManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatDataManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStateManager;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.IPathfindingManager;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.ultimatebot.bot.ai.controllers.heal.BotHealController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.noobs.BotNoobMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.BotTeleportController;
import com.monkey.ultimatebot.bot.ai.controllers.totem.BotTotemController;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.combat.mode.CombatModeEngine;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.extension.runtime.CoreBotControl;
import com.monkey.ultimatebot.extension.runtime.CoreNativeBotAccess;
import com.monkey.ultimatebot.extension.runtime.CustomBrainRuntime;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public class BotAI {

    public enum CombatState {
        AGGRESSIVE,
        DEFENSIVE,
        REPOSITIONING,
        ANCHOR_SETUP,
        CRYSTAL_SETUP,
        RETREATING
    }

    private final ITrainingBot bot;
    private final Player bukkitBot;
    private final BotNoobMovementController noobMovementController;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final BotTotemController totemController;
    private final BotAttackController attackController;
    private final BotInventoryController inventoryController;
    private final BotEnderpearlController enderpearlController;
    private final BotCPVPController cpvpController;
    private final BotRAPVPController rapvpController;
    private final BotHealController healController;
    private final BotTeleportController teleportController;
    private final ICombatStateManager combatStateManager;
    private final ICombatDataManager combatDataManager;
    private final IPathfindingManager pathfindingManager;
    private final ICombatStrategyExecutor combatStrategyExecutor;
    private final CombatModeEngine combatModeEngine;
    private final IdleBehaviorController idleBehaviorController;
    private final SustainFoodController sustainFoodController;
    private final FollowBehaviorController followBehaviorController;
    private final CustomBrainRuntime customBrainRuntime;
    private final BotOptions options;
    private long lastForcedVerticalTeleportTime = 0L;
    private static final long FORCED_VERTICAL_TELEPORT_COOLDOWN_MS = 3000L;

    public BotAI(ITrainingBot bot, UltimateBot plugin, BotOptions options) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.bukkitBot = bot.asBukkitPlayer();
        this.options = java.util.Objects.requireNonNull(options, "options");
        java.util.Objects.requireNonNull(plugin, "plugin");

        this.noobMovementController = new BotNoobMovementController(bot);
        this.movementController = new BotMovementController(
                bot, plugin.getRuntimeSettings().blockStateCache());
        this.rotationController = new BotRotationController(bot);
        this.totemController = new BotTotemController(bot, plugin);
        this.attackController = new BotAttackController(bot);
        this.inventoryController = new BotInventoryController(bot);
        this.inventoryController.setInfiniteResources(
                plugin.getConfig().getBoolean("bot.combat.infinite-resources", true));
        this.healController = new BotHealController(bot, inventoryController);
        this.teleportController = new BotTeleportController(bot);
        this.enderpearlController = new BotEnderpearlController(bot, inventoryController, rotationController);
        this.enderpearlController.setEnabled(options.isEnderPearls());
        this.cpvpController = new BotCPVPController(bot, inventoryController);
        this.cpvpController.setEnabled(options.isCrystalPvp());
        this.cpvpController.setDifficulty(options.getDifficulty());
        this.rapvpController =
                new BotRAPVPController(bot, inventoryController, rotationController, enderpearlController);
        this.rapvpController.setDifficulty(options.getDifficulty());

        this.combatStateManager = new CombatStateManager(bot, inventoryController, cpvpController, rapvpController);
        this.combatDataManager = new CombatDataManager(bot, enderpearlController, rapvpController, cpvpController);
        this.pathfindingManager = new PathfindingManager(bot, movementController, enderpearlController);
        this.combatStrategyExecutor = new CombatStrategyExecutor(
                bot,
                movementController,
                rotationController,
                attackController,
                inventoryController,
                enderpearlController,
                cpvpController,
                rapvpController,
                combatStateManager,
                combatDataManager);
        this.idleBehaviorController =
                new IdleBehaviorController(bot, options, plugin, movementController, rotationController);
        this.sustainFoodController = new SustainFoodController(bot, options, inventoryController);
        this.followBehaviorController =
                new FollowBehaviorController(bot, movementController, noobMovementController, pathfindingManager);
        CoreBotControl extensionControl =
                new CoreBotControl(bot, movementController, rotationController, attackController, inventoryController);
        CoreNativeBotAccess nativeAccess =
                new CoreNativeBotAccess(
                        com.monkey.ultimatebot.compat.MinecraftVersionAccess.minecraftVersion(),
                        bot,
                        NMSBridgeManager.get());
        this.combatModeEngine = new CombatModeEngine(
                plugin,
                bot,
                options,
                movementController,
                rotationController,
                attackController,
                inventoryController,
                cpvpController,
                rapvpController,
                combatStrategyExecutor,
                plugin.getWorldProtectionService(),
                extensionControl,
                nativeAccess);
        this.customBrainRuntime =
                new CustomBrainRuntime(plugin, bot, options, extensionControl, nativeAccess, inventoryController);
    }

    public void tick(org.bukkit.entity.LivingEntity targetBukkitPlayer) {
        tick(targetBukkitPlayer, true);
    }

    public void tick(org.bukkit.entity.LivingEntity targetBukkitPlayer, boolean allowCombat) {
        syncExplosiveCombat();
        enderpearlController.setEnabled(options.isEnderPearls());
        if (targetBukkitPlayer == null || targetBukkitPlayer.isDead()) return;
        if (handleSustainFood()) {
            return;
        }
        idleBehaviorController.recordTargetSeen();

        boolean combatEnabled = bot.isCombat() && allowCombat;
        boolean followEnabled = bot.isFollow() || options.isFollow();
        if (customBrainRuntime.tick(targetBukkitPlayer, followEnabled, combatEnabled)) {
            return;
        }

        if (!options.isHealing() && healController.isHealing()) {
            healController.resetHealState();
        }
        boolean isCurrentlyHealing = options.isHealing() && healController.isHealing();

        if (!(targetBukkitPlayer instanceof Player)) {
            tickMobTarget(targetBukkitPlayer, combatEnabled);
            return;
        }
        Player playerTarget = (Player) targetBukkitPlayer;

        combatDataManager.updateCombatData(
                playerTarget, combatEnabled && options.getCombatMode().equals(CombatMode.CRYSTAL));

        if (combatStateManager instanceof CombatStateManager) { CombatStateManager stateManager = (CombatStateManager) combatStateManager;
            stateManager.updateDamageData(
                    combatDataManager.getConsecutiveDamageCount(), combatDataManager.getLastDamageTime());
        }

        if (combatEnabled) {
            followBehaviorController.reset();
            if (!isCurrentlyHealing && combatModeEngine.controlsNavigation((org.bukkit.entity.Player) targetBukkitPlayer)) {
                clearActivePathfinding();
                combatStateManager.updateCombatState(playerTarget);
                combatModeEngine.tick((org.bukkit.entity.Player) targetBukkitPlayer);
            } else {
                if (shouldForceVerticalTeleport(playerTarget) && canForceVerticalTeleport()) {
                    boolean teleported =
                            teleportController.teleportSafeNear((org.bukkit.entity.Player) targetBukkitPlayer)
                                    || teleportController.teleportBeside((org.bukkit.entity.Player) targetBukkitPlayer);
                    if (teleported) {
                        lastForcedVerticalTeleportTime = System.currentTimeMillis();
                        clearActivePathfinding();
                        rotationController.updateRotation(targetBukkitPlayer);
                    }
                }

                if (options.isEnderPearls()
                        && enderpearlController.checkAndPerformAutoTeleport(
                                (org.bukkit.entity.Player) targetBukkitPlayer)) {
                    clearActivePathfinding();
                    rotationController.updateRotation(targetBukkitPlayer);
                }
                if (!followActivePath(playerTarget)) {
                    pathfindingManager.checkForStuck(targetBukkitPlayer);
                    enderpearlController.tick();

                    if (pathfindingManager.isUsingPathfinding()) {
                        followActivePath(playerTarget);
                    } else if (bot.isCombat() && !isCurrentlyHealing && allowCombat) {
                        combatStateManager.updateCombatState(playerTarget);
                        combatModeEngine.tick((org.bukkit.entity.Player) targetBukkitPlayer);
                    } else if (isCurrentlyHealing) {
                        combatStateManager.updateCombatState(playerTarget);
                        executeHealingMovement(targetBukkitPlayer);
                    } else {
                        combatStrategyExecutor.basicFollowBehavior(playerTarget);
                    }
                }
            }
        } else if (followEnabled) {
            combatModeEngine.suspend();
            if (!isCurrentlyHealing) {
                followBehaviorController.tick((org.bukkit.entity.Player) targetBukkitPlayer);
            } else {
                followBehaviorController.reset();
                executeHealingMovement(targetBukkitPlayer);
            }
        } else {
            combatModeEngine.suspend();
            followBehaviorController.reset();
            clearActivePathfinding();
            movementController.stopMovement();
            return;
        }

        if (pathfindingManager instanceof PathfindingManager) { PathfindingManager manager = (PathfindingManager) pathfindingManager;
            manager.updateLastBotPosition();
        }

        rotationController.updateRotation(targetBukkitPlayer);
    }

    public void tickIdle() {
        syncExplosiveCombat();
        enderpearlController.setEnabled(options.isEnderPearls());
        if (customBrainRuntime.tick(null, options.isFollow(), options.isCombat())) {
            return;
        }
        if (handleSustainFood()) {
            return;
        }

        idleBehaviorController.tick();
    }

    private void executeHealingMovement(org.bukkit.entity.LivingEntity target) {
        double distance = bot.distanceTo(target);

        if (distance < 3.0) {
            movementController.moveAwayFrom(target, 4.0);
        } else if (distance > 8.0) {
            movementController.moveTowards(target, 5.0);
        } else {
            movementController.setUnderFire(true);
            movementController.maintainDistance(target, distance);
        }
    }

    public void manageTotem() {
        totemController.manageTotem();
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        options.setDifficulty(difficulty);
        this.rapvpController.setDifficulty(difficulty);
        this.cpvpController.setDifficulty(difficulty);
    }

    public DifficultyLevel getDifficulty() {
        return options.getDifficulty();
    }

    public BotMovementController getMovementController() {
        return movementController;
    }

    public BotRotationController getRotationController() {
        return rotationController;
    }

    public BotTotemController getTotemController() {
        return totemController;
    }

    public BotInventoryController getInventoryController() {
        return inventoryController;
    }

    public BotEnderpearlController getEnderpearlController() {
        return enderpearlController;
    }

    public BotCPVPController getCPVPController() {
        return cpvpController;
    }

    public BotRAPVPController getRAPVPController() {
        return rapvpController;
    }

    public BotHealController getHealController() {
        return healController;
    }

    public BotTeleportController getTeleportController() {
        return teleportController;
    }

    public CombatState getCurrentState() {
        return CombatState.valueOf(combatStateManager.getCurrentState().name());
    }

    public void recordShieldImpact(UUID attackerUUID) {
        combatModeEngine.recordShieldImpact(Objects.requireNonNull(attackerUUID, "attackerUUID"));
    }

    public boolean usesCustomBrain() {
        return customBrainRuntime.isConfigured();
    }

    public void customBrainTargetChanged(
            org.bukkit.entity.@Nullable LivingEntity previous, org.bukkit.entity.@Nullable LivingEntity current) {
        customBrainRuntime.targetChanged(previous, current);
    }

    public void customBrainDamaged(org.bukkit.event.entity.EntityDamageEvent event) {
        customBrainRuntime.damaged(Objects.requireNonNull(event, "event"));
    }

    public void refreshExtensions() {
        customBrainRuntime.refreshRegistration();
        combatModeEngine.refreshRegistration();
    }

    private boolean canForceVerticalTeleport() {
        return System.currentTimeMillis() - lastForcedVerticalTeleportTime >= FORCED_VERTICAL_TELEPORT_COOLDOWN_MS
                && !teleportController.isTeleporting();
    }

    private void tickMobTarget(LivingEntity bukkitTarget, boolean combatEnabled) {
        rotationController.updateRotation(bukkitTarget);
        if (!combatEnabled) {
            combatModeEngine.suspend();
            movementController.stopMovement();
            return;
        }

        if (!options.isHealing() && healController.isHealing()) {
            healController.resetHealState();
        }
        if (options.isHealing() && healController.isHealing()) {
            Vector away = bot.bukkitPosition().subtract(bukkitTarget.getLocation().toVector());
            away.setY(0.0D);
            if (away.lengthSquared() > 0.001D) {
                movementController.moveToPosition(bot.bukkitPosition().add(away.normalize().multiply(5.0D)));
            }
            return;
        }

        // Never run synchronous Pathetic A* against mobs — Spark shows it owning the server thread
        // (isValidByCustomProcessors). Combat strategies already approach/strafe the target.
        clearActivePathfinding();
        combatModeEngine.tick(bukkitTarget);
    }

    private boolean followActivePath(LivingEntity target) {
        if (!pathfindingManager.isUsingPathfinding() || !movementController.hasActivePath()) {
            return false;
        }

        Vector targetPosition = target.getLocation().toVector();
        if (movementController.shouldRecalculatePath(targetPosition)
                && !movementController.calculatePathTo(targetPosition)) {
            pathfindingManager.setUsingPathfinding(false);
            movementController.clearPath();
            return false;
        }

        if (!movementController.followPath()) {
            pathfindingManager.setUsingPathfinding(false);
            return false;
        }

        return true;
    }

    public void clearActivePathfinding() {
        if (!pathfindingManager.isUsingPathfinding() && !movementController.hasActivePath()) {
            return;
        }
        pathfindingManager.setUsingPathfinding(false);
        movementController.clearPath();
    }

    private void syncExplosiveCombat() {
        boolean explosionsEnabled = options.isExplosions();
        boolean crystalMode = options.getCombatMode().equals(CombatMode.CRYSTAL);
        cpvpController.setEnabled(explosionsEnabled && crystalMode && options.isCrystalPvp());
        if (!crystalMode) {
            rapvpController.disable();
        }
        if (explosionsEnabled) {
            return;
        }

        rapvpController.disable();
        // CART uses CRYSTAL_SLOT for TNT minecart — never wipe non-crystal mode kits.
        if (!crystalMode) {
            return;
        }
        inventoryController.setItem(BotInventoryController.CRYSTAL_SLOT, com.monkey.ultimatebot.compat.ItemStackAccess.empty());
        inventoryController.setItem(BotInventoryController.ANCHOR_SLOT, com.monkey.ultimatebot.compat.ItemStackAccess.empty());
        inventoryController.setItem(BotInventoryController.GLOW_SLOT, com.monkey.ultimatebot.compat.ItemStackAccess.empty());
    }

    private boolean handleSustainFood() {
        return sustainFoodController.tick();
    }

    private boolean shouldForceVerticalTeleport(Player target) {
        Vector botPos = bot.bukkitPosition();
        Vector targetPos = target.getLocation().toVector();
        double horizontalDistance = Math.hypot(botPos.getX() - targetPos.getX(), botPos.getZ() - targetPos.getZ());
        if (horizontalDistance > 3.2D) {
            return false;
        }

        double verticalDistance = Math.abs(botPos.getY() - targetPos.getY());
        if (verticalDistance < 3.0D) {
            return false;
        }

        int botX = botPos.getBlockX();
        int botY = botPos.getBlockY();
        int botZ = botPos.getBlockZ();
        int targetX = targetPos.getBlockX();
        int targetY = targetPos.getBlockY();
        int targetZ = targetPos.getBlockZ();
        int minY = Math.min(botY, targetY) + 1;
        int maxY = Math.max(botY, targetY) - 1;
        if (maxY < minY) {
            return false;
        }

        int solidBetween = 0;
        for (int y = minY; y <= maxY; y++) {
            Block checkAtBotColumn = bukkitBot.getWorld().getBlockAt(botX, y, botZ);
            Block checkAtTargetColumn = bukkitBot.getWorld().getBlockAt(targetX, y, targetZ);
            if (isSolid(checkAtBotColumn) || isSolid(checkAtTargetColumn)) {
                solidBetween++;
                if (solidBetween >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isBlock() && type.isSolid() && !block.isPassable();
    }

    public void close() {
        customBrainRuntime.close();
        combatModeEngine.close();
        idleBehaviorController.close();
        sustainFoodController.close();
        movementController.clearPath();
        movementController.clearCache();
        healController.resetHealState();
    }
}
