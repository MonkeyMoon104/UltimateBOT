package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.model.BotLocation;
import com.monkey.ultimatebot.bot.BotOptions;
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
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    private final BotOptions options;
    private final UltimateBot plugin;
    private final Random idleRandom = new Random();
    private long lastForcedVerticalTeleportTime = 0L;
    private long lastSeenTargetTime = System.currentTimeMillis();
    private long lastIdleDestinationTime = 0L;
    private Vec3 idleDestination;
    private boolean sustainFoodSlotActive = false;
    private boolean eatingSustainFood = false;
    private int sustainFoodTicks = 0;
    private static final long FORCED_VERTICAL_TELEPORT_COOLDOWN_MS = 3000L;
    private static final long IDLE_DESTINATION_RETRY_MS = 3500L;
    private static final double IDLE_DESTINATION_REACHED_DISTANCE = 1.6D;
    private static final int SUSTAIN_FOOD_DURATION_TICKS = 32;
    private static final int SUSTAIN_FOOD_TRIGGER_LEVEL = 6;

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
        lastSeenTargetTime = System.currentTimeMillis();
        idleDestination = null;

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

        combatDataManager.updateCombatData(playerTarget, combatEnabled);

        if (combatStateManager instanceof CombatStateManager stateManager) {
            stateManager.updateDamageData(
                    combatDataManager.getConsecutiveDamageCount(), combatDataManager.getLastDamageTime());
        }

        if (combatEnabled) {
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
                    combatStrategyExecutor.executeCombatStrategy(playerTarget);
                } else if (isCurrentlyHealing) {
                    combatStateManager.updateCombatState(playerTarget);
                    executeHealingMovement(playerTarget);
                } else {
                    combatStrategyExecutor.basicFollowBehavior(playerTarget);
                }
            }
        } else {
            if (!isCurrentlyHealing) {
                if (!followActivePath(playerTarget)) {
                    pathfindingManager.checkForStuck(playerTarget);
                    if (pathfindingManager.isUsingPathfinding()) {
                        followActivePath(playerTarget);
                    } else {
                        noobMovementController.moveTowards(playerTarget, 2.5);
                    }
                }
            } else {
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

        if (!options.isIdleWander()) {
            idleDestination = null;
            movementController.stopMovement();
            return;
        }

        Vec3 spawn = resolveSpawnPosition();
        if (spawn == null) {
            return;
        }

        long now = System.currentTimeMillis();
        double distanceFromSpawn = bot.position().distanceTo(spawn);
        boolean shouldReturn = distanceFromSpawn > options.getIdleReturnDistance()
                || now - lastSeenTargetTime >= options.getIdleReturnDelayMs();

        if (shouldReturn && distanceFromSpawn > IDLE_DESTINATION_REACHED_DISTANCE) {
            idleDestination = spawn;
            moveIdleTo(spawn);
            return;
        }

        if (idleDestination == null
                || bot.position().distanceTo(idleDestination) <= IDLE_DESTINATION_REACHED_DISTANCE
                || now - lastIdleDestinationTime >= IDLE_DESTINATION_RETRY_MS) {
            idleDestination = findIdleDestination(spawn);
            lastIdleDestinationTime = now;
        }

        if (idleDestination != null) {
            moveIdleTo(idleDestination);
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
                combatStrategyExecutor.executeMeleeCombat(target);
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
        cpvpController.setEnabled(explosionsEnabled && options.isCrystalPvp());
        if (explosionsEnabled) {
            return;
        }

        rapvpController.disable();
        inventoryController.setItem(BotInventoryController.CRYSTAL_SLOT, net.minecraft.world.item.ItemStack.EMPTY);
        inventoryController.setItem(BotInventoryController.ANCHOR_SLOT, net.minecraft.world.item.ItemStack.EMPTY);
        inventoryController.setItem(BotInventoryController.GLOW_SLOT, net.minecraft.world.item.ItemStack.EMPTY);
    }

    private boolean handleSustainFood() {
        syncSustainFoodSlot();

        if (options.isHealing()) {
            eatingSustainFood = false;
            sustainFoodTicks = 0;
            return false;
        }

        if (bot.isUsingItem() && Items.GOLDEN_APPLE.equals(bot.getUseItem().getItem())) {
            bot.releaseUsingItem();
            eatingSustainFood = false;
            sustainFoodTicks = 0;
            inventoryController.switchToSword();
            return false;
        }

        if (eatingSustainFood) {
            sustainFoodTicks++;
            if (sustainFoodTicks >= SUSTAIN_FOOD_DURATION_TICKS || !bot.isUsingItem()) {
                applySustainFood();
                eatingSustainFood = false;
                sustainFoodTicks = 0;
                inventoryController.switchToSword();
                return false;
            }
            return true;
        }

        if (bot.getFoodData().getFoodLevel() > SUSTAIN_FOOD_TRIGGER_LEVEL) {
            return false;
        }

        inventoryController.switchToSlot(BotInventoryController.GOLDEN_APPLE_SLOT);
        try {
            bot.startUsingItem(InteractionHand.MAIN_HAND);
        } catch (Exception ignored) {
            applySustainFood();
            inventoryController.switchToSword();
            return false;
        }
        eatingSustainFood = true;
        sustainFoodTicks = 0;
        return true;
    }

    private void syncSustainFoodSlot() {
        if (options.isHealing()) {
            if (sustainFoodSlotActive) {
                inventoryController.setItem(
                        BotInventoryController.GOLDEN_APPLE_SLOT, new ItemStack(Items.GOLDEN_APPLE, 64));
                sustainFoodSlotActive = false;
            }
            return;
        }

        if (!sustainFoodSlotActive) {
            inventoryController.setItem(BotInventoryController.GOLDEN_APPLE_SLOT, new ItemStack(Items.COOKED_BEEF, 64));
            sustainFoodSlotActive = true;
        }
    }

    private void applySustainFood() {
        if (bot.getFoodData().getFoodLevel() >= 20) {
            bot.releaseUsingItem();
            return;
        }
        bot.getFoodData().eat(8, 0.8F);
        bot.releaseUsingItem();
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

    private void moveIdleTo(Vec3 destination) {
        movementController.moveToPosition(destination);
        rotationController.lookAt(destination.x, destination.y, destination.z);
    }

    private Vec3 findIdleDestination(Vec3 spawn) {
        double radius = options.getIdleWanderRadius();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = idleRandom.nextDouble() * Math.PI * 2.0D;
            double distance = 2.0D + idleRandom.nextDouble() * Math.max(1.0D, radius - 2.0D);
            int x = (int) Math.floor(spawn.x + Math.cos(angle) * distance);
            int z = (int) Math.floor(spawn.z + Math.sin(angle) * distance);
            int y = findSafeY(x, (int) Math.round(spawn.y), z);
            if (y == Integer.MIN_VALUE) {
                continue;
            }

            Vec3 candidate = new Vec3(x + 0.5D, y, z + 0.5D);
            if (isPvpAllowed(candidate)) {
                return candidate;
            }
        }
        return spawn;
    }

    private int findSafeY(int x, int baseY, int z) {
        World world = bot.getBukkitEntity().getWorld();
        int minBuildHeight = world == null ? -64 : world.getMinHeight();
        int maxBuildHeight = world == null ? 320 : world.getMaxHeight();
        int minY = Math.max(minBuildHeight, baseY - 6);
        int maxY = Math.min(maxBuildHeight - 2, baseY + 6);
        for (int y = maxY; y >= minY; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            BlockPos head = feet.above();
            BlockPos ground = feet.below();
            if (!level.getBlockState(ground).isSolidRender()) {
                continue;
            }
            if (level.getBlockState(feet).isSolidRender()
                    || level.getBlockState(head).isSolidRender()) {
                continue;
            }
            return y;
        }
        return Integer.MIN_VALUE;
    }

    private Vec3 resolveSpawnPosition() {
        BotLocation spawn = options.getSpawnLocation();
        if (spawn == null) {
            return null;
        }
        return new Vec3(spawn.x(), spawn.y(), spawn.z());
    }

    private boolean isPvpAllowed(Vec3 position) {
        if (!options.isRespectWorldGuardPvp() || plugin.getWorldGuardPvpService() == null) {
            return true;
        }

        World world = bot.getBukkitEntity().getWorld();
        if (world == null) {
            BotLocation spawn = options.getSpawnLocation();
            world = spawn == null || spawn.worldUUID() == null ? null : Bukkit.getWorld(spawn.worldUUID());
        }
        if (world == null) {
            return false;
        }

        Location location = new Location(world, position.x, position.y, position.z);
        return plugin.getWorldGuardPvpService().isPvpAllowed(location);
    }
}
