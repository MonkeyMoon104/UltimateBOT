package com.monkey.ultimatebot.license;

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
            String existing = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
            if (!existing.trim().isEmpty()) {
                return existing;
            }
        }

        String installationId = UUID.randomUUID().toString();
        Files.write(file, installationId.getBytes(StandardCharsets.UTF_8));
        return installationId;
    }
}
