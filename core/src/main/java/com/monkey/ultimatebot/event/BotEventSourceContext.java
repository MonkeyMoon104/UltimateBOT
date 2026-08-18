package com.monkey.ultimatebot.event;

import com.monkey.ultimatebot.api.event.base.BotEventSource;
import java.util.concurrent.Callable;

public final class BotEventSourceContext {
    private static final ThreadLocal<BotEventSource> CURRENT = new ThreadLocal<>();

    private BotEventSourceContext() {}

    public static BotEventSource currentOr(BotEventSource fallback) {
        BotEventSource current = CURRENT.get();
        return current == null ? fallback : current;
    }

    public static <T> T call(BotEventSource source, Callable<T> action) throws Exception {
        BotEventSource previous = CURRENT.get();
        CURRENT.set(source);
        try {
            return action.call();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }
}
