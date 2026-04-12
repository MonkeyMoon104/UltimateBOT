package com.monkey.mcbot.licenseserver.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class LicenseKeyGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int BLOCKS = 4;
    private static final int BLOCK_SIZE = 4;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder builder = new StringBuilder(BLOCKS * BLOCK_SIZE + (BLOCKS - 1));
        for (int block = 0; block < BLOCKS; block++) {
            if (block > 0) {
                builder.append('-');
            }
            for (int i = 0; i < BLOCK_SIZE; i++) {
                builder.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
            }
        }
        return builder.toString();
    }
}
