package com.monkey.ultimatebot.lang;

import com.monkey.ultimatebot.UltimateBot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LanguageManager {

    private static final String LANGS_DIRECTORY = "langs";
    private static final String DEFAULT_LANG_FILE = "EN.yml";
    private static final String LANG_FILE_CONFIG_KEY = "lang-file";
    private static final int MAX_MISSING_KEYS_TO_LOG = 20;

    private final UltimateBot plugin;
    private final File langsFolder;

    private FileConfiguration defaultLanguageConfiguration = new YamlConfiguration();
    private FileConfiguration activeLanguageConfiguration = new YamlConfiguration();
    private String activeLanguageFileName = DEFAULT_LANG_FILE;
    private boolean fallbackToDefault = true;
    private Set<String> knownLanguageFiles = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public LanguageManager(UltimateBot plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.langsFolder = new File(plugin.getDataFolder(), LANGS_DIRECTORY);
    }

    public synchronized void reload() {
        ensureLangsFolderExists();
        Set<String> filesBeforeReload = listLanguageFilesOnDisk();
        copyBundledLanguageFilesIfMissing();
        Set<String> filesAfterReload = listLanguageFilesOnDisk();
        logDetectedNewLanguageFiles(filesBeforeReload, filesAfterReload);
        knownLanguageFiles = filesAfterReload;

        defaultLanguageConfiguration = loadLanguageFromDisk(DEFAULT_LANG_FILE);

        String configuredLanguage =
                normalizeLanguageFileName(plugin.getConfig().getString(LANG_FILE_CONFIG_KEY, DEFAULT_LANG_FILE));

        LoadedLanguage loaded = loadConfiguredLanguage(configuredLanguage);
        activeLanguageConfiguration = loaded.configuration;
        activeLanguageFileName = loaded.fileName;
        fallbackToDefault = loaded.fallback;

        if (fallbackToDefault && !DEFAULT_LANG_FILE.equalsIgnoreCase(configuredLanguage)) {
            plugin.getLogger()
                    .warning("Using fallback language '" + DEFAULT_LANG_FILE + "' because '" + configuredLanguage
                            + "' is invalid.");
        }

        plugin.getLogger()
                .info("Language loaded: " + activeLanguageFileName
                        + (fallbackToDefault ? " (fallback default)" : "")
                        + ". Available files: " + String.join(", ", knownLanguageFiles));
    }

    public synchronized String getString(String path) {
        return getString(path, path);
    }

    public synchronized String getString(String path, String fallbackValue) {
        String value = activeLanguageConfiguration.getString(path);
        if (value != null) {
            return value;
        }

        String defaultValue = defaultLanguageConfiguration.getString(path);
        if (defaultValue != null) {
            return defaultValue;
        }

        return fallbackValue == null ? path : fallbackValue;
    }

    public synchronized List<String> getStringList(String path) {
        if (activeLanguageConfiguration.contains(path)) {
            return activeLanguageConfiguration.getStringList(path);
        }

        if (defaultLanguageConfiguration.contains(path)) {
            return defaultLanguageConfiguration.getStringList(path);
        }

        return Collections.emptyList();
    }

    public synchronized FileConfiguration getActiveLanguageConfiguration() {
        return activeLanguageConfiguration;
    }

    public synchronized String getActiveLanguageFileName() {
        return activeLanguageFileName;
    }

    public synchronized boolean isFallbackToDefault() {
        return fallbackToDefault;
    }

    private LoadedLanguage loadConfiguredLanguage(String configuredFileName) {
        if (configuredFileName.equalsIgnoreCase(DEFAULT_LANG_FILE)) {
            return new LoadedLanguage(defaultLanguageConfiguration, DEFAULT_LANG_FILE, false);
        }

        File configuredFile = new File(langsFolder, configuredFileName);
        if (!configuredFile.exists() || !configuredFile.isFile()) {
            plugin.getLogger().severe("Language file not found: " + configuredFile.getAbsolutePath());
            return new LoadedLanguage(defaultLanguageConfiguration, DEFAULT_LANG_FILE, true);
        }

        FileConfiguration configuredLanguage = loadLanguageFromDisk(configuredFileName);
        Set<String> missingKeys = findMissingKeys(defaultLanguageConfiguration, configuredLanguage);
        if (!missingKeys.isEmpty()) {
            logMissingKeys(configuredFileName, missingKeys);
            return new LoadedLanguage(defaultLanguageConfiguration, DEFAULT_LANG_FILE, true);
        }

        return new LoadedLanguage(configuredLanguage, configuredFileName, false);
    }

    private FileConfiguration loadLanguageFromDisk(String fileName) {
        File targetFile = new File(langsFolder, fileName);
        if (!targetFile.exists() || !targetFile.isFile()) {
            plugin.getLogger().severe("Language file missing on disk: " + targetFile.getAbsolutePath());
            return new YamlConfiguration();
        }

        return YamlConfiguration.loadConfiguration(targetFile);
    }

    private void ensureLangsFolderExists() {
        if (!langsFolder.exists() && !langsFolder.mkdirs()) {
            plugin.getLogger().severe("Could not create language folder: " + langsFolder.getAbsolutePath());
        }
    }

    private void copyBundledLanguageFilesIfMissing() {
        copyBundledLangsFromPluginJar();

        File defaultLang = new File(langsFolder, DEFAULT_LANG_FILE);
        if (!defaultLang.exists()) {
            copyDefaultLanguageFallback();
        }
    }

    private boolean copyBundledLangsFromPluginJar() {
        CodeSource codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            return false;
        }

        final Path pluginPath;
        try {
            pluginPath = Path.of(codeSource.getLocation().toURI());
        } catch (URISyntaxException | IllegalArgumentException ex) {
            plugin.getLogger().log(Level.WARNING, "Cannot resolve plugin jar path for language extraction.", ex);
            return false;
        }

        if (!Files.isRegularFile(pluginPath)) {
            return false;
        }

        boolean copiedAny = false;

        try (JarFile jarFile = new JarFile(pluginPath.toFile())) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String entryName = entry.getName();
                if (!entryName.startsWith(LANGS_DIRECTORY + "/") || !entryName.endsWith(".yml")) {
                    continue;
                }

                String languageFileName = entryName.substring((LANGS_DIRECTORY + "/").length());
                if (languageFileName.isBlank() || languageFileName.contains("/") || languageFileName.contains("\\")) {
                    continue;
                }

                File target = new File(langsFolder, languageFileName);
                if (target.exists()) {
                    continue;
                }

                try (InputStream input = jarFile.getInputStream(entry)) {
                    Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copiedAny = true;
                    plugin.getLogger().info("Installed bundled language file: " + languageFileName);
                } catch (IOException copyException) {
                    plugin.getLogger()
                            .log(
                                    Level.WARNING,
                                    "Failed to copy bundled language '" + languageFileName + "' to disk.",
                                    copyException);
                }
            }
        } catch (IOException ioException) {
            plugin.getLogger().log(Level.WARNING, "Failed to scan plugin jar for bundled language files.", ioException);
        }

        return copiedAny;
    }

    private void copyDefaultLanguageFallback() {
        File defaultTarget = new File(langsFolder, DEFAULT_LANG_FILE);
        if (defaultTarget.exists()) {
            return;
        }

        try {
            plugin.saveResource(LANGS_DIRECTORY + "/" + DEFAULT_LANG_FILE, false);
            plugin.getLogger().info("Installed default language file: " + DEFAULT_LANG_FILE);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger()
                    .log(
                            Level.SEVERE,
                            "Default language resource '" + LANGS_DIRECTORY + "/" + DEFAULT_LANG_FILE
                                    + "' is missing from jar.",
                            ex);
        }
    }

    private Set<String> listLanguageFilesOnDisk() {
        Set<String> files = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        File[] entries = langsFolder.listFiles();
        if (entries == null) {
            return files;
        }

        for (File entry : entries) {
            if (!entry.isFile()) {
                continue;
            }

            String name = entry.getName();
            if (name.toLowerCase(Locale.ROOT).endsWith(".yml")) {
                files.add(name);
            }
        }

        return files;
    }

    private void logDetectedNewLanguageFiles(Set<String> filesBeforeReload, Set<String> filesAfterReload) {
        Set<String> previousKnown = knownLanguageFiles.isEmpty() ? filesBeforeReload : knownLanguageFiles;
        for (String fileName : filesAfterReload) {
            if (!previousKnown.contains(fileName)) {
                plugin.getLogger().info("Detected new language file: " + fileName);
            }
        }
    }

    private void logMissingKeys(String configuredFileName, Set<String> missingKeys) {
        plugin.getLogger()
                .severe("Language file '" + configuredFileName + "' is missing " + missingKeys.size()
                        + " required key(s). Falling back to " + DEFAULT_LANG_FILE + ".");

        int index = 0;
        for (String missingKey : missingKeys) {
            if (index >= MAX_MISSING_KEYS_TO_LOG) {
                plugin.getLogger()
                        .severe("... and " + (missingKeys.size() - MAX_MISSING_KEYS_TO_LOG) + " more key(s).");
                break;
            }
            plugin.getLogger().severe("Missing key: " + missingKey);
            index++;
        }
    }

    private Set<String> findMissingKeys(FileConfiguration requiredConfig, FileConfiguration candidateConfig) {
        Set<String> missing = new LinkedHashSet<>();

        for (String key : requiredConfig.getKeys(true)) {
            Object value = requiredConfig.get(key);
            if (value instanceof ConfigurationSection) {
                continue;
            }

            if (!candidateConfig.contains(key)) {
                missing.add(key);
            }
        }

        return missing;
    }

    private String normalizeLanguageFileName(String configured) {
        if (configured == null || configured.isBlank()) {
            return DEFAULT_LANG_FILE;
        }

        String trimmed = configured.trim().replace('\\', '/');
        int slashIndex = trimmed.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < trimmed.length() - 1) {
            trimmed = trimmed.substring(slashIndex + 1);
        }

        if (!trimmed.toLowerCase(Locale.ROOT).endsWith(".yml")) {
            trimmed = trimmed + ".yml";
        }

        return trimmed;
    }

    private static final class LoadedLanguage {
        private final FileConfiguration configuration;
        private final String fileName;
        private final boolean fallback;

        private LoadedLanguage(FileConfiguration configuration, String fileName, boolean fallback) {
            this.configuration = configuration;
            this.fileName = fileName;
            this.fallback = fallback;
        }
    }
}
