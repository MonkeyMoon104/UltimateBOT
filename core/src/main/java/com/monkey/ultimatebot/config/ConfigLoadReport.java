package com.monkey.ultimatebot.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigLoadReport {

    private int keysRead;
    private final List<InvalidKey> invalidKeys = new ArrayList<>();

    public void incrementKeysRead() {
        keysRead++;
    }

    public void addInvalidKey(String path, Object invalidValue, Object defaultValue) {
        invalidKeys.add(new InvalidKey(path, String.valueOf(invalidValue), String.valueOf(defaultValue)));
    }

    public int keysRead() {
        return keysRead;
    }

    public int invalidCount() {
        return invalidKeys.size();
    }

    public List<InvalidKey> invalidKeys() {
        return Collections.unmodifiableList(invalidKeys);
    }

    public static final class InvalidKey {
        private final String path;
        private final String invalidValue;
        private final String defaultValue;

        InvalidKey(String path, String invalidValue, String defaultValue) {
            this.path = path;
            this.invalidValue = invalidValue;
            this.defaultValue = defaultValue;
        }

        public String path() { return path; }
        public String invalidValue() { return invalidValue; }
        public String defaultValue() { return defaultValue; }
    }
}
