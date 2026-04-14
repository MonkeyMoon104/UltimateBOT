package com.monkey.mcbot.licenseserver.service;

import com.monkey.mcbot.licenseserver.api.dto.AdminPluginVersionEntry;
import com.monkey.mcbot.licenseserver.api.dto.PluginUpdateCheckRequest;
import com.monkey.mcbot.licenseserver.api.dto.PluginUpdateCheckResponse;
import com.monkey.mcbot.licenseserver.domain.PluginVersionEntity;
import com.monkey.mcbot.licenseserver.repo.PluginVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginUpdateService {

    private final PluginVersionRepository repository;
    private final PluginVersionComparator comparator;

    public PluginUpdateService(PluginVersionRepository repository,
                               PluginVersionComparator comparator) {
        this.repository = repository;
        this.comparator = comparator;
    }

    public PluginUpdateCheckResponse check(PluginUpdateCheckRequest request) {
        String product = request.product().trim().toLowerCase();
        String currentVersion = request.currentVersion().trim();

        PluginVersionEntity latest = repository.findFirstByProductCodeAndLatestTrue(product).orElse(null);
        if (latest == null) {
            return new PluginUpdateCheckResponse(
                    false,
                    product,
                    currentVersion,
                    currentVersion,
                    null,
                    "No update metadata configured"
            );
        }

        boolean updateAvailable = comparator.compare(latest.getVersionName(), currentVersion) > 0;
        return new PluginUpdateCheckResponse(
                updateAvailable,
                product,
                currentVersion,
                latest.getVersionName(),
                latest.getReleaseUrl(),
                updateAvailable ? "New version available" : "Plugin is up to date"
        );
    }

    public List<AdminPluginVersionEntry> listCatalog(String product) {
        return repository.findByProductCodeOrderByVersionNameAsc(product.trim().toLowerCase())
                .stream()
                .map(v -> new AdminPluginVersionEntry(
                        v.getProductCode(),
                        v.getVersionName(),
                        v.getReleaseUrl(),
                        v.isLatest()
                ))
                .toList();
    }
}
