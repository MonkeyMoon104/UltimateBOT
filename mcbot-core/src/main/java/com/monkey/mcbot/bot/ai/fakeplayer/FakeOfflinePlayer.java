package com.monkey.mcbot.bot.ai.fakeplayer;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/** Bukkit compatibility adapter for deprecated and return-only generic {@link OfflinePlayer} signatures. */
@SuppressWarnings({"deprecation", "TypeParameterUnusedInFormals"})
public final class FakeOfflinePlayer implements OfflinePlayer {

    private final ITrainingBot bot;

    public FakeOfflinePlayer(ITrainingBot bot) {
        this.bot = bot;
    }

    @Override
    public UUID getUniqueId() {
        return bot.asPlayer().getUUID();
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        return null;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(
            @Nullable String s, @Nullable Date date, @Nullable String s1) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(
            @Nullable String s, @Nullable Instant instant, @Nullable String s1) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(
            @Nullable String s, @Nullable Duration duration, @Nullable String s1) {
        return null;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean b) {}

    @Override
    public String getName() {
        return bot.asPlayer().getName().getString();
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Player getPlayer() {
        return bot.getTargetPlayer();
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public @Nullable Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public long getLastLogin() {
        return 0;
    }

    @Override
    public long getLastSeen() {
        return 0;
    }

    @Override
    public @Nullable Location getRespawnLocation() {
        return null;
    }

    @Override
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {}

    @Override
    public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {}

    @Override
    public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {}

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {}

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {}

    @Override
    public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {}

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {}

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {}

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int i) {}

    @Override
    public @Nullable Location getLastDeathLocation() {
        return null;
    }

    @Override
    public @Nullable Location getLocation() {
        return null;
    }

    @Override
    public PersistentDataContainerView getPersistentDataContainer() {
        return null;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean b) {}
}
