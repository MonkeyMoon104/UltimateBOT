package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.BotArmorTier;
import java.util.Objects;

/** Request body for changing all standard bot armor pieces to one tier. */
public record ArmorRequest(BotArmorTier armor) {
    public ArmorRequest {
        Objects.requireNonNull(armor, "armor");
    }
}
