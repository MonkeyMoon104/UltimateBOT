package com.monkey.ultimatebot.common.model;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Platform-neutral installed-addon status exposed by the remote API and SDK. */
public record AddonInfo(
        String id,
        String name,
        String version,
        String state,
        List<String> authors,
        List<String> dependencies,
        @Nullable String failure) {
    public AddonInfo {
        id = requireText(id, "id");
        name = requireText(name, "name");
        version = requireText(version, "version");
        state = requireText(state, "state");
        authors = List.copyOf(Objects.requireNonNull(authors, "authors"));
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
