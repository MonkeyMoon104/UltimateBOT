package com.monkey.ultimatebot.wrapper;

import com.monkey.ultimatebot.wrapper.bukkit.BukkitWrapper;
import com.monkey.ultimatebot.wrapper.folia.FoliaWrapper;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

public final class WrapperManager {

    private final Map<WrapperType, PlatformWrapper> wrappers;
    private final PlatformWrapper activeWrapper;

    public WrapperManager(JavaPlugin plugin) {
        BukkitWrapper bukkitWrapper = new BukkitWrapper(plugin);
        FoliaWrapper foliaWrapper = new FoliaWrapper(plugin);

        EnumMap<WrapperType, PlatformWrapper> resolvedWrappers = new EnumMap<>(WrapperType.class);
        resolvedWrappers.put(WrapperType.BUKKIT, bukkitWrapper);
        resolvedWrappers.put(WrapperType.FOLIA, foliaWrapper);

        this.wrappers = Collections.unmodifiableMap(resolvedWrappers);
        this.activeWrapper = foliaWrapper.capabilities().foliaDetected() ? foliaWrapper : bukkitWrapper;
    }

    public PlatformWrapper active() {
        return activeWrapper;
    }

    public @Nullable PlatformWrapper get(WrapperType wrapperType) {
        return wrappers.get(wrapperType);
    }

    public WrapperCapabilities capabilities() {
        return activeWrapper.capabilities();
    }

    public String describeActiveWrapper() {
        return activeWrapper.type().name() + " [" + capabilities().summary() + "]";
    }
}
