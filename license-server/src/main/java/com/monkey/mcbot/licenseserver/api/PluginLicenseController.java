package com.monkey.mcbot.licenseserver.api;

import com.monkey.mcbot.licenseserver.api.dto.PluginLicenseRequest;
import com.monkey.mcbot.licenseserver.api.dto.PluginLicenseResponse;
import com.monkey.mcbot.licenseserver.service.LicenseValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plugin")
public class PluginLicenseController {

    private final LicenseValidationService validationService;

    public PluginLicenseController(LicenseValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public PluginLicenseResponse validate(@Valid @RequestBody PluginLicenseRequest request,
                                          HttpServletRequest servletRequest) {
        return validationService.validate(request, servletRequest.getRemoteAddr(), "VALIDATE");
    }

    @PostMapping("/heartbeat")
    public PluginLicenseResponse heartbeat(@Valid @RequestBody PluginLicenseRequest request,
                                           HttpServletRequest servletRequest) {
        return validationService.validate(request, servletRequest.getRemoteAddr(), "HEARTBEAT");
    }
}
