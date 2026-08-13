package com.monkey.ultimatebot.protocol;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.monkey.ultimatebot.common.util.ImmutableCollections;
import org.jspecify.annotations.Nullable;

/** Version-neutral profile data used by the packet rendering layer. */
public final class BotProfileData {
    private final UUID id;
    private final String name;
    private final List<Texture> textures;

    public BotProfileData(UUID id, String name, List<Texture> textures) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.textures = ImmutableCollections.copyOf(Objects.requireNonNull(textures, "textures"));
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<Texture> textures() {
        return textures;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotProfileData)) {
            return false;
        }
        BotProfileData other = (BotProfileData) obj;
        return id.equals(other.id) && name.equals(other.name) && textures.equals(other.textures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, textures);
    }

    @Override
    public String toString() {
        return "BotProfileData[id=" + id + ", name=" + name + ", textures=" + textures + "]";
    }

    /** A signed or unsigned skin texture property. */
    public static final class Texture {
        private final String name;
        private final String value;
        private final @Nullable String signature;

        public Texture(String name, String value, @Nullable String signature) {
            this.name = Objects.requireNonNull(name, "name");
            this.value = Objects.requireNonNull(value, "value");
            this.signature = signature;
        }

        public String name() {
            return name;
        }

        public String value() {
            return value;
        }

        public @Nullable String signature() {
            return signature;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Texture)) {
                return false;
            }
            Texture other = (Texture) obj;
            return name.equals(other.name)
                    && value.equals(other.value)
                    && Objects.equals(signature, other.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value, signature);
        }

        @Override
        public String toString() {
            return "Texture[name=" + name + ", value=" + value + ", signature=" + signature + "]";
        }
    }
}
