package com.monkey.mcbot.licenseserver.service;

import com.monkey.mcbot.licenseserver.domain.PluginVersionEntity;
import com.monkey.mcbot.licenseserver.repo.PluginVersionRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PluginVersionCatalogSyncService {

    private final PluginVersionCatalog catalog;
    private final PluginVersionRepository repository;

    public PluginVersionCatalogSyncService(PluginVersionCatalog catalog,
                                           PluginVersionRepository repository) {
        this.catalog = catalog;
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncOnStartup() {
        List<PluginVersionCatalog.CatalogVersion> versions = catalog.versions();
        if (versions.isEmpty()) {
            throw new IllegalStateException("Catalog cannot be empty");
        }

        Map<String, Integer> latestCountByProduct = new HashMap<>();
        Map<String, Integer> totalCountByProduct = new HashMap<>();
        for (PluginVersionCatalog.CatalogVersion item : versions) {
            totalCountByProduct.merge(item.productCode(), 1, Integer::sum);
            if (item.latest()) {
                latestCountByProduct.merge(item.productCode(), 1, Integer::sum);
            }
        }

        totalCountByProduct.forEach((product, totalCount) -> {
            if (totalCount <= 0) {
                throw new IllegalStateException("Catalog contains invalid product entries: " + product);
            }
            int latestCount = latestCountByProduct.getOrDefault(product, 0);
            if (latestCount != 1) {
                throw new IllegalStateException("Catalog must define exactly one latest version for product: " + product);
            }
        });

        for (String product : totalCountByProduct.keySet()) {
            repository.clearLatestForProduct(product);
        }

        for (PluginVersionCatalog.CatalogVersion item : versions) {
            upsert(item);
        }
    }

    private void upsert(PluginVersionCatalog.CatalogVersion item) {
        PluginVersionEntity entity = repository.findByProductCodeAndVersionName(item.productCode(), item.versionName())
                .orElseGet(PluginVersionEntity::new);

        entity.setProductCode(item.productCode());
        entity.setVersionName(item.versionName());
        entity.setReleaseUrl(item.releaseUrl());
        entity.setLatest(item.latest());
        repository.save(entity);
    }
}
