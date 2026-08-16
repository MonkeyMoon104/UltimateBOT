package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_13_R2;

import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.NetworkManager;
import net.minecraft.server.v1_13_R2.Packet;
import org.jspecify.annotations.NullUnmarked;

/**
 * Drop-all network manager so fake players can tick without a real client channel.
 *
 * <p>Paper 1.13.1 still uses the obfuscated remote-address field {@code l}; Spigot/Paper 1.13.2
 * map it to {@code socketAddress}. Same Craft {@code v1_13_R2} — dual-path via reflection, not a
 * new NMS package.
 */
@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(EnumProtocolDirection.CLIENTBOUND);
        this.channel = new EmbeddedChannel();
        setRemoteAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
    }

    private void setRemoteAddress(SocketAddress address) {
        try {
            Method spoof = NetworkManager.class.getMethod("setSpoofedRemoteAddress", SocketAddress.class);
            spoof.invoke(this, address);
            return;
        } catch (ReflectiveOperationException ignored) {
            // Spigot jar has no Paper spoof helper — fall through to fields.
        }
        String[] names = {"socketAddress", "l"};
        for (int i = 0; i < names.length; i++) {
            try {
                Field field = NetworkManager.class.getDeclaredField(names[i]);
                field.setAccessible(true);
                field.set(this, address);
                return;
            } catch (ReflectiveOperationException ignored) {
                // try next mapping
            }
        }
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        // Intentionally empty — bots never flush to a real connection.
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
