package com.monkey.ultimatebot.common.lib;

import com.monkey.ultimatebot.common.util.TextValues;
import java.util.Locale;

public enum LibraryTrack {
    LEGACY,
    MODERN;

    public String folderName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static LibraryTrack fromFolderName(String name) {
        if (TextValues.isBlank(name)) {
            throw new IllegalArgumentException("track folder name is blank");
        }
        return valueOf(name.trim().toUpperCase(Locale.ROOT));
    }
}
