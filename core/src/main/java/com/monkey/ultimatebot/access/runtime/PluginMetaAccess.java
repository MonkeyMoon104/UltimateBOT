package com.monkey.ultimatebot.access.runtime;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jspecify.annotations.Nullable;

public final class PluginMetaAccess {

    private static final @Nullable Method GET_PLUGIN_META = resolveGetPluginMeta();
    private static final @Nullable Method META_GET_NAME = resolveMetaMethod("getName");
    private static final @Nullable Method META_GET_VERSION = resolveMetaMethod("getVersion");
    private static final @Nullable Method META_GET_AUTHORS = resolveMetaMethod("getAuthors");

    private PluginMetaAccess() {}

    public static String name(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        Object meta = pluginMeta(plugin);
        if (meta != null && META_GET_NAME != null) {
            Object value = invoke(META_GET_NAME, meta);
            if (value instanceof String) {
                String name = (String) value;
                if (!name.trim().isEmpty()) {
                    return name;
                }
            }
        }
        return description(plugin).getName();
    }

    public static String version(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        Object meta = pluginMeta(plugin);
        if (meta != null && META_GET_VERSION != null) {
            Object value = invoke(META_GET_VERSION, meta);
            if (value instanceof String) {
                String version = (String) value;
                return version;
            }
        }
        return description(plugin).getVersion();
    }

    @SuppressWarnings("unchecked")
    public static List<String> authors(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        Object meta = pluginMeta(plugin);
        if (meta != null && META_GET_AUTHORS != null) {
            Object value = invoke(META_GET_AUTHORS, meta);
            if (value instanceof List<?>) {
                List<?> authors = (List<?>) value;
                return (List<String>) authors;
            }
        }
        return description(plugin).getAuthors();
    }

    private static @Nullable Object pluginMeta(Plugin plugin) {
        if (GET_PLUGIN_META == null) {
            return null;
        }
        return invoke(GET_PLUGIN_META, plugin);
    }

    @SuppressWarnings("deprecation")
    private static PluginDescriptionFile description(Plugin plugin) {
        return plugin.getDescription();
    }

    private static @Nullable Method resolveGetPluginMeta() {
        try {
            return Plugin.class.getMethod("getPluginMeta");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveMetaMethod(String name) {
        if (GET_PLUGIN_META == null) {
            return null;
        }
        Class<?> metaType = GET_PLUGIN_META.getReturnType();
        try {
            return metaType.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Object invoke(Method method, Object target) {
        try {
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
