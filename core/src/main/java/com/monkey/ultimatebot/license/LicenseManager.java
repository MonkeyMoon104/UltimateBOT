package com.monkey.ultimatebot.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.access.runtime.PluginMetaAccess;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.wrapper.WrapperTask;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

public final class LicenseManager {

    private static final Duration GRACE_PERIOD = Duration.ofHours(24);
    private static final long HEARTBEAT_PERIOD_TICKS = 20L * 60L * 30L;
    private static final String PRODUCT_CODE = "ultimatebot";
    private static final String PLACEHOLDER_KEY = "XXXX-XXXX-XXXX-XXXX";

    private final UltimateBot plugin;
    private final InstallationIdStore installationIdStore;
    private final LicenseStateStore licenseStateStore;
    private final ServerFingerprintService fingerprintService;
    private final LicenseHttpClient licenseHttpClient;

    private @Nullable WrapperTask heartbeatTask;
    private String licenseKey = "";
    private String installationId = "";
    private String fingerprintHash = "";
    private String hostFingerprint = "";
    private int serverPort;

    public LicenseManager(UltimateBot plugin) {
        this.plugin = plugin;

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.installationIdStore = new InstallationIdStore();
        this.licenseStateStore = new LicenseStateStore(objectMapper);
        this.fingerprintService = new ServerFingerprintService();
        this.licenseHttpClient = new LicenseHttpClient(objectMapper);
    }

    public LicenseStartupResult validateOnStartup() {
        this.licenseKey = normalizeLicenseKey(plugin.getConfig().getString("license-key", ""));
        if (licenseKey.trim().isEmpty() || PLACEHOLDER_KEY.equalsIgnoreCase(licenseKey)) {
            return LicenseStartupResult.denied("MISSING_LICENSE_KEY", "license-key missing in config.yml");
        }

        try {
            this.installationId =
                    installationIdStore.loadOrCreate(plugin.getDataFolder().toPath());
            this.fingerprintHash = fingerprintService.computeInstallationFingerprint(plugin);
            this.hostFingerprint = fingerprintService.computeHostFingerprint(plugin);
            this.serverPort = fingerprintService.resolveServerPort(plugin);
            LicenseValidationResponse response = licenseHttpClient.validate(buildRequest());
            if (!response.allowed() && isHostLimit(response)) {

                String legacyHost = fingerprintService.computeHostFingerprintLegacy(plugin);
                if (!legacyHost.equals(hostFingerprint)) {
                    UltimateBotLogging.detail(
                            plugin.getLogger(),
                            "License",
                            "Host limit on stable fingerprint -> retrying legacy host fingerprint");
                    this.hostFingerprint = legacyHost;
                    response = licenseHttpClient.validate(buildRequest());
                }
            }
            if (!response.allowed()) {
                return LicenseStartupResult.denied(requireReasonCode(response), response.message());
            }

            licenseStateStore.saveSuccess(plugin.getDataFolder().toPath(), Instant.now());
            return LicenseStartupResult.allowed(false, "License valid");
        } catch (IOException ex) {
            UltimateBotLogging.warn(
                    plugin.getLogger(),
                    "License",
                    "Validation request failed -> "
                            + ex.getClass().getSimpleName()
                            + ": "
                            + safeMessage(ex.getMessage()));
            boolean graceAllowed =
                    licenseStateStore.hasValidGrace(plugin.getDataFolder().toPath(), GRACE_PERIOD, Instant.now());
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

        heartbeatTask = plugin.getWrapperManager()
                .active()
                .runAsyncRepeating(this::runHeartbeatCheck, HEARTBEAT_PERIOD_TICKS, HEARTBEAT_PERIOD_TICKS);
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
                UltimateBotLogging.warn(
                        plugin.getLogger(),
                        "License",
                        "Runtime validation denied -> " + reasonCode + " | " + safeMessage(response.message()));
                disablePluginSync("License denied during runtime: " + reasonCode);
                return;
            }

            licenseStateStore.saveSuccess(plugin.getDataFolder().toPath(), Instant.now());
            UltimateBotLogging.detail(
                    plugin.getLogger(), "License", "Heartbeat ok -> status=" + safeMessage(response.status()));
        } catch (Exception ex) {
            boolean graceAllowed =
                    licenseStateStore.hasValidGrace(plugin.getDataFolder().toPath(), GRACE_PERIOD, Instant.now());
            if (graceAllowed) {
                UltimateBotLogging.warn(plugin.getLogger(), "License", "Heartbeat failed -> using remaining grace");
                return;
            }

            UltimateBotLogging.error(plugin.getLogger(), "License", "Heartbeat failed and grace expired", ex);
            disablePluginSync("License server unreachable and grace expired");
        }
    }

    private void disablePluginSync(String reason) {
        plugin.getWrapperManager().active().runSync(() -> {
            plugin.getLogger().severe(reason);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        });
    }

    private LicenseValidationRequest buildRequest() {
        return new LicenseValidationRequest(
                licenseKey,
                PRODUCT_CODE,
                PluginMetaAccess.version(plugin),
                installationId,
                fingerprintHash,
                hostFingerprint,
                serverPort);
    }

    private String normalizeLicenseKey(@Nullable String rawLicenseKey) {
        return rawLicenseKey == null ? "" : rawLicenseKey.trim().toUpperCase(Locale.ROOT);
    }

    private String requireReasonCode(LicenseValidationResponse response) {
        if (response.reasonCode() == null || response.reasonCode().trim().isEmpty()) {
            return "UNKNOWN_DENIAL";
        }
        return response.reasonCode();
    }

    private static boolean isHostLimit(LicenseValidationResponse response) {
        String reason = response.reasonCode();
        return reason != null && "HOST_LIMIT_REACHED".equalsIgnoreCase(reason.trim());
    }

    private String safeMessage(@Nullable String value) {
        return value == null || value.trim().isEmpty() ? "n/a" : value;
    }
}
