package com.monkey.ultimatebot.addon.runtime;

import java.util.stream.Collectors;

import com.monkey.ultimatebot.api.addon.AddonRegistry;
import com.monkey.ultimatebot.api.addon.AddonSnapshot;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CoreAddonRegistry implements AddonRegistry {
    private final ConcurrentMap<String, AddonSnapshot> addons = new ConcurrentHashMap<>();

    @Override
    public List<AddonSnapshot> addons() {
        return addons.values().stream()
                .sorted((left, right) ->
                        left.descriptor().id().compareTo(right.descriptor().id()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AddonSnapshot> addon(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(addons.get(id.trim().toLowerCase(Locale.ROOT)));
    }

    void update(AddonSnapshot snapshot) {
        addons.put(snapshot.descriptor().id().toLowerCase(Locale.ROOT), snapshot);
    }

    public void clear() {
        addons.clear();
    }
}
