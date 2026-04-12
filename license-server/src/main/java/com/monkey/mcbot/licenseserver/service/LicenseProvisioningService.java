package com.monkey.mcbot.licenseserver.service;

import com.monkey.mcbot.licenseserver.api.dto.AdminCreateLicenseRequest;
import com.monkey.mcbot.licenseserver.api.dto.AdminCreateLicenseResponse;
import com.monkey.mcbot.licenseserver.domain.LicenseActivationEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseStatus;
import com.monkey.mcbot.licenseserver.repo.LicenseActivationRepository;
import com.monkey.mcbot.licenseserver.repo.LicenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LicenseProvisioningService {

    private final LicenseRepository licenseRepository;
    private final LicenseKeyGenerator keyGenerator;
    private final LicenseHmacService hmacService;
    private final LicenseEventService eventService;
    private final LicenseEncryptionService encryptionService;
    private final LicenseActivationRepository activationRepository;

    public LicenseProvisioningService(LicenseRepository licenseRepository,
                                      LicenseKeyGenerator keyGenerator,
                                      LicenseHmacService hmacService,
                                      LicenseEventService eventService,
                                      LicenseEncryptionService encryptionService,
                                      LicenseActivationRepository activationRepository) {
        this.licenseRepository = licenseRepository;
        this.keyGenerator = keyGenerator;
        this.hmacService = hmacService;
        this.eventService = eventService;
        this.encryptionService = encryptionService;
        this.activationRepository = activationRepository;
    }

    @Transactional
    public AdminCreateLicenseResponse createLicense(AdminCreateLicenseRequest request) {
        String licenseKey = generateUniqueKey();

        LicenseEntity license = new LicenseEntity();
        license.setLicenseKeyHmac(hmacService.hmac(licenseKey));
        license.setLicenseKeyPrefix(hmacService.prefix(licenseKey));
        license.setLicenseKeyEncrypted(encryptionService.encrypt(licenseKey));
        license.setCustomerId(request.customerId());
        license.setProductCode(request.productCode());
        license.setPlanCode(request.planCode());
        license.setStatus(LicenseStatus.ACTIVE);
        license.setExpiresAt(request.expiresAt());
        license.setMaxServers(request.maxServers());

        LicenseEntity saved = licenseRepository.save(license);
        eventService.log(saved, null, "CREATE", "SUCCESS", null, "License created", null);

        return new AdminCreateLicenseResponse(
                saved.getId(),
                licenseKey,
                saved.getStatus(),
                saved.getExpiresAt(),
                saved.getMaxServers()
        );
    }

    @Transactional
    public void revokeLicense(UUID licenseId, String reason) {
        LicenseEntity license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found: " + licenseId));

        license.setStatus(LicenseStatus.REVOKED);
        license.setRevokedReason(reason);
        licenseRepository.save(license);
        eventService.log(license, null, "REVOKE", "SUCCESS", "REVOKED", reason, null);
    }

    @Transactional
    public void deleteLicense(UUID licenseId) {
        LicenseEntity license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found: " + licenseId));
        licenseRepository.delete(license);
    }

    @Transactional
    public void clearInactiveActivations(UUID licenseId) {
        LicenseEntity license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found: " + licenseId));

        Instant recentThreshold = Instant.now().minusSeconds(5 * 60);

        List<LicenseActivationEntity> inactive = activationRepository.findAllByLicense(license)
                .stream()
                .filter(a -> a.getLastSeenAt().isBefore(recentThreshold))
                .toList();

        if (inactive.isEmpty()) {
            throw new IllegalStateException("No inactive activations — all servers appear to be online.");
        }

        activationRepository.deleteAll(inactive);
        eventService.log(license, null, "CLEAR_INACTIVE_ACTIVATIONS", "SUCCESS", null,
                "Cleared " + inactive.size() + " inactive activation(s)", null);
    }

    private String generateUniqueKey() {
        while (true) {
            String candidate = keyGenerator.generate();
            if (!licenseRepository.existsByLicenseKeyHmac(hmacService.hmac(candidate))) {
                return candidate;
            }
        }
    }
}
