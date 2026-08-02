package com.monkey.ultimatebot.protocol;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Version-neutral profile data used by the packet rendering layer. */
public record BotProfileData(UUID id, String name, List<Texture> textures) {

    public BotProfileData {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        textures = List.copyOf(Objects.requireNonNull(textures, "textures"));
    }

    /** A signed or unsigned skin texture property. */
    public record Texture(
            String name, String value, @Nullable String signature) {

        public Texture {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(value, "value");
        }
    }
}
