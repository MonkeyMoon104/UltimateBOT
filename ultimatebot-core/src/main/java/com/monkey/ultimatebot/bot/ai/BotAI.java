package com.monkey.ultimatebot.bot.ai;

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
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BotAI {

    public enum CombatState {
        AGGRESSIVE,
        DEFENSIVE,
        REPOSITIONING,
        ANCHOR_SETUP,
        CRYSTAL_SETUP,
        RETREATING
    }

    private final Player bot;
    private final Level level;
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
    private final BotOptions options;
    private final UltimateBot plugin;
    private long lastForcedVerticalTeleportTime = 0L;
    private static final long FORCED_VERTICAL_TELEPORT_COOLDOWN_MS = 3000L;

    public BotAI(Player bot, UltimateBot plugin, BotOptions options) {
        this.bot = java.util.Objects.requireNonNull(bot, "bot");
        this.level = bot.level();
        this.options = java.util.Objects.requireNonNull(options, "options");
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");

        this.noobMovementController = new BotNoobMovementController(bot, level);
        this.movementController = new BotMovementController(
                bot, level, plugin.getRuntimeSettings().blockStateCache());
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
        this.pathfindingManager = new PathfindingManager(bot, level, movementController, enderpearlController);
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
        this.combatModeEngine = new CombatModeEngine(
                bot,
                options,
                movementController,
                rotationController,
                attackController,
                inventoryController,
                cpvpController,
                rapvpController,
                combatStrategyExecutor,
                plugin.getWorldProtectionService());
        this.idleBehaviorController =
                new IdleBehaviorController(bot, options, plugin, movementController, rotationController);
        this.sustainFoodController = new SustainFoodController(bot, options, inventoryController);
        this.followBehaviorController =
                new FollowBehaviorController(bot, movementController, noobMovementController, pathfindingManager);
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

        LivingEntity target = resolveNmsTarget(targetBukkitPlayer);
        if (target == null) {
            tickIdle();
            return;
        }

        if (!options.isHealing() && healController.isHealing()) {
            healController.resetHealState();
        }
        boolean isCurrentlyHealing = options.isHealing() && healController.isHealing();
        boolean combatEnabled = ((ITrainingBot) bot).isCombat() && allowCombat;

        if (!(target instanceof Player playerTarget)) {
            tickMobTarget(target, combatEnabled);
            return;
        }

        combatDataManager.updateCombatData(
                playerTarget, combatEnabled && options.getCombatMode() == CombatMode.CRYSTAL);

        if (combatStateManager instanceof CombatStateManager stateManager) {
            stateManager.updateDamageData(
                    combatDataManager.getConsecutiveDamageCount(), combatDataManager.getLastDamageTime());
        }

        if (combatEnabled) {
            followBehaviorController.reset();
            if (shouldForceVerticalTeleport(playerTarget) && canForceVerticalTeleport()) {
                boolean teleported = teleportController.teleportSafeNear((org.bukkit.entity.Player) targetBukkitPlayer)
                        || teleportController.teleportBeside((org.bukkit.entity.Player) targetBukkitPlayer);
                if (teleported) {
                    lastForcedVerticalTeleportTime = System.currentTimeMillis();
                    if (pathfindingManager.isUsingPathfinding()) {
                        pathfindingManager.setUsingPathfinding(false);
                        movementController.clearPath();
                    }
                    rotationController.updateRotation(playerTarget);
                }
            }

            if (options.isEnderPearls()
                    && enderpearlController.checkAndPerformAutoTeleport(
                            (org.bukkit.entity.Player) targetBukkitPlayer)) {
                if (pathfindingManager.isUsingPathfinding()) {
                    pathfindingManager.setUsingPathfinding(false);
                    movementController.clearPath();
                }
                rotationController.updateRotation(playerTarget);
            }
            if (!followActivePath(playerTarget)) {
                pathfindingManager.checkForStuck(playerTarget);
                enderpearlController.tick();

                if (pathfindingManager.isUsingPathfinding()) {
                    followActivePath(playerTarget);
                } else if (((ITrainingBot) bot).isCombat() && !isCurrentlyHealing && allowCombat) {
                    combatStateManager.updateCombatState(playerTarget);
                    combatModeEngine.tick(playerTarget);
                } else if (isCurrentlyHealing) {
                    combatStateManager.updateCombatState(playerTarget);
                    executeHealingMovement(playerTarget);
                } else {
                    combatStrategyExecutor.basicFollowBehavior(playerTarget);
                }
            }
        } else {
            combatModeEngine.suspend();
            if (!isCurrentlyHealing) {
                followBehaviorController.tick(playerTarget);
            } else {
                followBehaviorController.reset();
                executeHealingMovement(playerTarget);
            }
        }

        if (pathfindingManager instanceof PathfindingManager manager) {
            manager.updateLastBotPosition();
        }

        rotationController.updateRotation(playerTarget);
    }

    public void tickIdle() {
        syncExplosiveCombat();
        enderpearlController.setEnabled(options.isEnderPearls());
        if (handleSustainFood()) {
            return;
        }

        idleBehaviorController.tick();
    }

    private void executeHealingMovement(Player target) {
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

    private boolean canForceVerticalTeleport() {
        return System.currentTimeMillis() - lastForcedVerticalTeleportTime >= FORCED_VERTICAL_TELEPORT_COOLDOWN_MS
                && !teleportController.isTeleporting();
    }

    private LivingEntity resolveNmsTarget(org.bukkit.entity.LivingEntity targetBukkitPlayer) {
        if (targetBukkitPlayer instanceof CraftLivingEntity craftLivingEntity) {
            return craftLivingEntity.getHandle();
        }
        if (targetBukkitPlayer instanceof CraftPlayer craftPlayer) {
            return craftPlayer.getHandle();
        }
        for (ITrainingBot managedBot : plugin.getBotRegistry().getAllBots().values()) {
            if (managedBot != null
                    && managedBot.asPlayer() != null
                    && managedBot.asPlayer().getUUID().equals(targetBukkitPlayer.getUniqueId())) {
                return managedBot.asPlayer();
            }
        }
        return null;
    }

    private void tickMobTarget(LivingEntity target, boolean combatEnabled) {
        rotationController.updateRotation(target);
        if (!combatEnabled) {
            combatModeEngine.suspend();
            movementController.stopMovement();
            return;
        }

        if (!options.isHealing() && healController.isHealing()) {
            healController.resetHealState();
        }
        if (options.isHealing() && healController.isHealing()) {
            Vec3 away = bot.position().subtract(target.position());
            if (away.horizontalDistanceSqr() > 0.001D) {
                movementController.moveToPosition(
                        bot.position().add(away.normalize().scale(5.0D)));
            }
            return;
        }
        if (!followActivePath(target)) {
            pathfindingManager.checkForStuck(target);
            if (pathfindingManager.isUsingPathfinding()) {
                followActivePath(target);
            } else {
                combatModeEngine.tick(target);
            }
        }
        if (pathfindingManager instanceof PathfindingManager manager) {
            manager.updateLastBotPosition();
        }
    }

    private boolean followActivePath(LivingEntity target) {
        if (!pathfindingManager.isUsingPathfinding() || !movementController.hasActivePath()) {
            return false;
        }

        if (movementController.shouldRecalculatePath(target.position())
                && !movementController.calculatePathTo(target.position())) {
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

    private void syncExplosiveCombat() {
        boolean explosionsEnabled = options.isExplosions();
        boolean crystalMode = options.getCombatMode() == CombatMode.CRYSTAL;
        cpvpController.setEnabled(explosionsEnabled && crystalMode && options.isCrystalPvp());
        if (!crystalMode) {
            rapvpController.disable();
        }
        if (explosionsEnabled) {
            return;
        }

        rapvpController.disable();
        inventoryController.setItem(BotInventoryController.CRYSTAL_SLOT, net.minecraft.world.item.ItemStack.EMPTY);
        inventoryController.setItem(BotInventoryController.ANCHOR_SLOT, net.minecraft.world.item.ItemStack.EMPTY);
        inventoryController.setItem(BotInventoryController.GLOW_SLOT, net.minecraft.world.item.ItemStack.EMPTY);
    }

    private boolean handleSustainFood() {
        return sustainFoodController.tick();
    }

    private boolean shouldForceVerticalTeleport(Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        double horizontalDistance = Math.hypot(botPos.x - targetPos.x, botPos.z - targetPos.z);
        if (horizontalDistance > 3.2D) {
            return false;
        }

        double verticalDistance = Math.abs(botPos.y - targetPos.y);
        if (verticalDistance < 3.0D) {
            return false;
        }

        BlockPos botBlock = bot.blockPosition();
        BlockPos targetBlock = target.blockPosition();
        int minY = Math.min(botBlock.getY(), targetBlock.getY()) + 1;
        int maxY = Math.max(botBlock.getY(), targetBlock.getY()) - 1;
        if (maxY < minY) {
            return false;
        }

        int solidBetween = 0;
        for (int y = minY; y <= maxY; y++) {
            BlockPos checkAtBotColumn = new BlockPos(botBlock.getX(), y, botBlock.getZ());
            BlockPos checkAtTargetColumn = new BlockPos(targetBlock.getX(), y, targetBlock.getZ());
            if (level.getBlockState(checkAtBotColumn).isSolidRender()
                    || level.getBlockState(checkAtTargetColumn).isSolidRender()) {
                solidBetween++;
                if (solidBetween >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

    public void close() {
        combatModeEngine.close();
        idleBehaviorController.close();
        sustainFoodController.close();
        movementController.clearPath();
        movementController.clearCache();
        healController.resetHealState();
    }
}
