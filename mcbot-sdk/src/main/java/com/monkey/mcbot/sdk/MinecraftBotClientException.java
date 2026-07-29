package com.monkey.mcbot.sdk;

public class MinecraftBotClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MinecraftBotClientException(String message) {
        super(message);
    }

    public MinecraftBotClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
