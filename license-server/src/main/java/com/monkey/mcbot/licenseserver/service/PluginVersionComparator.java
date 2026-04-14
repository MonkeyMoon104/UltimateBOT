package com.monkey.mcbot.licenseserver.service;

import org.springframework.stereotype.Component;

@Component
public class PluginVersionComparator {

    public int compare(String a, String b) {
        String[] left = tokenize(a);
        String[] right = tokenize(b);
        int max = Math.max(left.length, right.length);

        for (int i = 0; i < max; i++) {
            String l = i < left.length ? left[i] : "0";
            String r = i < right.length ? right[i] : "0";

            Integer li = asInteger(l);
            Integer ri = asInteger(r);
            int delta;
            if (li != null && ri != null) {
                delta = Integer.compare(li, ri);
            } else {
                delta = l.compareToIgnoreCase(r);
            }

            if (delta != 0) {
                return delta;
            }
        }
        return 0;
    }

    private String[] tokenize(String value) {
        if (value == null || value.isBlank()) {
            return new String[]{"0"};
        }
        return value.trim().split("[^A-Za-z0-9]+");
    }

    private Integer asInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
