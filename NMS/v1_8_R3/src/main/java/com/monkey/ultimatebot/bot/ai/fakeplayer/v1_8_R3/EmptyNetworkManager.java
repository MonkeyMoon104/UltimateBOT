package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_8_R3;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(EnumProtocolDirection.CLIENTBOUND);

        this.channel = new EmbeddedChannel(new ChannelHandlerAdapter() {});
        this.l = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void handle(Packet packet) {}

    @Override
    public boolean g() {
        return true;
    }
}
