package com.monkey.mcbot.licenseserver.version;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PluginVersionCatalog {

    /*
     * Manual catalog:
     * - add every released version here
     * - keep exactly one entry with latest=true for each product
     */
    public List<CatalogVersion> versions() {
        String url = "https://builtbybit.com/resources/minecraftbot-pvp-practice-bots.100308/";
        return List.of(
                new CatalogVersion("minecraftbot", "1.0.0", url, false),
                new CatalogVersion("minecraftbot", "1.0.1", url, false),
                new CatalogVersion("minecraftbot", "1.0.2", url, false),
                new CatalogVersion("minecraftbot", "1.0.3", url, false),
                new CatalogVersion("minecraftbot", "1.0.4", url, true)
        );
    }

    public record CatalogVersion(
            String productCode,
            String versionName,
            String releaseUrl,
            boolean latest
    ) {
    }
}
