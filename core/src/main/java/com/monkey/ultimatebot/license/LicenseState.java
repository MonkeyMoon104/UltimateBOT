package com.monkey.ultimatebot.license;

import java.time.Instant;

public record LicenseState(Instant lastSuccessfulValidationAt) {}
