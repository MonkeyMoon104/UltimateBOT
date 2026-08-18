package com.monkey.ultimatebot.access.player;

import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import org.bukkit.entity.Player;

public final class PlayerRotationAccess {

    private static final boolean SET_ROTATION_WORKS = MinecraftVersionAccess.isAtLeast(1, 17);

    private PlayerRotationAccess() {}

    public static void set(Player player, float yaw, float pitch) {
        Objects.requireNonNull(player, "player");
        if (SET_ROTATION_WORKS) {
            player.setRotation(yaw, pitch);
            return;
        }
        NMSBridgeManager.get().setBotRotation(player, yaw, pitch);
    }
}
