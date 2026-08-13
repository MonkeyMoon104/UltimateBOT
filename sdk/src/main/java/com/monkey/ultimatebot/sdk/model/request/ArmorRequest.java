package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.BotArmorTier;
import java.util.Objects;

/** Request body for changing all standard bot armor pieces to one tier. */
public final class ArmorRequest {
    private final BotArmorTier armor;

    public ArmorRequest(BotArmorTier armor) {


        Objects.requireNonNull(armor, "armor");
        this.armor = armor;
    }

    public BotArmorTier armor() {
        return armor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ArmorRequest)) {
            return false;
        }
        ArmorRequest other = (ArmorRequest) obj;
        return java.util.Objects.equals(armor, other.armor);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(armor);
    }

    @Override
    public String toString() {
        return "ArmorRequest[armor=" + armor + "]";
    }
}
