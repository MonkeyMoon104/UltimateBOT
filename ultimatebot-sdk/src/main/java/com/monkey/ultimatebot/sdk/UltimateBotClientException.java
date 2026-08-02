package com.monkey.ultimatebot.sdk;

public class UltimateBotClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UltimateBotClientException(String message) {
        super(message);
    }

    public UltimateBotClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
