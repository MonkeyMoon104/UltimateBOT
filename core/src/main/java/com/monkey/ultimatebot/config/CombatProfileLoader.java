package com.monkey.ultimatebot.config;

import com.monkey.ultimatebot.combat.profile.CombatModeConfiguration;
import com.monkey.ultimatebot.combat.profile.CombatProfileCatalog;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public final class CombatProfileLoader {
    private final Path configurationPath;
    private final Logger logger;

    public CombatProfileLoader(Path configurationPath, Logger logger) {
        this.configurationPath = Objects.requireNonNull(configurationPath, "configurationPath");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public CombatProfileCatalog load() {
        try {
            ConfigurationNode root = YamlConfigurationLoader.builder()
                    .path(configurationPath)
                    .build()
                    .load();
            Map<CombatMode, CombatModeConfiguration> modes = new LinkedHashMap<>();
            for (CombatMode mode : CombatMode.values()) {
                modes.put(mode, readMode(root, mode));
            }
            return new CombatProfileCatalog(modes);
        } catch (RuntimeException | ConfigurateException exception) {
            logger.log(Level.SEVERE, "Could not load combat-modes.yml", exception);
            throw new IllegalStateException("Combat mode configuration is invalid", exception);
        }
    }

    private CombatModeConfiguration readMode(ConfigurationNode root, CombatMode mode) {
        ConfigurationNode modeNode = root.node("combat-modes", key(mode));
        EnumMap<DifficultyTier, CombatTuning> profiles = new EnumMap<>(DifficultyTier.class);
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            ConfigurationNode base = root.node("difficulty-defaults", key(difficulty));
            ConfigurationNode override = modeNode.node("difficulties", key(difficulty));
            profiles.put(difficulty, readTuning(base, override));
        }
        return new CombatModeConfiguration(
                mode,
                modeNode.node("enabled").getBoolean(true),
                modeNode.node("icon").getString("IRON_SWORD"),
                profiles);
    }

    private CombatTuning readTuning(ConfigurationNode base, ConfigurationNode override) {
        CombatTuning fallback = CombatTuning.builder().build();
        return CombatTuning.builder()
                .attackRange(number(base, override, "attack-range", fallback.attackRange()))
                .attackCooldownTicks(integer(base, override, "attack-cooldown-ticks", fallback.attackCooldownTicks()))
                .reactionTicks(integer(base, override, "reaction-ticks", fallback.reactionTicks()))
                .movementSpeed(number(base, override, "movement-speed", fallback.movementSpeed()))
                .strafeStrength(number(base, override, "strafe-strength", fallback.strafeStrength()))
                .aimAccuracy(number(base, override, "aim-accuracy", fallback.aimAccuracy()))
                .aggression(number(base, override, "aggression", fallback.aggression()))
                .retreatHealthRatio(number(base, override, "retreat-health-ratio", fallback.retreatHealthRatio()))
                .healingHealthRatio(number(base, override, "healing-health-ratio", fallback.healingHealthRatio()))
                .specialActionCooldownTicks(
                        integer(base, override, "special-action-cooldown-ticks", fallback.specialActionCooldownTicks()))
                .pearlTriggerDistance(number(base, override, "pearl-trigger-distance", fallback.pearlTriggerDistance()))
                .maxActionsPerTick(integer(base, override, "max-actions-per-tick", fallback.maxActionsPerTick()))
                .defensiveChance(number(base, override, "defensive-chance", fallback.defensiveChance()))
                .sprintResetChance(number(base, override, "sprint-reset-chance", fallback.sprintResetChance()))
                .build();
    }

    private double number(ConfigurationNode base, ConfigurationNode override, String name, double fallback) {
        return override.node(name).virtual()
                ? base.node(name).getDouble(fallback)
                : override.node(name).getDouble(fallback);
    }

    private int integer(ConfigurationNode base, ConfigurationNode override, String name, int fallback) {
        return override.node(name).virtual()
                ? base.node(name).getInt(fallback)
                : override.node(name).getInt(fallback);
    }

    private String key(DifficultyTier value) {
        return value.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    }

    private String key(CombatMode value) {
        return value.value();
    }
}
