package com.monkey.ultimatebot.compat;

/**
 * JVM major version for dual-path libraries (InvUI / Caffeine 3 need Java 11+).
 */
public final class JavaRuntimeAccess {

    private static final int MAJOR = detectMajor();

    private JavaRuntimeAccess() {}

    public static int major() {
        return MAJOR;
    }

    public static boolean isAtLeast(int major) {
        return MAJOR >= major;
    }

    private static int detectMajor() {
        String version = System.getProperty("java.specification.version", "8");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2));
        }
        int dot = version.indexOf('.');
        return Integer.parseInt(dot < 0 ? version : version.substring(0, dot));
    }
}
