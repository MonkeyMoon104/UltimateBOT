package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_8_R1;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.minecraft.server.v1_8_R1.EnumProtocolDirection;
import net.minecraft.server.v1_8_R1.NetworkManager;
import net.minecraft.server.v1_8_R1.Packet;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(EnumProtocolDirection.CLIENTBOUND);
        try {
            java.lang.reflect.Field channelField = NetworkManager.class.getDeclaredField("i");
            channelField.setAccessible(true);

            channelField.set(this, new EmbeddedChannel(new ChannelHandlerAdapter() {}));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to init empty NetworkManager channel", e);
        }
        this.j = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void handle(Packet packet) {}

    @Override
    public boolean g() {
        return true;
    }
}
