package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R1;

import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.minecraft.server.v1_16_R1.EnumProtocolDirection;
import net.minecraft.server.v1_16_R1.NetworkManager;
import net.minecraft.server.v1_16_R1.Packet;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(EnumProtocolDirection.CLIENTBOUND);
        this.channel = new EmbeddedChannel();
        this.socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
    }

    @Override
    public void sendPacket(Packet<?> packet) {}

    @Override
    public boolean isConnected() {
        return true;
    }
}
