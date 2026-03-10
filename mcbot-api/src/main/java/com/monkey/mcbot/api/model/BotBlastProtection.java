package com.monkey.mcbot.api.model;

public record BotBlastProtection(
        boolean feet,
        boolean legs,
        boolean chest,
        boolean head
) {
    public static BotBlastProtection all(boolean enabled) {
        return new BotBlastProtection(enabled, enabled, enabled, enabled);
    }

    public static BotBlastProtection of(boolean bootsBlastEnabled,
                                        boolean leggingsBlastEnabled,
                                        boolean chestplateBlastEnabled,
                                        boolean helmetBlastEnabled) {
        return new BotBlastProtection(bootsBlastEnabled, leggingsBlastEnabled, chestplateBlastEnabled, helmetBlastEnabled);
    }

    public static BotBlastProtection of(int bootsBlastEnabled,
                                        int leggingsBlastEnabled,
                                        int chestplateBlastEnabled,
                                        int helmetBlastEnabled) {
        return new BotBlastProtection(
                parseBinary(bootsBlastEnabled, "boots"),
                parseBinary(leggingsBlastEnabled, "leggings"),
                parseBinary(chestplateBlastEnabled, "chestplate"),
                parseBinary(helmetBlastEnabled, "helmet")
        );
    }

    public boolean allEnabled() {
        return feet && legs && chest && head;
    }

    private static boolean parseBinary(int value, String field) {
        if (value == 0) {
            return false;
        }
        if (value == 1) {
            return true;
        }
        throw new IllegalArgumentException(field + " blast value must be 0 or 1");
    }
}
