package com.monkey.mcbot.license;

import java.time.Instant;

public record LicenseState(Instant lastSuccessfulValidationAt) {}
