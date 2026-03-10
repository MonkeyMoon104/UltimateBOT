package com.monkey.mcbot.nms;

import com.monkey.mcbot.logging.MinecraftBotLogging;
import org.bukkit.Bukkit;
import java.util.logging.Logger;

public class NMSBridgeManager {

    private static INMSBridge instance;
    private static final String SUPPORTED_VERSIONS = "1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11";

    public static void init() {
        Logger logger = Bukkit.getLogger();
        String version = Bukkit.getMinecraftVersion();

        String className = switch (version) {
            case "1.21.4" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_4";
            case "1.21.5" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_5";
            case "1.21.6" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_6";
            case "1.21.7" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_7";
            case "1.21.8" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_8";
            case "1.21.9" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_9";
            case "1.21.10" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_10";
            case "1.21.11" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_11";
            default -> {
                MinecraftBotLogging.logNmsUnsupportedVersion(logger, version, SUPPORTED_VERSIONS);
                throw new RuntimeException(
                        "[MinecraftBot] Versione Minecraft non supportata: " + version
                );
            }
        };
        MinecraftBotLogging.logNmsInitStart(logger, version, className, SUPPORTED_VERSIONS);

        try {
            Class<?> clazz = Class.forName(className);
            instance = (INMSBridge) clazz.getDeclaredConstructor().newInstance();
            MinecraftBotLogging.logNmsInitSuccess(logger, instance);
        } catch (ClassNotFoundException e) {
            MinecraftBotLogging.logNmsInitFailure(logger, className, e);
            throw new RuntimeException("[MinecraftBot] Classe bridge non trovata: " + className, e);
        } catch (Exception e) {
            MinecraftBotLogging.logNmsInitFailure(logger, className, e);
            throw new RuntimeException("[MinecraftBot] Impossibile caricare NMS Bridge", e);
        }
    }

    public static INMSBridge get() {
        if (instance == null) {
            throw new RuntimeException(
                    "[MinecraftBot] NMSBridgeManager non inizializzato! Chiama init() nell'onEnable."
            );
        }
        return instance;
    }
}
