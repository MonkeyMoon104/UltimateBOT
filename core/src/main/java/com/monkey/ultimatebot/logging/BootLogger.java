package com.monkey.ultimatebot.logging;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
import com.monkey.ultimatebot.access.runtime.PluginMetaAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

public final class BootLogger {

    private final UltimateBot plugin;
    private final Logger logger;
    private final long startedAtNanos;
    private final List<PhaseSnapshot> phases = new ArrayList<>();
    private @Nullable PhaseSnapshot currentPhase;

    public BootLogger(UltimateBot plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.startedAtNanos = System.nanoTime();

        UltimateBotLogging.bootBanner(
                logger,
                "Boot",
                "  __  ____ ____________  ______ _____________  ____  ______",
                " / / / / //_  __/  _/  |/  / _ /_  __/ __/ _ )/ __ \\/_  __/",
                "/ /_/ / /__/ / _/ // /|_/ / __ |/ / / _// _  / /_/ / / /   ",
                "\\____/____/_/ /___/_/  /_/_/ |_/_/ /___/____/\\____/ /_/    ");

        UltimateBotLogging.bootBanner(
                logger,
                "Boot",
                "UltimateBot Boot Start",
                "Plugin: " + PluginMetaAccess.name(plugin) + " v" + PluginMetaAccess.version(plugin),
                "Server: " + Bukkit.getName(),
                "MC: " + MinecraftVersionAccess.minecraftVersion() + " | Java: " + System.getProperty("java.version"));
    }

    public void boot(String message) {
        UltimateBotLogging.infoWithAccent(logger, "Boot", activeModule(), message);
    }

    public void nms(String message) {
        UltimateBotLogging.infoWithAccent(logger, "NMS", activeModule(), message);
    }

    public void config(String message) {
        UltimateBotLogging.infoWithAccent(logger, "Config", activeModule(), message);
    }

    public void module(String name, long initTimeMs) {
        UltimateBotLogging.successWithAccent(
                logger, "Module", activeModule(), name + " -> ready (" + initTimeMs + "ms)");
    }

    public void warn(String message) {
        UltimateBotLogging.warnWithAccent(logger, "Warn", activeModule(), message);
    }

    public void error(String message, @Nullable Throwable cause) {
        if (cause != null) {
            UltimateBotLogging.errorWithAccent(logger, "Error", activeModule(), message, cause);
        } else {
            logger.log(Level.SEVERE, UltimateBotLogging.formatLine("Error", message));
        }
    }

    public void beginPhase(int index, String category, String title) {
        this.currentPhase = new PhaseSnapshot(index, System.nanoTime());
        String phaseModule = phaseModule(index);
        UltimateBotLogging.sectionDivider(
                logger,
                phaseModule,
                "PHASE " + String.format(Locale.ROOT, "%02d", index) + " - " + title);
        UltimateBotLogging.infoWithAccent(logger, category, phaseModule,
                "Step started");
    }

    public void completePhase(String summary) {
        if (currentPhase == null) {
            return;
        }
        currentPhase.complete(System.nanoTime());
        phases.add(currentPhase);
        UltimateBotLogging.detailWithAccent(logger, "Boot", activeModule(),
                "Done in " + formatDuration(currentPhase.durationNanos()) + " | " + summary);
        currentPhase = null;
    }

    public void complete() {
        long totalDuration = System.nanoTime() - startedAtNanos;
        UltimateBotLogging.bootBanner(
                logger,
                "Boot",
                "UltimateBot Boot Complete",
                "Plugin: " + PluginMetaAccess.name(plugin) + " enabled",
                "Elapsed: " + formatDuration(totalDuration),
                "Phases: " + phases.size() + " completed");
    }

    public void fail(Throwable error) {
        long elapsed = System.nanoTime() - startedAtNanos;
        UltimateBotLogging.bootBanner(
                logger,
                "Boot",
                "UltimateBot Boot Failed",
                "Plugin: " + PluginMetaAccess.name(plugin),
                "Elapsed: " + formatDuration(elapsed));
        UltimateBotLogging.error(logger, "Boot", "Startup exception -> " + safeMessage(error), error);
    }

    public Logger getLogger() {
        return logger;
    }

    private static String formatDuration(long nanos) {
        double millis = nanos / 1_000_000.0D;
        if (millis < 1000.0D) {
            return String.format(Locale.ROOT, "%.2fms", millis);
        }
        return String.format(Locale.ROOT, "%.2fs", millis / 1000.0D);
    }

    private static String safeMessage(Throwable error) {
        return error.getMessage() == null || error.getMessage().trim().isEmpty()
                ? error.getClass().getSimpleName()
                : error.getMessage();
    }

    private static final class PhaseSnapshot {
        private final int index;
        private final long startedAtNanos;
        private long completedAtNanos;

        private PhaseSnapshot(int index, long startedAtNanos) {
            this.index = index;
            this.startedAtNanos = startedAtNanos;
        }

        private void complete(long completedAtNanos) {
            this.completedAtNanos = completedAtNanos;
        }

        private long durationNanos() {
            return completedAtNanos - startedAtNanos;
        }
    }

    private static String phaseModule(int index) {
        switch (index) {
            case 1:
                return "Phase01";
            case 2:
                return "Phase02";
            case 3:
                return "Phase03";
            case 4:
                return "Phase04";
            case 5:
                return "Phase05";
            case 6:
                return "Phase06";
            case 7:
                return "Phase07";
            case 8:
                return "Phase08";
            case 9:
                return "Phase09";
            default:
                return "Boot";
        }
    }

    private String activeModule() {
        if (currentPhase == null) {
            return "Boot";
        }
        return phaseModule(currentPhase.index);
    }
}
