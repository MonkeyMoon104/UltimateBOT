package com.monkey.ultimatebot.nms;

import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

public class NMSBridgeManager {

    private static @Nullable INMSBridge instance;
    private static final String SUPPORTED_VERSIONS =
            "1.17+, 1.18.x, 1.19.x, 1.20.x, 1.21.x, 26.1.x, 26.2.x"
                    + " (1.8–1.16.5: limited stub until legacy NMS modules ship)";

    public static void init() {
        init(java.util.logging.Logger.getLogger("UltimateBot"));
    }

    public static void init(Logger logger) {
        String version = MinecraftVersionAccess.minecraftVersion();
        String className = resolveBridgeClassName(version);
        if (className == null) {
            UltimateBotLogging.logNmsUnsupportedVersion(logger, version, SUPPORTED_VERSIONS);
            throw new RuntimeException("[UltimateBot] Unsupported Minecraft version: " + version);
        }
        UltimateBotLogging.logNmsInitStart(logger, version, className, SUPPORTED_VERSIONS);

        try {
            if (StubNMSBridge.class.getName().equals(className)) {
                instance = StubNMSBridge.forVersion(version);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = (INMSBridge) clazz.getDeclaredConstructor().newInstance();
            }
            UltimateBotLogging.logNmsInitSuccess(logger, instance);
            warnIfRuntimeUnsupported(logger, version, instance);
        } catch (ClassNotFoundException e) {
            if (isPre117(version)) {
                logger.warning(
                        "[UltimateBot] NMS bridge "
                                + className
                                + " is not in this build yet; limited mode on Minecraft "
                                + version
                                + ".");
                instance = StubNMSBridge.forVersion(version);
                UltimateBotLogging.logNmsInitSuccess(logger, instance);
                warnIfRuntimeUnsupported(logger, version, instance);
                return;
            }
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

    public static Set<PlatformCapability> capabilities() {
        return get().capabilities();
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static boolean isBotRuntimeSupported() {
        return instance != null && instance.isBotRuntimeSupported();
    }

    public static boolean supports(PlatformCapability capability) {
        return get().supports(capability);
    }

    /** Like {@link #supports} but returns {@code false} when the bridge is not yet initialized. */
    public static boolean supportsOrFalse(PlatformCapability capability) {
        return instance != null && instance.supports(capability);
    }

    public static boolean supportsCombatMode(CombatMode mode) {
        return get().supportsCombatMode(mode);
    }

    private static @Nullable String resolveBridgeClassName(String version) {
        if (version == null || version.isEmpty() || "unknown".equalsIgnoreCase(version)) {
            return StubNMSBridge.class.getName();
        }
        String normalized = version.toLowerCase(Locale.ROOT).trim();

        String legacy = resolveLegacyBridge(normalized);
        if (legacy != null) {
            return legacy;
        }

                switch (normalized) {
            case "1.17":
                // Spigot maps differ from 1.17.1; CodeMC remapped-mojang + official 1.17 maps (no
                // Paper 1.17.0 userdev bundle).
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_17";
            case "1.17.1":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_17_1";
            case "1.18":
            case "1.18.1":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_18_1";
            case "1.18.2":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_18_2";
            case "1.19":
                // Spigot intermediate names differ from 1.19.1/1.19.2 within the same Craft v1_19_R1.
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_19";
            case "1.19.1":
            case "1.19.2":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_19_2";
            case "1.19.3":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_19_3";
            case "1.19.4":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_19_4";
            case "1.20":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_20";
            case "1.20.1":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_20_1";
            case "1.20.2":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_20_2";
            case "1.20.3":
            case "1.20.4":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_20_4";
            case "1.20.5":
            case "1.20.6":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_20_6";
            case "1.21":
            case "1.21.1":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_1";
            case "1.21.3":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_3";
            case "1.21.4":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_4";
            case "1.21.5":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_5";
            case "1.21.6":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_6";
            case "1.21.7":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_7";
            case "1.21.8":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_8";
            case "1.21.9":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_9";
            case "1.21.10":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_10";
            case "1.21.11":
                return "com.monkey.ultimatebot.nms.NMSBridge_v1_21_11";
            default:
                return resolveV26Bridge(normalized);
        }
    }

    private static void warnIfRuntimeUnsupported(Logger logger, String version, INMSBridge bridge) {
        if (!bridge.isBotRuntimeSupported()) {
            logger.warning(
                    "[UltimateBot] Limited mode on Minecraft "
                            + version
                            + ": fake-player bots are not implemented for this revision yet."
                            + " Plugin stays enabled.");
        }
    }

    /**
     * Pre-1.17 bridges. Class names stay mapped so a later {@code NMS/v1_16_R3} (etc.) commit only
     * needs settings + shadow wiring; missing classes fall back to {@link StubNMSBridge}.
     */
    private static @Nullable String resolveLegacyBridge(String version) {
        // 1.8.x
        if ("1.8".equals(version) || "1.8.0".equals(version) || "1.8.1".equals(version) || "1.8.2".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_8_R1";
        }
        if ("1.8.3".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_8_R2";
        }
        if ("1.8.4".equals(version)
                || "1.8.5".equals(version)
                || "1.8.6".equals(version)
                || "1.8.7".equals(version)
                || "1.8.8".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_8_R3";
        }

        // 1.9.x — R1 for 1.9–1.9.2, R2 for 1.9.3–1.9.4
        if ("1.9".equals(version) || "1.9.0".equals(version) || "1.9.1".equals(version) || "1.9.2".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_9_R1";
        }
        if ("1.9.3".equals(version) || "1.9.4".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_9_R2";
        }

        // 1.10.x
        if ("1.10".equals(version) || version.startsWith("1.10.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_10_R1";
        }

        // 1.11.x
        if ("1.11".equals(version) || version.startsWith("1.11.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_11_R1";
        }

        // 1.12.x
        if ("1.12".equals(version) || version.startsWith("1.12.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_12_R1";
        }

        // 1.13.x — R1 for 1.13, R2 for 1.13.1–1.13.2
        if ("1.13".equals(version) || "1.13.0".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_13_R1";
        }
        if ("1.13.1".equals(version) || "1.13.2".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_13_R2";
        }

        // 1.14.x
        if ("1.14".equals(version) || version.startsWith("1.14.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_14_R1";
        }

        // 1.15.x
        if ("1.15".equals(version) || version.startsWith("1.15.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_15_R1";
        }

        // 1.16.x — R1 / R2 / R3
        if ("1.16".equals(version) || "1.16.0".equals(version) || "1.16.1".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_16_R1";
        }
        if ("1.16.2".equals(version) || "1.16.3".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_16_R2";
        }
        if ("1.16.4".equals(version) || "1.16.5".equals(version)) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v1_16_R3";
        }

        if (isPre117(version)) {
            return StubNMSBridge.class.getName();
        }
        return null;
    }

    /** 1.8 through 1.16.x (inclusive). */
    private static boolean isPre117(String version) {
        if (version.startsWith("26.")) {
            return false;
        }
        int firstDot = version.indexOf('.');
        if (firstDot <= 0 || firstDot + 1 >= version.length()) {
            return true;
        }
        int secondDot = version.indexOf('.', firstDot + 1);
        String majorText = version.substring(0, firstDot);
        String minorText =
                secondDot < 0 ? version.substring(firstDot + 1) : version.substring(firstDot + 1, secondDot);
        try {
            int major = Integer.parseInt(majorText);
            int minor = Integer.parseInt(minorText);
            if (major != 1) {
                return major < 1;
            }
            return minor < 17;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private static @Nullable String resolveV26Bridge(String version) {
        if ("26.1".equals(version) || version.startsWith("26.1.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v26_1";
        }
        if ("26.2".equals(version) || version.startsWith("26.2.")) {
            return "com.monkey.ultimatebot.nms.NMSBridge_v26_2";
        }
        // Unknown modern-looking version: prefer stub over hard crash so the plugin can enable.
        return StubNMSBridge.class.getName();
    }

    public static INMSBridge get() {
        if (instance == null) {
            throw new RuntimeException("[UltimateBot] NMSBridgeManager is not initialized. Call init() in onEnable.");
        }
        return java.util.Objects.requireNonNull(instance, "NMS bridge");
    }
}
