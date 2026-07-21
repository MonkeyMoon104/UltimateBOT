package com.monkey.mcbot.sdk.model;

import java.util.UUID;

public record BotLocationRequest(
        String worldName,
        UUID worldUUID,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}
