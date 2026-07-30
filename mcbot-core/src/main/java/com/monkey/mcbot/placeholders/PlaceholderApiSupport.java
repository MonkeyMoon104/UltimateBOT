package com.monkey.mcbot.placeholders;

import com.monkey.mcbot.MinecraftBot;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlaceholderApiSupport {

    private static final String PLACEHOLDER_API_PLUGIN = "PlaceholderAPI";
    private static final String PLACEHOLDER_API_CLASS = "me.clip.placeholderapi.PlaceholderAPI";
    private static final String PLACEHOLDER_COORDINATOR_CLASS =
            "com.monkey.mcbot.placeholders.BotPlaceholderCoordinator";

    private static volatile Method setPlaceholdersMethod;

    private PlaceholderApiSupport() {}

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin(PLACEHOLDER_API_PLUGIN) != null && hasPlaceholderApiClass();
    }

    public static PlaceholderRegistration createRegistration(MinecraftBot plugin) {
        if (!isAvailable()) {
            return null;
        }

        try {
            Class<?> coordinatorClass =
                    Class.forName(PLACEHOLDER_COORDINATOR_CLASS, true, PlaceholderApiSupport.class.getClassLoader());
            Object instance =
                    coordinatorClass.getConstructor(MinecraftBot.class).newInstance(plugin);
            if (instance instanceof PlaceholderRegistration registration) {
                return registration;
            }

            plugin.getLogger().warning("Placeholder coordinator does not implement PlaceholderRegistration");
        } catch (Throwable error) {
            plugin.getLogger().log(Level.WARNING, "Unable to initialize PlaceholderAPI integration", error);
        }

        return null;
    }

    public static String apply(Player owner, String input) {
        if (owner == null || input == null || input.isBlank() || !isAvailable()) {
            return input;
        }

        try {
            Method method = resolveSetPlaceholdersMethod();
            Object result = method.invoke(null, owner, input);
            return result instanceof String resolved ? resolved : input;
        } catch (Throwable ignored) {
            return input;
        }
    }

    private static boolean hasPlaceholderApiClass() {
        try {
            Class.forName(PLACEHOLDER_API_CLASS, false, PlaceholderApiSupport.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Method resolveSetPlaceholdersMethod() throws ReflectiveOperationException {
        Method cachedMethod = setPlaceholdersMethod;
        if (cachedMethod != null) {
            return cachedMethod;
        }

        Class<?> placeholderApiClass =
                Class.forName(PLACEHOLDER_API_CLASS, true, PlaceholderApiSupport.class.getClassLoader());
        Method resolvedMethod = placeholderApiClass.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
        setPlaceholdersMethod = resolvedMethod;
        return resolvedMethod;
    }
}
