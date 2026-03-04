package com.monkey.mcbot.nms;

import org.bukkit.Bukkit;
import java.util.logging.Logger;

public class NMSBridgeManager {

    private static INMSBridge instance;

    public static void init() {
        Logger logger = Bukkit.getLogger();
        String version = Bukkit.getMinecraftVersion();

        logger.info("[SandboxTraining] ==============================");
        logger.info("[SandboxTraining] Inizializzazione NMS Bridge...");
        logger.info("[SandboxTraining] Versione Minecraft rilevata: " + version);

        String className = switch (version) {
            case "1.21.4" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_4";
            case "1.21.5" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_5";
            case "1.21.6" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_6";
            case "1.21.7" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_7";
            case "1.21.8" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_8";
            case "1.21.9" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_9";
            case "1.21.10" -> "com.monkey.mcbot.nms.NMSBridge_v1_21_10";
            default -> {
                logger.severe("[SandboxTraining] Versione non supportata: " + version);
                logger.severe("[SandboxTraining] Versioni supportate: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10");
                throw new RuntimeException(
                        "[SandboxTraining] Versione Minecraft non supportata: " + version
                );
            }
        };

        logger.info("[SandboxTraining] Caricamento classe: " + className);

        try {
            Class<?> clazz = Class.forName(className);
            instance = (INMSBridge) clazz.getDeclaredConstructor().newInstance();
            logger.info("[SandboxTraining] NMS Bridge caricato con successo!");
            logger.info("[SandboxTraining] Implementazione attiva: " + instance.getClass().getSimpleName());
            logger.info("[SandboxTraining] ==============================");
        } catch (ClassNotFoundException e) {
            logger.severe("[SandboxTraining] Classe bridge non trovata: " + className);
            logger.severe("[SandboxTraining] Il jar potrebbe essere corrotto o incompleto.");
            throw new RuntimeException("[SandboxTraining] Classe bridge non trovata: " + className, e);
        } catch (Exception e) {
            logger.severe("[SandboxTraining] Errore durante il caricamento del bridge: " + e.getMessage());
            throw new RuntimeException("[SandboxTraining] Impossibile caricare NMS Bridge", e);
        }
    }

    public static INMSBridge get() {
        if (instance == null) {
            throw new RuntimeException(
                    "[SandboxTraining] NMSBridgeManager non inizializzato! Chiama init() nell'onEnable."
            );
        }
        return instance;
    }
}