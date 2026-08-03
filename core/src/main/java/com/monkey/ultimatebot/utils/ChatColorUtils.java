package com.monkey.ultimatebot.utils;

import java.util.Objects;

public class ChatColorUtils {

    public static String translate(String input) {
        Objects.requireNonNull(input, "input");
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && isLegacyColorCode(chars[i + 1])) {
                chars[i] = '\u00A7';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }

        return new String(chars);
    }

    private static boolean isLegacyColorCode(char code) {
        return (code >= '0' && code <= '9')
                || (code >= 'a' && code <= 'f')
                || (code >= 'A' && code <= 'F')
                || (code >= 'k' && code <= 'o')
                || (code >= 'K' && code <= 'O')
                || code == 'r'
                || code == 'R'
                || code == 'x'
                || code == 'X';
    }
}
