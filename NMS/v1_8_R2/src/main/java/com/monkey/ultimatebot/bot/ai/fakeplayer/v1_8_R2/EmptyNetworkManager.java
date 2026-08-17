package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_8_R2;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.minecraft.server.v1_8_R2.EnumProtocolDirection;
import net.minecraft.server.v1_8_R2.NetworkManager;
import net.minecraft.server.v1_8_R2.Packet;
import org.jspecify.annotations.NullUnmarked;

/** Drop-all network manager so fake players can tick without a real client channel. */
@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(EnumProtocolDirection.CLIENTBOUND);
        // Spigot 1.8 Netty rejects EmbeddedChannel() with no handlers.
        this.k = new EmbeddedChannel(new ChannelHandlerAdapter() {});
        this.l = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void handle(Packet packet) {
        // Intentionally empty — bots never flush to a real connection.
    }

    @Override
    public boolean g() {
        return true;
    }
}
