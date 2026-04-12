package com.monkey.mcbot.licenseserver.service;

import com.monkey.mcbot.licenseserver.config.LicenseServerProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class LicenseHmacService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final SecretKeySpec secretKeySpec;

    public LicenseHmacService(LicenseServerProperties properties) {
        this.secretKeySpec = new SecretKeySpec(properties.hmacSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String normalize(String licenseKey) {
        return licenseKey == null ? "" : licenseKey.trim().toUpperCase(Locale.ROOT);
    }

    public String hmac(String licenseKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(normalize(licenseKey).getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute license_key_hmac", ex);
        }
    }

    public String prefix(String licenseKey) {
        String normalized = normalize(licenseKey);
        return normalized.length() >= 4 ? normalized.substring(0, 4) : normalized;
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.ROOT, "%02x", value));
        }
        return builder.toString();
    }
}
