package com.monkey.ultimatebot.addon.runtime;

import java.net.URL;
import java.net.URLClassLoader;

final class AddonClassLoader extends URLClassLoader {
    private static final String[] PARENT_FIRST = {
        "java.",
        "javax.",
        "jdk.",
        "org.bukkit.",
        "io.papermc.",
        "net.kyori.",
        "org.jspecify.",
        "net.minecraft.",
        "com.mojang.",
        "com.monkey.ultimatebot."
    };

    AddonClassLoader(URL jar, ClassLoader parent) {
        super(new URL[] {jar}, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                loaded = isParentFirst(name) ? loadParentThenLocal(name) : loadLocalThenParent(name);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    private Class<?> loadParentThenLocal(String name) throws ClassNotFoundException {
        try {
            return getParent().loadClass(name);
        } catch (ClassNotFoundException ignored) {
            return findClass(name);
        }
    }

    private Class<?> loadLocalThenParent(String name) throws ClassNotFoundException {
        try {
            return findClass(name);
        } catch (ClassNotFoundException ignored) {
            return getParent().loadClass(name);
        }
    }

    private static boolean isParentFirst(String name) {
        for (String prefix : PARENT_FIRST) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
