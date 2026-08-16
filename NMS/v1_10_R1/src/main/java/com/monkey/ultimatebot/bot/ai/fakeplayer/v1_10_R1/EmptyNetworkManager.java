package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_10_R1;

import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.minecraft.server.v1_10_R1.EnumProtocolDirection;
import net.minecraft.server.v1_10_R1.NetworkManager;
import net.minecraft.server.v1_10_R1.Packet;
import org.jspecify.annotations.NullUnmarked;

/** Drop-all network manager so fake players can tick without a real client channel. */
@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(EnumProtocolDirection.CLIENTBOUND);
        this.channel = new EmbeddedChannel();
        this.l = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
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
