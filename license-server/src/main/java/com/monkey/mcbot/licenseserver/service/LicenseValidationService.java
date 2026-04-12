package com.monkey.mcbot.licenseserver.service;

import com.monkey.mcbot.licenseserver.api.dto.PluginLicenseRequest;
import com.monkey.mcbot.licenseserver.api.dto.PluginLicenseResponse;
import com.monkey.mcbot.licenseserver.config.LicenseServerProperties;
import com.monkey.mcbot.licenseserver.domain.ActivationStatus;
import com.monkey.mcbot.licenseserver.domain.LicenseActivationEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseStatus;
import com.monkey.mcbot.licenseserver.repo.LicenseActivationRepository;
import com.monkey.mcbot.licenseserver.repo.LicenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LicenseValidationService {

    private final LicenseRepository licenseRepository;
    private final LicenseActivationRepository activationRepository;
    private final LicenseHmacService hmacService;
    private final LicenseEventService eventService;
    private final LicenseServerProperties properties;

    public LicenseValidationService(LicenseRepository licenseRepository,
                                    LicenseActivationRepository activationRepository,
                                    LicenseHmacService hmacService,
                                    LicenseEventService eventService,
                                    LicenseServerProperties properties) {
        this.licenseRepository = licenseRepository;
        this.activationRepository = activationRepository;
        this.hmacService = hmacService;
        this.eventService = eventService;
        this.properties = properties;
    }

    @Transactional
    public PluginLicenseResponse validate(PluginLicenseRequest request, String ip, String eventType) {
        LicenseEntity license = licenseRepository.findByLicenseKeyHmac(hmacService.hmac(request.licenseKey())).orElse(null);

        if (license == null) {
            eventService.log(null, request.installationId(), eventType, "DENIED", "NOT_FOUND", "License not found", ip);
            return PluginLicenseResponse.denied(null, "NOT_FOUND", "License not found");
        }

        if (!license.getProductCode().equalsIgnoreCase(request.product())) {
            eventService.log(license, request.installationId(), eventType, "DENIED", "PRODUCT_MISMATCH", "Wrong product", ip);
            return PluginLicenseResponse.denied(license.getStatus(), "PRODUCT_MISMATCH", "Wrong product");
        }

        if (license.getStatus() == LicenseStatus.REVOKED) {
            eventService.log(license, request.installationId(), eventType, "DENIED", "REVOKED", "License revoked", ip);
            return PluginLicenseResponse.denied(LicenseStatus.REVOKED, "REVOKED", "License revoked");
        }

        if (license.getStatus() == LicenseStatus.ABUSE_BLOCKED) {
            eventService.log(license, request.installationId(), eventType, "DENIED", "ABUSE_BLOCKED", "License blocked", ip);
            return PluginLicenseResponse.denied(LicenseStatus.ABUSE_BLOCKED, "ABUSE_BLOCKED", "License blocked");
        }

        if (license.getExpiresAt() != null && license.getExpiresAt().isBefore(Instant.now())) {
            license.setStatus(LicenseStatus.EXPIRED);
            licenseRepository.save(license);
            eventService.log(license, request.installationId(), eventType, "DENIED", "EXPIRED", "License expired", ip);
            return PluginLicenseResponse.denied(LicenseStatus.EXPIRED, "EXPIRED", "License expired");
        }

        Instant activeSince = Instant.now().minusSeconds(properties.activeWindowMinutes() * 60L);
        LicenseActivationEntity activation = activationRepository.findByLicenseAndInstallationId(license, request.installationId())
                .orElse(null);

        if (activation == null) {
            long activeCount = activationRepository.countActiveByLicense(license, activeSince);
            if (activeCount >= license.getMaxServers()) {
                eventService.log(license, request.installationId(), eventType, "DENIED", "MAX_SERVERS_REACHED", "Maximum active servers reached", ip);
                return PluginLicenseResponse.denied(LicenseStatus.ACTIVE, "MAX_SERVERS_REACHED", "Maximum active servers reached");
            }

            activation = new LicenseActivationEntity();
            activation.setLicense(license);
            activation.setInstallationId(request.installationId());
        }

        activation.setFingerprintHash(request.fingerprintHash());
        activation.setLastIp(ip);
        activation.setLastPluginVersion(request.pluginVersion());
        activation.setLastSeenAt(Instant.now());
        activation.setStatus(ActivationStatus.ACTIVE);
        activationRepository.save(activation);

        eventService.log(license, request.installationId(), eventType, "ALLOWED", null, "License valid", ip);
        return PluginLicenseResponse.allowed(
                license.getStatus(),
                license.getPlanCode(),
                license.getExpiresAt(),
                license.getMaxServers(),
                "License valid",
                Instant.now().plusSeconds(properties.gracePeriodHours() * 3600L)
        );
    }
}
