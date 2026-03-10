package com.monkey.mcbot.logging;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.MinecraftBotAPI;
import com.monkey.mcbot.api.model.BotMode;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.CombatDataManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.CombatStateManager;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.CombatStrategyExecutor;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.PathfindingManager;
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
import com.monkey.mcbot.nms.INMSBridge;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MinecraftBotLogging {

    private static final String PREFIX = "[MinecraftBot/LOG] ";
    private static final int STARTUP_PHASES = 7;

    private static final List<Class<?>> CONTROLLER_CLASSES = List.of(
            BotNoobMovementController.class,
            BotMovementController.class,
            BotRotationController.class,
            BotTotemController.class,
            BotAttackController.class,
            BotInventoryController.class,
            BotHealController.class,
            BotTeleportController.class,
            BotEnderpearlController.class,
            BotCPVPController.class,
            BotRAPVPController.class,
            CombatStateManager.class,
            CombatDataManager.class,
            PathfindingManager.class,
            CombatStrategyExecutor.class
    );

    private MinecraftBotLogging() {
    }

    public static void logBootstrapStart(MinecraftBot plugin) {
        Logger logger = plugin.getLogger();
        logger.info(" ");
        logger.info(PREFIX + "============================================================");
        logger.info(PREFIX + "BOOTSTRAP START");
        logger.info(PREFIX + "Plugin: " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
        logger.info(PREFIX + "Server: " + Bukkit.getName() + " | " + Bukkit.getVersion());
        logger.info(PREFIX + "Minecraft: " + Bukkit.getMinecraftVersion());
        logger.info(PREFIX + "Java: " + System.getProperty("java.version"));
        logger.info(PREFIX + "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        logger.info(PREFIX + "JVM max memory (MB): " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        logger.info(PREFIX + "============================================================");
    }

    public static void logBootstrapCompleted(MinecraftBot plugin, boolean placeholderPresent, boolean placeholderRegistered) {
        Logger logger = plugin.getLogger();
        logger.info(PREFIX + "------------------------------------------------------------");
        logger.info(PREFIX + "BOOTSTRAP COMPLETED");
        logger.info(PREFIX + "Summary:");
        logger.info(PREFIX + " - NMS bridge: " + Bukkit.getMinecraftVersion());
        logger.info(PREFIX + " - Bot modes loaded: " + BotMode.values().length);
        logger.info(PREFIX + " - Core bot types loaded: " + BotType.values().length);
        logger.info(PREFIX + " - AI controllers wired: " + CONTROLLER_CLASSES.size());
        logger.info(PREFIX + " - PlaceholderAPI present: " + placeholderPresent);
        logger.info(PREFIX + " - PlaceholderAPI registered: " + placeholderRegistered);
        logger.info(PREFIX + "============================================================");
        logger.info(" ");
    }

    public static void logStartupPhase(Logger logger, int phase, String title) {
        logger.info(PREFIX + "Phase " + phase + "/" + STARTUP_PHASES + " -> " + title + " (loading...)");
    }

    public static void logComponentReady(Logger logger, String component, String details) {
        logger.info(PREFIX + "Component ready: " + component + " | " + details);
    }

    public static void logBotTypeCatalog(Logger logger) {
        String coreTypes = java.util.Arrays.stream(BotType.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        String apiTypes = java.util.Arrays.stream(BotMode.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        logger.info(PREFIX + "Core bot types loaded (" + BotType.values().length + "): " + coreTypes);
        logger.info(PREFIX + "API bot modes loaded (" + BotMode.values().length + "): " + apiTypes);
    }

    public static void logControllerCatalog(Logger logger) {
        logger.info(PREFIX + "Controller catalog loaded (" + CONTROLLER_CLASSES.size() + "):");
        for (Class<?> controllerClass : CONTROLLER_CLASSES) {
            logger.info(PREFIX + " - " + controllerClass.getSimpleName() + " (" + controllerClass.getName() + ")");
        }
    }

    public static void logNmsInitStart(Logger logger, String minecraftVersion, String className, String supportedVersions) {
        logger.info(PREFIX + "NMS init start | minecraftVersion=" + minecraftVersion);
        logger.info(PREFIX + "NMS supported versions: [" + supportedVersions + "]");
        logger.info(PREFIX + "NMS bridge class selected: " + className);
    }

    public static void logNmsUnsupportedVersion(Logger logger, String minecraftVersion, String supportedVersions) {
        logger.severe(PREFIX + "NMS unsupported minecraft version: " + minecraftVersion);
        logger.severe(PREFIX + "NMS supported versions: " + supportedVersions);
    }

    public static void logNmsInitSuccess(Logger logger, INMSBridge bridge) {
        logger.info(PREFIX + "NMS init success. activeBridge=" + bridge.getClass().getName());
    }

    public static void logNmsInitFailure(Logger logger, String className, Throwable error) {
        logger.log(Level.SEVERE, PREFIX + "NMS init failure. class=" + className + ", reason=" + error.getMessage(), error);
    }

    public static void logApiRegistered(Logger logger, MinecraftBotAPI api) {
        logger.info(PREFIX + "API ready. plugin=" + api.getPlugin().getName()
                + ", botManager=" + api.getBotManager().getClass().getSimpleName()
                + ", botRegistry=" + api.getBotRegistry().getClass().getSimpleName());
    }
}
