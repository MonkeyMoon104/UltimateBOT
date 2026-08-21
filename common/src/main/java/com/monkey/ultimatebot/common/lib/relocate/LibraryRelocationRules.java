package com.monkey.ultimatebot.common.lib.relocate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LibraryRelocationRules {
    public static final String RULES_VERSION = "1";
    public static final String ROOT = "com.monkey.ultimatebot.libs";

    private static final Map<String, String> RULES = buildRules();

    private LibraryRelocationRules() {}

    public static Map<String, String> rules() {
        return RULES;
    }

    public static String relocateClassName(String originalClassName) {
        if (originalClassName == null || originalClassName.isEmpty()) {
            return originalClassName;
        }
        String bestFrom = null;
        String bestTo = null;
        for (Map.Entry<String, String> entry : RULES.entrySet()) {
            String from = entry.getKey();
            if (originalClassName.equals(from) || originalClassName.startsWith(from + ".")) {
                if (bestFrom == null || from.length() > bestFrom.length()) {
                    bestFrom = from;
                    bestTo = entry.getValue();
                }
            }
        }
        if (bestFrom == null) {
            return originalClassName;
        }
        return bestTo + originalClassName.substring(bestFrom.length());
    }

    private static Map<String, String> buildRules() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("com.fasterxml.jackson", ROOT + ".jackson");
        map.put("revxrsal.commands", ROOT + ".lamp");
        map.put("org.spongepowered.configurate", ROOT + ".configurate");
        map.put("io.leangen.geantyref", ROOT + ".geantyref");
        map.put("net.kyori.option", ROOT + ".option");
        map.put("de.bsommerfeld.pathetic", ROOT + ".pathetic");
        map.put("org.bstats", ROOT + ".bstats");
        map.put("com.github.benmanes.caffeine", ROOT + ".caffeine");
        map.put("xyz.xenondevs.invui", ROOT + ".invui");
        map.put("xyz.xenondevs.inventoryaccess", ROOT + ".inventoryaccess");
        map.put("org.jetbrains.annotations", ROOT + ".jetbrains.annotations");
        map.put("org.checkerframework", ROOT + ".checkerframework");
        map.put("com.google.errorprone.annotations", ROOT + ".errorprone.annotations");
        return Collections.unmodifiableMap(map);
    }
}
