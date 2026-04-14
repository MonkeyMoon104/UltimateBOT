package com.monkey.mcbot.licenseserver.api;

import com.monkey.mcbot.licenseserver.api.dto.AdminPluginVersionEntry;
import com.monkey.mcbot.licenseserver.config.LicenseServerProperties;
import com.monkey.mcbot.licenseserver.service.PluginUpdateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1/internal/updates")
public class InternalUpdateController {

    private final PluginUpdateService pluginUpdateService;
    private final LicenseServerProperties properties;

    public InternalUpdateController(PluginUpdateService pluginUpdateService,
                                    LicenseServerProperties properties) {
        this.pluginUpdateService = pluginUpdateService;
        this.properties = properties;
    }

    @GetMapping
    public List<AdminPluginVersionEntry> listCatalog(@RequestHeader("X-Admin-Token") String adminToken,
                                                      @RequestParam String product) {
        requireAdminToken(adminToken);
        return pluginUpdateService.listCatalog(product);
    }

    private void requireAdminToken(String adminToken) {
        if (!properties.adminToken().equals(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }
    }
}
