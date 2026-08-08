package com.monkey.ultimatebot.nms;

import com.monkey.ultimatebot.logging.UltimateBotLogging;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

public class NMSBridgeManager {

    private static @Nullable INMSBridge instance;
    private static final String SUPPORTED_VERSIONS =
            "1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.x, 26.2.x";

    public static void init() {
        init(Bukkit.getLogger());
    }

    public static void init(Logger logger) {
        String version = Bukkit.getMinecraftVersion();

        String className =
                switch (version) {
                    case "1.21.4" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_4";
                    case "1.21.5" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_5";
                    case "1.21.6" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_6";
                    case "1.21.7" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_7";
                    case "1.21.8" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_8";
                    case "1.21.9" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_9";
                    case "1.21.10" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_10";
                    case "1.21.11" -> "com.monkey.ultimatebot.nms.NMSBridge_v1_21_11";
                    default -> resolveV26Bridge(version);
                };
        if (className == null) {
            UltimateBotLogging.logNmsUnsupportedVersion(logger, version, SUPPORTED_VERSIONS);
            throw new RuntimeException("[UltimateBot] Unsupported Minecraft version: " + version);
        }
        UltimateBotLogging.logNmsInitStart(logger, version, className, SUPPORTED_VERSIONS);

        try {
            Class<?> clazz = Class.forName(className);
            instance = (INMSBridge) clazz.getDeclaredConstructor().newInstance();
            UltimateBotLogging.logNmsInitSuccess(logger, instance);
        } catch (ClassNotFoundException e) {
            UltimateBotLogging.logNmsInitFailure(logger, className, e);
            throw new RuntimeException("[UltimateBot] Bridge class not found: " + className, e);
        } catch (Exception e) {
            UltimateBotLogging.logNmsInitFailure(logger, className, e);
            throw new RuntimeException("[UltimateBot] Failed to load NMS bridge", e);
        }
    }

    public static String getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    public static java.util.Set<com.monkey.ultimatebot.common.model.PlatformCapability> capabilities() {
        return get().capabilities();
    }

    public static boolean supports(com.monkey.ultimatebot.common.model.PlatformCapability capability) {
        return get().supports(capability);
    }

    private static @Nullable String resolveV26Bridge(String version) {
        if ("26.1".equals(version) || version.startsWith("26.1.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v26_1";
        }
        if ("26.2".equals(version) || version.startsWith("26.2.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v26_2";
        }
        return null;
    }

    public static INMSBridge get() {
        if (instance == null) {
            throw new RuntimeException("[UltimateBot] NMSBridgeManager is not initialized. Call init() in onEnable.");
        }
        return java.util.Objects.requireNonNull(instance, "NMS bridge");
    }
}
