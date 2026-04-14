package com.monkey.mcbot.licenseserver.api;

import com.monkey.mcbot.licenseserver.api.dto.PluginLicenseRequest;
import com.monkey.mcbot.licenseserver.api.dto.PluginLicenseResponse;
import com.monkey.mcbot.licenseserver.api.dto.PluginUpdateCheckRequest;
import com.monkey.mcbot.licenseserver.api.dto.PluginUpdateCheckResponse;
import com.monkey.mcbot.licenseserver.service.LicenseValidationService;
import com.monkey.mcbot.licenseserver.service.PluginUpdateService;
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
    private final PluginUpdateService pluginUpdateService;

    public PluginLicenseController(LicenseValidationService validationService,
                                   PluginUpdateService pluginUpdateService) {
        this.validationService = validationService;
        this.pluginUpdateService = pluginUpdateService;
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

    @PostMapping("/updates/check")
    public PluginUpdateCheckResponse checkUpdate(@Valid @RequestBody PluginUpdateCheckRequest request) {
        return pluginUpdateService.check(request);
    }
}
