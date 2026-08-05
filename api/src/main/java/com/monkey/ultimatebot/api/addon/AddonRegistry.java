package com.monkey.ultimatebot.api.addon;

import java.util.List;
import java.util.Optional;

/** Read-only view of addons discovered by UltimateBot. */
public interface AddonRegistry {
    List<AddonSnapshot> addons();

    Optional<AddonSnapshot> addon(String id);
}
