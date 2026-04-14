package com.monkey.mcbot.logging;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.MinecraftBotAPI;
import com.monkey.mcbot.api.model.BotMode;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.nms.INMSBridge;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MinecraftBotLogging {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String SILVER = "\u001B[38;5;250m";
    private static final String WHITE = "\u001B[97m";
    private static final String GOLD = "\u001B[38;5;214m";
    private static final String CYAN = "\u001B[96m";
    private static final String BLUE = "\u001B[94m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELLOW = "\u001B[93m";
    private static final String RED = "\u001B[91m";
    private static final String MAGENTA = "\u001B[95m";

    private static final int STARTUP_PHASES = 9;
    private static final long WARMUP_REPORT_DELAY_TICKS = 20L;
    private static final long STABLE_REPORT_DELAY_TICKS = 60L;

    private static final int INTERNAL_CONTROLLER_COUNT = 15;

    private MinecraftBotLogging() {
    }

    public static StartupSession beginBootstrap(MinecraftBot plugin) {
        return new StartupSession(plugin);
    }

    public static void schedulePostEnableDiagnostics(MinecraftBot plugin, StartupSession session) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                info(
                        plugin.getLogger(),
                        "Boot",
                        "Warmup checkpoint -> online=" + Bukkit.getOnlinePlayers().size()
                                + " | worlds=" + Bukkit.getWorlds().size()
                                + " | activeBots=" + safeBotCount(plugin)
                                + " | placeholders=" + session.placeholderCount()
                ), WARMUP_REPORT_DELAY_TICKS);

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                info(
                        plugin.getLogger(),
                        "Boot",
                        "Startup stable -> memory=" + formatMemorySnapshot()
                                + " | commands=" + session.commandCount()
                                + " | listeners=" + session.listenerStateCountSummary()
                                + " | playerOptions=" + safePlayerOptionCount(plugin)
                ), STABLE_REPORT_DELAY_TICKS);
    }

    public static String catalogSummary() {
        return "types=" + BotType.values().length
                + " | modes=" + BotMode.values().length
                + " | controllers=" + INTERNAL_CONTROLLER_COUNT;
    }

    public static String controllerSummary() {
        return "count=" + INTERNAL_CONTROLLER_COUNT + " | names=hidden";
    }

    public static void logNmsInitStart(Logger logger, String minecraftVersion, String className, String supportedVersions) {
        info(logger, "NMS", "Minecraft -> " + minecraftVersion);
        detail(logger, "NMS", "Bridge -> " + className.substring(className.lastIndexOf('.') + 1));
        detail(logger, "NMS", "Supported -> " + supportedVersions);
    }

    public static void logNmsUnsupportedVersion(Logger logger, String minecraftVersion, String supportedVersions) {
        warn(logger, "NMS", "Unsupported minecraft version -> " + minecraftVersion);
        warn(logger, "NMS", "Supported versions -> " + supportedVersions);
    }

    public static void logNmsInitSuccess(Logger logger, INMSBridge bridge) {
        success(logger, "NMS", "Bridge ready -> " + bridge.getClass().getSimpleName());
    }

    public static void logNmsInitFailure(Logger logger, String className, Throwable error) {
        error(logger, "NMS", "Bridge load failed -> " + className + " | " + safeMessage(error), error);
    }

    public static void logApiRegistered(Logger logger, MinecraftBotAPI api) {
        success(
                logger,
                "API",
                "Public API registered -> manager="
                        + apiManagerSummary()
                        + " | registry="
                        + apiRegistrySummary()
        );
    }

    public static String apiManagerSummary() {
        return "bound";
    }

    public static String apiRegistrySummary() {
        return "bound";
    }

    public static void info(Logger logger, String module, String message) {
        emit(logger, Level.INFO, module, WHITE, message);
    }

    public static void detail(Logger logger, String module, String message) {
        emit(logger, Level.INFO, module, SILVER, "> " + message);
    }

    public static void success(Logger logger, String module, String message) {
        emit(logger, Level.INFO, module, GREEN, "+ " + message);
    }

    public static void warn(Logger logger, String module, String message) {
        emit(logger, Level.WARNING, module, YELLOW, "! " + message);
    }

    public static void error(Logger logger, String module, String message, Throwable error) {
        logger.log(Level.SEVERE, format(module, RED, "x " + message, RED), error);
    }

    private static void banner(Logger logger, String module, String messageColor, String... lines) {
        String accent = moduleColor(module);
        separator(logger, accent, '=');
        for (String line : lines) {
            logger.info(format(module, accent, line, messageColor));
        }
        separator(logger, accent, '=');
    }

    private static void separator(Logger logger, String color, char symbol) {
        logger.info(BOLD + color + String.valueOf(symbol).repeat(58) + RESET);
    }

    private static void emit(Logger logger, Level level, String module, String messageColor, String message) {
        String line = format(module, moduleColor(module), message, messageColor);
        if (level == Level.INFO) {
            logger.info(line);
        } else if (level == Level.WARNING) {
            logger.warning(line);
        } else {
            logger.log(level, line);
        }
    }

    private static String format(String module, String accentColor, String message, String messageColor) {
        return BOLD + accentColor + "[" + module + "]" + RESET + " " + messageColor + message + RESET;
    }

    private static String moduleColor(String module) {
        return switch (module) {
            case "Boot" -> GOLD;
            case "License" -> YELLOW;
            case "Update" -> CYAN;
            case "NMS" -> BLUE;
            case "Core" -> CYAN;
            case "API" -> GREEN;
            case "Hooks" -> MAGENTA;
            case "PAPI" -> YELLOW;
            default -> WHITE;
        };
    }

    private static String joinNames(Enum<?>[] values) {
        return java.util.Arrays.stream(values)
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private static String joinOrNone(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "none";
        }
        return values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private static String formatWorldSummary() {
        List<World> worlds = Bukkit.getWorlds();
        return worlds.size() + " -> " + joinOrNone(worlds.stream().map(World::getName).toList());
    }

    private static String formatMemorySnapshot() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory() / (1024 * 1024);
        long total = runtime.totalMemory() / (1024 * 1024);
        long free = runtime.freeMemory() / (1024 * 1024);
        long used = total - free;
        return "used=" + used + "MB free=" + free + "MB total=" + total + "MB max=" + max + "MB";
    }

    private static String formatDuration(long nanos) {
        double millis = nanos / 1_000_000.0D;
        if (millis < 1000.0D) {
            return String.format(Locale.ROOT, "%.2fms", millis);
        }
        return String.format(Locale.ROOT, "%.2fs", millis / 1000.0D);
    }

    private static String safeMessage(Throwable error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? error.getClass().getSimpleName()
                : error.getMessage();
    }

    private static int safeBotCount(MinecraftBot plugin) {
        return plugin.getBotRegistry() == null ? 0 : plugin.getBotRegistry().size();
    }

    private static int safePlayerOptionCount(MinecraftBot plugin) {
        return plugin.getPlayerOptions() == null ? 0 : plugin.getPlayerOptions().size();
    }

    public static final class StartupSession {

        private final MinecraftBot plugin;
        private final Logger logger;
        private final long startedAtNanos;
        private final List<PhaseSnapshot> phases = new ArrayList<>();

        private PhaseSnapshot currentPhase;
        private String currentModule = "Boot";
        private String currentContext = "bootstrap";
        private String nmsBridgeName = "pending";
        private String nmsSupportedVersions = "pending";
        private List<String> registeredCommands = List.of();
        private List<String> registeredListeners = List.of();
        private List<String> disabledListeners = List.of();
        private List<String> registeredPlaceholders = List.of();
        private boolean placeholderPresent;
        private boolean placeholderRegistered;
        private boolean apiPublished;

        private StartupSession(MinecraftBot plugin) {
            this.plugin = plugin;
            this.logger = plugin.getLogger();
            this.startedAtNanos = System.nanoTime();

            banner(
                    logger,
                    "Boot",
                    WHITE,
                    plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion(),
                    "Authors: " + joinOrNone(plugin.getDescription().getAuthors()),
                    "Starting up..."
            );
            MinecraftBotLogging.detail(logger, "Boot", "Server -> " + Bukkit.getName() + " | " + Bukkit.getVersion());
            MinecraftBotLogging.detail(logger, "Boot", "Minecraft -> " + Bukkit.getMinecraftVersion() + " | Java -> " + System.getProperty("java.version"));
            MinecraftBotLogging.detail(logger, "Boot", "Worlds -> " + formatWorldSummary());
            MinecraftBotLogging.detail(logger, "Boot", "Memory -> " + formatMemorySnapshot());
        }

        public void beginPhase(int phase, String module, String title) {
            this.currentModule = module;
            this.currentContext = module + " :: " + title;
            this.currentPhase = new PhaseSnapshot(phase, module, title, System.nanoTime());

            separator(logger, moduleColor(module), '-');
            info(logger, module, "[" + String.format(Locale.ROOT, "%02d", phase) + "/" + STARTUP_PHASES + "] " + title);
        }

        public void detail(String label, String value) {
            MinecraftBotLogging.detail(logger, currentModule, label + " -> " + value);
        }

        public void ready(String component, String details) {
            success(logger, currentModule, component + " -> " + details);
        }

        public void warn(String component, String details) {
            MinecraftBotLogging.warn(logger, currentModule, component + " -> " + details);
        }

        public void markNmsBridge(String nmsBridgeName, String supportedVersions) {
            this.nmsBridgeName = nmsBridgeName;
            this.nmsSupportedVersions = supportedVersions;
        }

        public void markCommands(List<String> registeredCommands) {
            this.registeredCommands = List.copyOf(registeredCommands);
        }

        public void markListeners(List<String> registeredListeners, List<String> disabledListeners) {
            this.registeredListeners = List.copyOf(registeredListeners);
            this.disabledListeners = List.copyOf(disabledListeners);
        }

        public void markPlaceholders(boolean placeholderPresent,
                                     boolean placeholderRegistered,
                                     List<String> placeholderKeys) {
            this.placeholderPresent = placeholderPresent;
            this.placeholderRegistered = placeholderRegistered;
            this.registeredPlaceholders = List.copyOf(placeholderKeys);
        }

        public void markApiPublished() {
            this.apiPublished = true;
        }

        public void completePhase(String summary) {
            if (currentPhase == null) {
                return;
            }

            currentPhase.complete(System.nanoTime(), summary);
            phases.add(currentPhase);
            MinecraftBotLogging.detail(logger, currentModule, "Done in " + formatDuration(currentPhase.durationNanos()) + " | " + summary);
            currentPhase = null;
        }

        public void completeBootstrap() {
            long totalDuration = System.nanoTime() - startedAtNanos;

            banner(
                    logger,
                    "Boot",
                    WHITE,
                    plugin.getDescription().getName() + " enabled",
                    "Startup time: " + formatDuration(totalDuration),
                    "NMS: " + nmsBridgeName + " | supported: " + nmsSupportedVersions,
                    "Commands: " + commandCount() + " | listeners: " + listenerStateCountSummary() + " | placeholders: " + placeholderCount()
            );
            MinecraftBotLogging.detail(logger, "Boot", "Command list -> " + joinOrNone(registeredCommands));
            MinecraftBotLogging.detail(logger, "Boot", "Listener list -> " + listenerStateCountSummary());
            if (!disabledListeners.isEmpty()) {
                MinecraftBotLogging.detail(logger, "Boot", "Listener fallback -> " + joinOrNone(disabledListeners));
            }
            MinecraftBotLogging.detail(logger, "Boot", "Placeholder state -> present=" + placeholderPresent + " registered=" + placeholderRegistered);
            MinecraftBotLogging.detail(logger, "Boot", "Phase timings -> " + phases.stream()
                    .map(phase -> String.format(
                            Locale.ROOT,
                            "%02d=%s",
                            phase.index,
                            formatDuration(phase.durationNanos())
                    ))
                    .collect(Collectors.joining(" | ")));
        }

        public void fail(Throwable error) {
            banner(
                    logger,
                    "Boot",
                    RED,
                    plugin.getDescription().getName() + " failed to start",
                    "Context: " + currentContext,
                    "Elapsed: " + formatDuration(System.nanoTime() - startedAtNanos)
            );
            error(logger, "Boot", "Startup exception -> " + safeMessage(error), error);
        }

        public int commandCount() {
            return registeredCommands.size();
        }

        public int listenerCount() {
            return registeredListeners.size();
        }

        public int disabledListenerCount() {
            return disabledListeners.size();
        }

        public int placeholderCount() {
            return registeredPlaceholders.size();
        }

        public String commandSummary() {
            return joinOrNone(registeredCommands);
        }

        public String listenerSummary() {
            return joinOrNone(registeredListeners);
        }

        public String disabledListenerSummary() {
            return joinOrNone(disabledListeners);
        }

        public String listenerStateCountSummary() {
            return "active=" + listenerCount() + " disabled=" + disabledListenerCount();
        }

        public String placeholderSummary() {
            return joinOrNone(registeredPlaceholders);
        }
    }

    private static final class PhaseSnapshot {

        private final int index;
        private final String module;
        private final String title;
        private final long startedAtNanos;
        private long completedAtNanos;
        private String summary = "done";

        private PhaseSnapshot(int index, String module, String title, long startedAtNanos) {
            this.index = index;
            this.module = module;
            this.title = title;
            this.startedAtNanos = startedAtNanos;
        }

        private void complete(long completedAtNanos, String summary) {
            this.completedAtNanos = completedAtNanos;
            this.summary = summary;
        }

        private long durationNanos() {
            return completedAtNanos - startedAtNanos;
        }
    }
}
