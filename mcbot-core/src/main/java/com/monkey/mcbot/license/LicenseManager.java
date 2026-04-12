package com.monkey.mcbot.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.logging.MinecraftBotLogging;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public final class LicenseManager {

    private static final Duration GRACE_PERIOD = Duration.ofHours(24);
    private static final long HEARTBEAT_PERIOD_TICKS = 20L * 60L * 30L;
    private static final String PRODUCT_CODE = "minecraftbot";
    private static final String PLACEHOLDER_KEY = "XXXX-XXXX-XXXX-XXXX";

    private final JavaPlugin plugin;
    private final InstallationIdStore installationIdStore;
    private final LicenseStateStore licenseStateStore;
    private final ServerFingerprintService fingerprintService;
    private final LicenseHttpClient licenseHttpClient;

    private BukkitTask heartbeatTask;
    private String licenseKey;
    private String installationId;
    private String fingerprintHash;

    public LicenseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        this.installationIdStore = new InstallationIdStore();
        this.licenseStateStore = new LicenseStateStore(objectMapper);
        this.fingerprintService = new ServerFingerprintService();
        this.licenseHttpClient = new LicenseHttpClient(objectMapper);
    }

    public LicenseStartupResult validateOnStartup() {
        this.licenseKey = normalizeLicenseKey(plugin.getConfig().getString("license-key", ""));
        if (licenseKey.isBlank() || PLACEHOLDER_KEY.equalsIgnoreCase(licenseKey)) {
            return LicenseStartupResult.denied("MISSING_LICENSE_KEY", "license-key missing in config.yml");
        }

        try {
            this.installationId = installationIdStore.loadOrCreate(plugin.getDataFolder().toPath());
            this.fingerprintHash = fingerprintService.compute(plugin);
            LicenseValidationResponse response = licenseHttpClient.validate(buildRequest());
            if (!response.allowed()) {
                return LicenseStartupResult.denied(requireReasonCode(response), response.message());
            }

            licenseStateStore.saveSuccess(plugin.getDataFolder().toPath(), Instant.now());
            return LicenseStartupResult.allowed(false, "License valid");
        } catch (IOException ex) {
            boolean graceAllowed = licenseStateStore.hasValidGrace(plugin.getDataFolder().toPath(), GRACE_PERIOD, Instant.now());
            if (!graceAllowed) {
                return LicenseStartupResult.denied("LICENSE_SERVER_UNREACHABLE", "License server unreachable");
            }
            return LicenseStartupResult.allowed(true, "License server unreachable, starting in grace mode");
        }
    }

    public void startHeartbeat() {
        if (heartbeatTask != null) {
            return;
        }

        heartbeatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::runHeartbeatCheck,
                HEARTBEAT_PERIOD_TICKS, HEARTBEAT_PERIOD_TICKS);
    }

    public void shutdown() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    private void runHeartbeatCheck() {
        try {
            LicenseValidationResponse response = licenseHttpClient.heartbeat(buildRequest());
            if (!response.allowed()) {
                String reasonCode = requireReasonCode(response);
                MinecraftBotLogging.warn(plugin.getLogger(), "License",
                        "Runtime validation denied -> " + reasonCode + " | " + safeMessage(response.message()));
                disablePluginSync("License denied during runtime: " + reasonCode);
                return;
            }

            licenseStateStore.saveSuccess(plugin.getDataFolder().toPath(), Instant.now());
            MinecraftBotLogging.detail(plugin.getLogger(), "License", "Heartbeat ok -> status=" + safeMessage(response.status()));
        } catch (Exception ex) {
            boolean graceAllowed = licenseStateStore.hasValidGrace(plugin.getDataFolder().toPath(), GRACE_PERIOD, Instant.now());
            if (graceAllowed) {
                MinecraftBotLogging.warn(plugin.getLogger(), "License", "Heartbeat failed -> using remaining grace");
                return;
            }

            MinecraftBotLogging.error(plugin.getLogger(), "License", "Heartbeat failed and grace expired", ex);
            disablePluginSync("License server unreachable and grace expired");
        }
    }

    private void disablePluginSync(String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().severe(reason);
            Bukkit.getPluginManager().disablePlugin(plugin);
        });
    }

    private LicenseValidationRequest buildRequest() {
        return new LicenseValidationRequest(
                licenseKey,
                PRODUCT_CODE,
                plugin.getPluginMeta().getVersion(),
                installationId,
                fingerprintHash
        );
    }

    private String normalizeLicenseKey(String rawLicenseKey) {
        return rawLicenseKey == null ? "" : rawLicenseKey.trim().toUpperCase(Locale.ROOT);
    }

    private String requireReasonCode(LicenseValidationResponse response) {
        if (response.reasonCode() == null || response.reasonCode().isBlank()) {
            return "UNKNOWN_DENIAL";
        }
        return response.reasonCode();
    }

    private String safeMessage(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }
}
