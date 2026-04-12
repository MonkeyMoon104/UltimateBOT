package com.monkey.mcbot.licenseserver.api;

import com.monkey.mcbot.licenseserver.api.dto.AdminCreateLicenseRequest;
import com.monkey.mcbot.licenseserver.api.dto.AdminCreateLicenseResponse;
import com.monkey.mcbot.licenseserver.api.dto.AdminRevokeLicenseRequest;
import com.monkey.mcbot.licenseserver.config.LicenseServerProperties;
import com.monkey.mcbot.licenseserver.service.LicenseProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/licenses")
public class InternalLicenseController {

    private final LicenseProvisioningService provisioningService;
    private final LicenseServerProperties properties;

    public InternalLicenseController(LicenseProvisioningService provisioningService,
                                     LicenseServerProperties properties) {
        this.provisioningService = provisioningService;
        this.properties = properties;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCreateLicenseResponse create(@RequestHeader("X-Admin-Token") String adminToken,
                                             @Valid @RequestBody AdminCreateLicenseRequest request) {
        requireAdminToken(adminToken);
        return provisioningService.createLicense(request);
    }

    @PostMapping("/{id}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@RequestHeader("X-Admin-Token") String adminToken,
                       @PathVariable UUID id,
                       @Valid @RequestBody AdminRevokeLicenseRequest request) {
        requireAdminToken(adminToken);
        provisioningService.revokeLicense(id, request.reason());
    }

    private void requireAdminToken(String adminToken) {
        if (!properties.adminToken().equals(adminToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin token");
        }
    }
}
