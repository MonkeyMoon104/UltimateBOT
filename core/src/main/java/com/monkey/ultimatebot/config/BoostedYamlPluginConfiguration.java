package com.monkey.ultimatebot.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Applies BoostedYAML auto-updates to {@code config.yml} before Bukkit reloads it. */
public final class BoostedYamlPluginConfiguration {

    public static final String VERSION_ROUTE = "config-version";

    private BoostedYamlPluginConfiguration() {}

    public static int updateAndSave(JavaPlugin plugin, Logger logger) throws IOException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        File configFile = new File(dataFolder, "config.yml");
        try (InputStream defaults = plugin.getResource("config.yml")) {
            if (defaults == null) {
                throw new IOException("Bundled config.yml is missing from the plugin jar");
            }
            YamlDocument document = YamlDocument.create(
                    configFile,
                    defaults,
                    GeneralSettings.builder().setRouteSeparator('.').build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning(VERSION_ROUTE)).build());
            document.update();
            document.save();
            YamlConfiguration loaded = YamlConfiguration.loadConfiguration(configFile);
            return loaded.getInt(VERSION_ROUTE, 0);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Failed to update config.yml with BoostedYAML", exception);
            throw exception;
        }
    }
}
