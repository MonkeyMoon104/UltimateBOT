package com.monkey.ultimatebot.utils.equipment;

final class TrimRegistriesLookup {
    private static final TrimRegistries INSTANCE = resolve();

    private TrimRegistriesLookup() {}

    static TrimRegistries get() {
        return INSTANCE;
    }

    private static TrimRegistries resolve() {
        try {

            Class.forName("io.papermc.paper.registry.RegistryAccess");
            Class.forName("io.papermc.paper.registry.RegistryKey");
            return (TrimRegistries) Class.forName("com.monkey.ultimatebot.utils.equipment.ModernTrimRegistries")
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyTrimRegistries();
        }
    }
}
