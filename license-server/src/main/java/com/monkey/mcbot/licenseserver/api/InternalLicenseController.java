package com.monkey.mcbot.licenseserver.api;

import com.monkey.mcbot.licenseserver.api.dto.AdminCreateLicenseRequest;
import com.monkey.mcbot.licenseserver.api.dto.AdminCreateLicenseResponse;
import com.monkey.mcbot.licenseserver.api.dto.AdminLicenseSearchEntry;
import com.monkey.mcbot.licenseserver.api.dto.AdminRevokeLicenseRequest;
import com.monkey.mcbot.licenseserver.config.LicenseServerProperties;
import com.monkey.mcbot.licenseserver.domain.LicenseStatus;
import com.monkey.mcbot.licenseserver.repo.LicenseRepository;
import com.monkey.mcbot.licenseserver.repo.LicenseSearchSpec;
import com.monkey.mcbot.licenseserver.service.LicenseProvisioningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/licenses")
public class InternalLicenseController {

    private final LicenseProvisioningService provisioningService;
    private final LicenseRepository licenseRepository;
    private final LicenseServerProperties properties;

    public InternalLicenseController(LicenseProvisioningService provisioningService,
                                     LicenseRepository licenseRepository,
                                     LicenseServerProperties properties) {
        this.provisioningService = provisioningService;
        this.licenseRepository = licenseRepository;
        this.properties = properties;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCreateLicenseResponse create(
            @RequestHeader("X-Admin-Token") String adminToken,
            @Valid @RequestBody AdminCreateLicenseRequest request) {
        requireAdminToken(adminToken);
        return provisioningService.createLicense(request);
    }

    @PostMapping("/{id}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @RequestHeader("X-Admin-Token") String adminToken,
            @PathVariable UUID id,
            @Valid @RequestBody AdminRevokeLicenseRequest request) {
        requireAdminToken(adminToken);
        provisioningService.revokeLicense(id, request.reason());
    }

    @GetMapping
    public List<AdminLicenseSearchEntry> search(
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) LicenseStatus status,
            @RequestParam(required = false) String customer,
            @RequestParam(defaultValue = "25") @Min(1) @Max(25) int limit) {

        requireAdminToken(adminToken);

        var spec = LicenseSearchSpec.of(
                blankToNull(prefix),
                blankToNull(product),
                status,
                blankToNull(customer)
        );

        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        return licenseRepository.findAll(spec, pageable)
                .stream()
                .map(l -> new AdminLicenseSearchEntry(
                        l.getId(),
                        l.getLicenseKeyPrefix(),
                        l.getCustomerId(),
                        l.getProductCode(),
                        l.getPlanCode(),
                        l.getStatus(),
                        l.getExpiresAt(),
                        l.getMaxServers(),
                        l.getCreatedAt()
                ))
                .toList();
    }

    private void requireAdminToken(String adminToken) {
        if (!properties.adminToken().equals(adminToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin token");
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}