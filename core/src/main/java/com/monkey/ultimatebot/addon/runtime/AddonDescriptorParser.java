package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.api.addon.AddonDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jspecify.annotations.Nullable;

final class AddonDescriptorParser {
    static final String DESCRIPTOR_PATH = "META-INF/ultimatebot-addon.properties";
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]{1,63}");

    private AddonDescriptorParser() {}

    static @Nullable AddonDescriptor parse(Path jar) throws AddonLoadException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry descriptorEntry = zip.getEntry(DESCRIPTOR_PATH);
            if (descriptorEntry == null) {
                return null;
            }
            Properties properties = new Properties();
            try (InputStream input = zip.getInputStream(descriptorEntry)) {
                properties.load(input);
            }
            String id = required(properties, "id").toLowerCase(Locale.ROOT);
            if (!ID_PATTERN.matcher(id).matches()) {
                throw new AddonLoadException("addon id must match " + ID_PATTERN.pattern());
            }
            return new AddonDescriptor(
                    id,
                    required(properties, "name"),
                    required(properties, "version"),
                    required(properties, "api-version"),
                    required(properties, "main"),
                    list(properties, "authors"),
                    list(properties, "dependencies"),
                    list(properties, "soft-dependencies"),
                    nativeProviders(properties));
        } catch (IOException | IllegalArgumentException error) {
            throw new AddonLoadException("cannot read " + jar.getFileName() + ": " + error.getMessage(), error);
        }
    }

    static void validateJarContents(Path jar) throws AddonLoadException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (ZipEntry entry : java.util.Collections.list(zip.entries())) {
                String name = entry.getName();
                if (name.startsWith("com/monkey/ultimatebot/")) {
                    throw new AddonLoadException(
                            jar.getFileName() + " bundles UltimateBot classes; use compileOnly instead");
                }
            }
        } catch (IOException error) {
            throw new AddonLoadException("cannot inspect " + jar.getFileName(), error);
        }
    }

    private static Map<String, String> nativeProviders(Properties properties) {
        Map<String, String> providers = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith("native.")) {
                continue;
            }
            String version = key.substring("native.".length()).trim();
            String implementation = properties.getProperty(key, "").trim();
            if (!version.isEmpty() && !implementation.isEmpty()) {
                providers.put(version, implementation);
            }
        }
        return providers;
    }

    private static List<String> list(Properties properties, String key) {
        String value = properties.getProperty(key, "");
        if (value.isBlank()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            String checked = tokenizer.nextToken().trim();
            if (!checked.isEmpty() && !values.contains(checked)) {
                values.add(checked);
            }
        }
        if ("dependencies".equals(key) || "soft-dependencies".equals(key)) {
            return values.stream().map(item -> item.toLowerCase(Locale.ROOT)).toList();
        }
        return List.copyOf(values);
    }

    private static String required(Properties properties, String key) throws AddonLoadException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new AddonLoadException("descriptor is missing " + key);
        }
        return value.trim();
    }
}
