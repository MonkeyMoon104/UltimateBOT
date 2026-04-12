package com.monkey.mcbot.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class InstallationIdStore {

    private static final String FILE_NAME = "license-installation.id";

    public String loadOrCreate(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        Path file = dataDirectory.resolve(FILE_NAME);

        if (Files.exists(file)) {
            String existing = Files.readString(file, StandardCharsets.UTF_8).trim();
            if (!existing.isBlank()) {
                return existing;
            }
        }

        String installationId = UUID.randomUUID().toString();
        Files.writeString(file, installationId, StandardCharsets.UTF_8);
        return installationId;
    }
}
