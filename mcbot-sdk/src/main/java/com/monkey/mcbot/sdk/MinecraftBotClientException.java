package com.monkey.mcbot.sdk;

public class MinecraftBotClientException extends RuntimeException {

    public MinecraftBotClientException(String message) {
        super(message);
    }

    public MinecraftBotClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
