package com.monkey.ultimatebot.compat;

import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * Player look-direction updates across Paper versions.
 *
 * <p>Paper 1.16.x {@code CraftPlayer#setRotation} throws {@link UnsupportedOperationException}
 * ("Consider teleporting instead"). 1.17+ implements it (and {@code BotCraftPlayer} broadcasts look
 * packets). Dual-path: prefer {@code setRotation}; on older servers set NMS yaw/pitch and broadcast
 * look packets via the bridge — never teleport, which cancels velocity and makes follow stutter.
 */
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
