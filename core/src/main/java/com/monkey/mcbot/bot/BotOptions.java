package com.monkey.mcbot.bot;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BotOptions {

    private final MinecraftBot training;
    private final Map<EquipmentSlot, ItemStack> armor;
    private final Map<EquipmentSlot, Boolean> blast = new HashMap<>();
    private int totems;
    private boolean follow = false;
    private boolean combat = false;
    private BotRank rank = BotRank.EASY;
    private BotType botType = BotType.SINGLE;
    private BotCreationSource creationSource = BotCreationSource.CORE;
    private UUID ownerUUID;
    private UUID preferredTargetUUID;
    private final Set<UUID> teamOwnerUUIDs = new LinkedHashSet<>();
    private final Set<UUID> targetUUIDs = new LinkedHashSet<>();
    private int minTotemCount = -1;
    private Integer maxTotemCountOverride;
    private BotRank minRank = BotRank.EASY;
    private BotRank maxRank = BotRank.GOD;

    public BotOptions(MinecraftBot training, Map<EquipmentSlot, ItemStack> armor) {
        this.training = Objects.requireNonNull(training, "training");
        this.armor = Objects.requireNonNull(armor, "armor");
        this.totems = training.getConfig().getInt("bot.default-totem-count", -1);
        this.totems = clampTotemCount(this.totems);
    }

    public BotType getBotType() {
        return botType;
    }

    public void setBotType(BotType botType) {
        this.botType = botType == null ? BotType.SINGLE : botType;
        this.totems = clampTotemCount(this.totems);
    }

    public boolean isEventBot() {
        return botType == BotType.EVENT;
    }

    public void setEventBot(boolean eventBot) {
        this.setBotType(eventBot ? BotType.EVENT : BotType.SINGLE);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getPreferredTargetUUID() {
        return preferredTargetUUID;
    }

    public void setPreferredTargetUUID(UUID preferredTargetUUID) {
        this.preferredTargetUUID = preferredTargetUUID;
        if (preferredTargetUUID != null) {
            this.targetUUIDs.add(preferredTargetUUID);
        }
    }

    public Set<UUID> getTargetUUIDs() {
        return Collections.unmodifiableSet(targetUUIDs);
    }

    public void setTargetUUIDs(Set<UUID> targetUUIDs) {
        this.targetUUIDs.clear();
        if (targetUUIDs != null) {
            for (UUID targetUUID : targetUUIDs) {
                if (targetUUID != null) {
                    this.targetUUIDs.add(targetUUID);
                }
            }
        }

        if (preferredTargetUUID != null && !this.targetUUIDs.contains(preferredTargetUUID)) {
            preferredTargetUUID = null;
        }
    }

    public boolean addTargetUUID(UUID targetUUID) {
        if (targetUUID == null) {
            return false;
        }
        return this.targetUUIDs.add(targetUUID);
    }

    public boolean removeTargetUUID(UUID targetUUID) {
        if (targetUUID == null) {
            return false;
        }
        if (Objects.equals(this.preferredTargetUUID, targetUUID)) {
            this.preferredTargetUUID = null;
        }
        return this.targetUUIDs.remove(targetUUID);
    }

    public boolean hasTargetFilters() {
        return !targetUUIDs.isEmpty();
    }

    public Set<UUID> getTeamOwnerUUIDs() {
        return Collections.unmodifiableSet(teamOwnerUUIDs);
    }

    public void setTeamOwnerUUIDs(Set<UUID> owners) {
        this.teamOwnerUUIDs.clear();
        if (owners != null) {
            this.teamOwnerUUIDs.addAll(owners);
        }
    }

    public boolean addTeamOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            return false;
        }
        return this.teamOwnerUUIDs.add(ownerUUID);
    }

    public boolean removeTeamOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            return false;
        }
        return this.teamOwnerUUIDs.remove(ownerUUID);
    }

    public boolean isTeamOwner(UUID ownerUUID) {
        return ownerUUID != null && teamOwnerUUIDs.contains(ownerUUID);
    }

    public BotRank getRank() {
        return rank;
    }

    public void setRank(BotRank rank) {
        this.rank = clampRank(rank == null ? BotRank.EASY : rank);
    }

    public BotRank getMinRank() {
        return minRank;
    }

    public BotRank getMaxRank() {
        return maxRank;
    }

    public void setRankRange(BotRank minRank, BotRank maxRank) {
        validateRankRange(minRank, maxRank);
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.rank = clampRank(this.rank);
    }

    public boolean isRankAllowed(BotRank rank) {
        if (rank == null) {
            return false;
        }
        return rank.ordinal() >= minRank.ordinal() && rank.ordinal() <= maxRank.ordinal();
    }

    public BotRank nextAllowedRank(BotRank current, boolean forward) {
        List<BotRank> allowedRanks = getAllowedRanks();
        if (allowedRanks.isEmpty()) {
            return BotRank.EASY;
        }

        BotRank base = current == null ? allowedRanks.get(0) : current;
        int index = allowedRanks.indexOf(base);
        if (index < 0) {
            return allowedRanks.get(0);
        }

        int nextIndex;
        if (forward) {
            nextIndex = (index + 1) % allowedRanks.size();
        } else {
            nextIndex = (index - 1 + allowedRanks.size()) % allowedRanks.size();
        }
        return allowedRanks.get(nextIndex);
    }

    public List<BotRank> getAllowedRanks() {
        List<BotRank> allowed = new ArrayList<>();
        for (BotRank value : BotRank.values()) {
            if (isRankAllowed(value)) {
                allowed.add(value);
            }
        }
        return allowed;
    }


    public int getTotems() {
        return totems;
    }

    public void setTotems(int totems) {
        this.totems = clampTotemCount(totems);
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isCombat() {return combat; }
    public void setCombat(boolean combat) {this.combat = combat; }

    public BotCreationSource getCreationSource() {
        return creationSource;
    }

    public void setCreationSource(BotCreationSource creationSource) {
        this.creationSource = creationSource == null ? BotCreationSource.CORE : creationSource;
    }

    public int getMinTotemCount() {
        return minTotemCount;
    }

    public int getMaxTotemCount() {
        int coreMax = getCoreMaxTotemCount();
        if (maxTotemCountOverride == null) {
            return coreMax;
        }
        return Math.min(maxTotemCountOverride, coreMax);
    }

    public void setTotemRange(int minTotemCount, int maxTotemCount) {
        validateTotemRange(minTotemCount, maxTotemCount);
        this.minTotemCount = minTotemCount;
        this.maxTotemCountOverride = maxTotemCount;
        this.totems = clampTotemCount(this.totems);
    }

    public int clampTotemCount(int candidate) {
        int min = minTotemCount;
        int max = getMaxTotemCount();

        int value = candidate;
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        if (value == -1 && min > -1) {
            value = min;
        }

        return value;
    }

    public int clampCurrentTotemCount() {
        this.totems = clampTotemCount(this.totems);
        return this.totems;
    }

    public boolean allowsUnlimitedTotems() {
        return minTotemCount <= -1;
    }

    public Map<EquipmentSlot, ItemStack> getArmor() {
        return armor;
    }

    public Map<EquipmentSlot, Boolean> getBlast() {
        return blast;
    }

    private int getCoreMaxTotemCount() {
        int normalMax = training.getConfig().getInt("bot.max-totem-normal", 37);
        int eventMax = training.getConfig().getInt("bot.max-totem-event", 74);
        return isEventBot() ? eventMax : normalMax;
    }

    private static void validateTotemRange(int minTotemCount, int maxTotemCount) {
        if (minTotemCount < -1) {
            throw new IllegalArgumentException("min totem count cannot be less than -1");
        }
        if (maxTotemCount < 0) {
            throw new IllegalArgumentException("max totem count cannot be less than 0");
        }
        if (minTotemCount > maxTotemCount) {
            throw new IllegalArgumentException("min totem count cannot be greater than max totem count");
        }
    }

    private static void validateRankRange(BotRank minRank, BotRank maxRank) {
        if (minRank == null || maxRank == null) {
            throw new IllegalArgumentException("rank bounds cannot be null");
        }
        if (minRank.ordinal() > maxRank.ordinal()) {
            throw new IllegalArgumentException("min rank cannot be greater than max rank");
        }
    }

    private BotRank clampRank(BotRank candidate) {
        if (candidate.ordinal() < minRank.ordinal()) {
            return minRank;
        }
        if (candidate.ordinal() > maxRank.ordinal()) {
            return maxRank;
        }
        return candidate;
    }
}
