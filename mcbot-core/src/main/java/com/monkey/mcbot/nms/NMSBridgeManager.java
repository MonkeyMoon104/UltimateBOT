package com.monkey.mcbot.nms;

import org.bukkit.Bukkit;
import java.util.logging.Logger;

public class NMSBridgeManager {

    private static INMSBridge instance;

    public static void init() {
        Logger logger = Bukkit.getLogger();
        String version = Bukkit.getMinecraftVersion();

        logger.info("[MinecraftBot] ==============================");
        logger.info("[MinecraftBot] Inizializzazione NMS Bridge...");
        logger.info("[MinecraftBot] Versione Minecraft rilevata: " + version);

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
                logger.severe("[MinecraftBot] Versione non supportata: " + version);
                logger.severe("[MinecraftBot] Versioni supportate: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11");
                throw new RuntimeException(
                        "[MinecraftBot] Versione Minecraft non supportata: " + version
                );
            }
        };

        logger.info("[MinecraftBot] Caricamento classe: " + className);

        try {
            Class<?> clazz = Class.forName(className);
            instance = (INMSBridge) clazz.getDeclaredConstructor().newInstance();
            logger.info("[MinecraftBot] NMS Bridge caricato con successo!");
            logger.info("[MinecraftBot] Implementazione attiva: " + instance.getClass().getSimpleName());
            logger.info("[MinecraftBot] ==============================");
        } catch (ClassNotFoundException e) {
            logger.severe("[MinecraftBot] Classe bridge non trovata: " + className);
            logger.severe("[MinecraftBot] Il jar potrebbe essere corrotto o incompleto.");
            throw new RuntimeException("[MinecraftBot] Classe bridge non trovata: " + className, e);
        } catch (Exception e) {
            logger.severe("[MinecraftBot] Errore durante il caricamento del bridge: " + e.getMessage());
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