package com.monkey.mcbot.bot.ai;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.CombatDataManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.CombatStateManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.CombatStrategyExecutor;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.PathfindingManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatDataManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatStateManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatStrategyExecutor;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.IPathfindingManager;
import com.monkey.mcbot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.mcbot.bot.ai.controllers.heal.BotHealController;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.noobs.BotNoobMovementController;
import com.monkey.mcbot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.mcbot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.mcbot.bot.ai.controllers.teleport.BotTeleportController;
import com.monkey.mcbot.bot.ai.controllers.totem.BotTotemController;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
    private final BotOptions options;

    public BotAI(Player bot, MinecraftBot plugin, BotOptions options) {
        this.bot = bot;
        this.level = bot.level();
        this.options = options;

        this.noobMovementController = new BotNoobMovementController(bot, level);
        this.movementController = new BotMovementController(bot, level);
        this.rotationController = new BotRotationController(bot);
        this.totemController = new BotTotemController(bot, plugin);
        this.attackController = new BotAttackController(bot);
        this.inventoryController = new BotInventoryController(bot);
        this.healController = new BotHealController(bot, inventoryController);
        this.teleportController = new BotTeleportController(bot);
        this.enderpearlController = new BotEnderpearlController(
                bot, inventoryController, rotationController);
        this.cpvpController = new BotCPVPController(bot, inventoryController);
        this.cpvpController.setRank(options.getRank());
        this.rapvpController = new BotRAPVPController(
                bot, inventoryController, rotationController, enderpearlController);
        this.rapvpController.setRank(options.getRank());

        this.combatStateManager = new CombatStateManager(
                bot, inventoryController, cpvpController, rapvpController);
        this.combatDataManager = new CombatDataManager(
                bot, enderpearlController, rapvpController, cpvpController);
        this.pathfindingManager = new PathfindingManager(
                bot, level, movementController, enderpearlController, combatStateManager);
        this.combatStrategyExecutor = new CombatStrategyExecutor(
                bot, movementController, rotationController, attackController,
                inventoryController, enderpearlController, cpvpController, rapvpController,
                combatStateManager, combatDataManager);
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer) {
        tick(targetBukkitPlayer, true);
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer, boolean allowCombat) {
        if (targetBukkitPlayer == null || targetBukkitPlayer.isDead()) return;

        Player target = ((CraftPlayer) targetBukkitPlayer).getHandle();

        boolean isCurrentlyHealing = healController.isHealing();
        boolean combatEnabled = ((ITrainingBot) bot).isCombat() && allowCombat;

        combatDataManager.updateCombatData(target, combatEnabled);

        if (combatStateManager instanceof CombatStateManager) {
            ((CombatStateManager) combatStateManager).updateDamageData(
                    combatDataManager.getConsecutiveDamageCount(),
                    combatDataManager.getLastDamageTime()
            );
        }

        if (combatEnabled) {
            if (enderpearlController.checkAndPerformAutoTeleport(targetBukkitPlayer)) {
                if (pathfindingManager.isUsingPathfinding()) {
                    pathfindingManager.setUsingPathfinding(false);
                    movementController.clearPath();
                }
                rotationController.updateRotation(target);
            }
            if (pathfindingManager.isUsingPathfinding() && movementController.hasActivePath()) {
                if (movementController.followPath()) {
                    rotationController.lookAt(
                            movementController.getCurrentPathPoint().x,
                            movementController.getCurrentPathPoint().y,
                            movementController.getCurrentPathPoint().z
                    );
                } else {
                    pathfindingManager.setUsingPathfinding(false);
                }
            } else {
                pathfindingManager.checkForStuck(target);
                enderpearlController.tick();

                if (((ITrainingBot) bot).isCombat() && !isCurrentlyHealing && allowCombat) {
                    combatStateManager.updateCombatState(target);
                    combatStrategyExecutor.executeCombatStrategy(target);
                } else if (isCurrentlyHealing) {
                    combatStateManager.updateCombatState(target);
                    executeHealingMovement(target);
                } else {
                    combatStrategyExecutor.basicFollowBehavior(target);
                }
            }
        } else {
            if (!isCurrentlyHealing) {
                noobMovementController.moveTowards(target, 2.5);
            } else {
                executeHealingMovement(target);
            }
        }

        if (pathfindingManager instanceof PathfindingManager) {
            ((PathfindingManager) pathfindingManager).updateLastBotPosition();
            ((PathfindingManager) pathfindingManager).updateLastActionTime();
        }
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

    public void setRank(BotRank rank) {
        options.setRank(rank);
        this.rapvpController.setRank(rank);
        this.cpvpController.setRank(rank);
    }

    public BotRank getRank() {
        return options.getRank();
    }

    public BotMovementController getMovementController() { return movementController; }
    public BotRotationController getRotationController() { return rotationController; }
    public BotTotemController getTotemController() { return totemController; }
    public BotInventoryController getInventoryController() { return inventoryController; }
    public BotEnderpearlController getEnderpearlController() { return enderpearlController; }
    public BotCPVPController getCPVPController() { return cpvpController; }
    public BotHealController getHealController() { return healController; }
    public BotTeleportController getTeleportController() { return teleportController; }
    public CombatState getCurrentState() {
        return CombatState.valueOf(combatStateManager.getCurrentState().name());
    }
}
