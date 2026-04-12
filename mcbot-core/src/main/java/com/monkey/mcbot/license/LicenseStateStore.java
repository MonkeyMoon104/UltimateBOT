package com.monkey.mcbot.license;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class LicenseStateStore {

    private static final String FILE_NAME = "license-state.json";
    private final ObjectMapper objectMapper;

    public LicenseStateStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<LicenseState> load(Path dataDirectory) {
        try {
            Path file = dataDirectory.resolve(FILE_NAME);
            if (!Files.exists(file)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(Files.readString(file, StandardCharsets.UTF_8), LicenseState.class));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public void saveSuccess(Path dataDirectory, Instant timestamp) throws IOException {
        Files.createDirectories(dataDirectory);
        Path file = dataDirectory.resolve(FILE_NAME);
        Files.writeString(
                file,
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new LicenseState(timestamp)),
                StandardCharsets.UTF_8
        );
    }

    public boolean hasValidGrace(Path dataDirectory, Duration gracePeriod, Instant now) {
        return load(dataDirectory)
                .map(LicenseState::lastSuccessfulValidationAt)
                .filter(lastSuccess -> lastSuccess.plus(gracePeriod).isAfter(now))
                .isPresent();
    }
}
