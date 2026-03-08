package com.monkey.mcbot.bot;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
    private UUID ownerUUID;
    private final Set<UUID> teamOwnerUUIDs = new LinkedHashSet<>();

    public BotOptions(MinecraftBot training, Map<EquipmentSlot, ItemStack> armor) {
        this.training = training;
        this.armor = armor;
        this.totems = training.getConfig().getInt("bot.default-totem-count", -1);
    }

    public BotType getBotType() {
        return botType;
    }

    public void setBotType(BotType botType) {
        this.botType = botType == null ? BotType.SINGLE : botType;
    }

    public boolean isEventBot() {
        return botType == BotType.EVENT;
    }

    public void setEventBot(boolean eventBot) {
        this.botType = eventBot ? BotType.EVENT : BotType.SINGLE;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
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
        this.rank = rank;
    }


    public int getTotems() {
        return totems;
    }

    public void setTotems(int totems) {
        this.totems = totems;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isCombat() {return combat; }
    public void setCombat(boolean combat) {this.combat = combat; }

    public Map<EquipmentSlot, ItemStack> getArmor() {
        return armor;
    }

    public Map<EquipmentSlot, Boolean> getBlast() {
        return blast;
    }
}
