package com.monkey.mcbot.placeholders;

import java.util.List;

public interface PlaceholderRegistration {

    boolean register();

    boolean unregister();

    String getIdentifier();

    List<String> getRegisteredPlaceholderKeys();
}
