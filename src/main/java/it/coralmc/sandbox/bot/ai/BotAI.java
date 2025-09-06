package it.coralmc.sandbox.bot.ai;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.botai.*;
import it.coralmc.sandbox.bot.ai.botai.inter.ICombatDataManager;
import it.coralmc.sandbox.bot.ai.botai.inter.ICombatStateManager;
import it.coralmc.sandbox.bot.ai.botai.inter.ICombatStrategyExecutor;
import it.coralmc.sandbox.bot.ai.botai.inter.IPathfindingManager;
import it.coralmc.sandbox.bot.ai.controllers.attack.BotAttackController;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.BotCPVPController;
import it.coralmc.sandbox.bot.ai.controllers.enderpearl.BotEnderpearlController;
import it.coralmc.sandbox.bot.ai.controllers.inventory.BotInventoryController;
import it.coralmc.sandbox.bot.ai.controllers.movement.BotMovementController;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.BotRAPVPController;
import it.coralmc.sandbox.bot.ai.controllers.rotation.BotRotationController;
import it.coralmc.sandbox.bot.ai.controllers.totem.BotTotemController;
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
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final BotTotemController totemController;
    private final BotAttackController attackController;
    private final BotInventoryController inventoryController;
    private final BotEnderpearlController enderpearlController;
    private final BotCPVPController cpvpController;
    private final BotRAPVPController rapvpController;

    private final ICombatStateManager combatStateManager;
    private final ICombatDataManager combatDataManager;
    private final IPathfindingManager pathfindingManager;
    private final ICombatStrategyExecutor combatStrategyExecutor;

    public BotAI(Player bot, SandboxTraining plugin) {
        this.bot = bot;
        this.level = bot.level();

        this.movementController = new BotMovementController(bot, level);
        this.rotationController = new BotRotationController(bot);
        this.totemController = new BotTotemController(bot, plugin);
        this.attackController = new BotAttackController(bot);
        this.inventoryController = new BotInventoryController(bot);
        this.enderpearlController = new BotEnderpearlController(bot, inventoryController, rotationController);
        this.cpvpController = new BotCPVPController(bot, inventoryController, rotationController);
        this.rapvpController = new BotRAPVPController(bot, inventoryController, rotationController, cpvpController, enderpearlController);

        this.combatStateManager = new CombatStateManager(bot, inventoryController, cpvpController, rapvpController);
        this.combatDataManager = new CombatDataManager(bot, enderpearlController, rapvpController, cpvpController);
        this.pathfindingManager = new PathfindingManager(bot, level, movementController, enderpearlController, combatStateManager);
        this.combatStrategyExecutor = new CombatStrategyExecutor(bot, movementController, rotationController,
                attackController, inventoryController, enderpearlController,
                cpvpController, rapvpController, combatStateManager, combatDataManager);
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer) {
        if (targetBukkitPlayer == null || targetBukkitPlayer.isDead()) return;

        Player target = ((CraftPlayer) targetBukkitPlayer).getHandle();

        combatDataManager.updateCombatData(target);

        if (combatStateManager instanceof CombatStateManager) {
            ((CombatStateManager) combatStateManager).updateDamageData(
                    combatDataManager.getConsecutiveDamageCount(),
                    combatDataManager.getLastDamageTime()
            );
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

            inventoryController.tick();
            enderpearlController.tick();

            if (((TrainingBot) bot).isCombat()) {
                combatStateManager.updateCombatState(target);
                combatStrategyExecutor.executeCombatStrategy(target);
            } else {
                combatStrategyExecutor.basicFollowBehavior(target);
            }
        }

        if (pathfindingManager instanceof PathfindingManager) {
            ((PathfindingManager) pathfindingManager).updateLastBotPosition();
            ((PathfindingManager) pathfindingManager).updateLastActionTime();
        }
    }

    public void manageTotem() {
        totemController.manageTotem();
    }

    public BotMovementController getMovementController() { return movementController; }
    public BotRotationController getRotationController() { return rotationController; }
    public BotTotemController getTotemController() { return totemController; }
    public BotInventoryController getInventoryController() { return inventoryController; }
    public BotEnderpearlController getEnderpearlController() { return enderpearlController; }
    public BotCPVPController getCPVPController() { return cpvpController; }
    public CombatState getCurrentState() {
        return CombatState.valueOf(combatStateManager.getCurrentState().name());
    }
}