package com.monkey.ultimatebot.utils.equipment;

/**
 * Selects modern ({@code RegistryAccess}) or legacy ({@link org.bukkit.Registry} static fields)
 * trim registry access. Modern is loaded reflectively so 1.20–1.20.4 never link {@code
 * RegistryAccess}.
 */
final class TrimRegistriesLookup {
    private static final TrimRegistries INSTANCE = resolve();

    private TrimRegistriesLookup() {}

    static TrimRegistries get() {
        return INSTANCE;
    }

    private static TrimRegistries resolve() {
        try {
            // Probe before linking ModernTrimRegistries so 1.20.4 never loads RegistryAccess.
            Class.forName("io.papermc.paper.registry.RegistryAccess");
            Class.forName("io.papermc.paper.registry.RegistryKey");
            return (TrimRegistries)
                    Class.forName("com.monkey.ultimatebot.utils.equipment.ModernTrimRegistries")
                            .getDeclaredConstructor()
                            .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyTrimRegistries();
        }
    }
}
