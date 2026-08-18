package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_7_R4;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelHandlerAdapter;
import net.minecraft.util.io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public final class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager() {
        super(false);
        EmbeddedChannel embedded = new EmbeddedChannel(new ChannelHandlerAdapter() {});
        InetSocketAddress loopback = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        try {
            boolean channelSet = setFirstAssignableField(NetworkManager.class, this, Channel.class, embedded);
            boolean addressSet = setFirstAssignableField(NetworkManager.class, this, SocketAddress.class, loopback);

            if (!channelSet) {
                setByNameIfPresent(NetworkManager.class, this, "m", embedded);
            }
            if (!addressSet) {
                setByNameIfPresent(NetworkManager.class, this, "n", loopback);
            }
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void handle(Packet packet, GenericFutureListener... listeners) {}

    @Override
    public boolean isConnected() {
        return true;
    }

    private static boolean setFirstAssignableField(
            Class<?> ownerClass, Object target, Class<?> expectedType, Object value) {
        for (Field field : ownerClass.getDeclaredFields()) {
            if (!expectedType.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                field.set(target, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return false;
    }

    private static void setByNameIfPresent(Class<?> ownerClass, Object target, String fieldName, Object value) {
        try {
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
