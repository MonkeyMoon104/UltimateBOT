package com.monkey.mcbot.bot;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.model.BotLocation;
import com.monkey.mcbot.api.model.BotSkin;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import com.monkey.mcbot.utils.armor.ArmorTier;
import com.monkey.mcbot.utils.equipment.ArmorTrimUtils;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BotOptions {

    private final MinecraftBot training;
    private final Map<EquipmentSlot, ItemStack> armor;
    private final Map<EquipmentSlot, Boolean> blast = new HashMap<>();
    private int totems;
    private boolean follow = false;
    private boolean combat = false;
    private boolean changeableFollow = true;
    private boolean changeableCombat = true;
    private boolean changeableBlast = true;
    private boolean changeableArmor = true;
    private boolean changeableTotem = true;
    private boolean changeableRank = true;
    private BotRank rank = BotRank.EASY;
    private BotType botType = BotType.SINGLE;
    private BotCreationSource creationSource = BotCreationSource.CORE;
    private UUID ownerUUID;
    private UUID preferredTargetUUID;
    private final Set<UUID> teamOwnerUUIDs = new LinkedHashSet<>();
    private final Set<UUID> targetUUIDs = new LinkedHashSet<>();
    private String botNameTemplate;
    private BotSkin botSkin = BotSkin.owner();
    private int minTotemCount = -1;
    private Integer maxTotemCountOverride;
    private BotRank minRank = BotRank.EASY;
    private BotRank maxRank = BotRank.GOD;
    private ArmorTier minArmorTier = ArmorTier.LEATHER;
    private ArmorTier maxArmorTier = ArmorTier.NETHERITE;
    private final Map<EquipmentSlot, String> trimPatternKeys = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, String> trimMaterialKeys = new EnumMap<>(EquipmentSlot.class);
    private BotLocation spawnLocation;
    private boolean autoTarget = false;
    private double autoTargetRange = 16.0D;
    private boolean respectWorldGuardPvp = false;
    private boolean stayAfterOwnerDeath = false;
    private boolean idleWander = false;
    private double idleWanderRadius = 10.0D;
    private double idleReturnDistance = 24.0D;
    private long idleReturnDelayMs = 8000L;
    private boolean crystalPvp = true;
    private boolean explosions = true;
    private boolean enderPearls = true;
    private boolean killMessageEnabled = true;
    private String customKillMessage;
    private final Map<Integer, ItemStack> equipmentContents = new HashMap<>();

    public BotOptions(MinecraftBot training, Map<EquipmentSlot, ItemStack> armor) {
        this.training = Objects.requireNonNull(training, "training");
        this.armor = Objects.requireNonNull(armor, "armor");
        this.totems = training.getConfig().getInt("bot.default-totem-count", -1);
        this.totems = clampTotemCount(this.totems);
        clampCurrentArmor();
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

    public String getBotNameTemplate() {
        return botNameTemplate;
    }

    public void setBotNameTemplate(String botNameTemplate) {
        this.botNameTemplate = botNameTemplate;
    }

    public BotSkin getBotSkin() {
        return botSkin;
    }

    public void setBotSkin(BotSkin botSkin) {
        this.botSkin = botSkin == null ? BotSkin.owner() : botSkin;
    }

    public BotLocation getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(BotLocation spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public boolean isAutoTarget() {
        return autoTarget;
    }

    public void setAutoTarget(boolean autoTarget) {
        this.autoTarget = autoTarget;
    }

    public double getAutoTargetRange() {
        return autoTargetRange;
    }

    public void setAutoTargetRange(double autoTargetRange) {
        this.autoTargetRange = autoTargetRange <= 0.0D ? 16.0D : autoTargetRange;
    }

    public boolean isRespectWorldGuardPvp() {
        return respectWorldGuardPvp;
    }

    public void setRespectWorldGuardPvp(boolean respectWorldGuardPvp) {
        this.respectWorldGuardPvp = respectWorldGuardPvp;
    }

    public boolean isStayAfterOwnerDeath() {
        return stayAfterOwnerDeath;
    }

    public void setStayAfterOwnerDeath(boolean stayAfterOwnerDeath) {
        this.stayAfterOwnerDeath = stayAfterOwnerDeath;
    }

    public boolean isIdleWander() {
        return idleWander;
    }

    public void setIdleWander(boolean idleWander) {
        this.idleWander = idleWander;
    }

    public double getIdleWanderRadius() {
        return idleWanderRadius;
    }

    public void setIdleWanderRadius(double idleWanderRadius) {
        this.idleWanderRadius = idleWanderRadius <= 0.0D ? 10.0D : idleWanderRadius;
    }

    public double getIdleReturnDistance() {
        return idleReturnDistance;
    }

    public void setIdleReturnDistance(double idleReturnDistance) {
        this.idleReturnDistance = idleReturnDistance <= 0.0D ? 24.0D : idleReturnDistance;
    }

    public long getIdleReturnDelayMs() {
        return idleReturnDelayMs;
    }

    public void setIdleReturnDelayMs(long idleReturnDelayMs) {
        this.idleReturnDelayMs = Math.max(0L, idleReturnDelayMs);
    }

    public boolean isCrystalPvp() {
        return crystalPvp;
    }

    public void setCrystalPvp(boolean crystalPvp) {
        this.crystalPvp = crystalPvp;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
        if (!explosions) {
            this.crystalPvp = false;
        }
    }

    public boolean isEnderPearls() {
        return enderPearls;
    }

    public void setEnderPearls(boolean enderPearls) {
        this.enderPearls = enderPearls;
    }

    public boolean isKillMessageEnabled() {
        return killMessageEnabled;
    }

    public void setKillMessageEnabled(boolean killMessageEnabled) {
        this.killMessageEnabled = killMessageEnabled;
    }

    public String getCustomKillMessage() {
        return customKillMessage;
    }

    public void setCustomKillMessage(String customKillMessage) {
        this.customKillMessage = customKillMessage == null || customKillMessage.isBlank() ? null : customKillMessage;
    }

    public Map<Integer, ItemStack> getEquipmentContents() {
        return equipmentContents;
    }

    public void setEquipmentContents(Map<Integer, ItemStack> equipmentContents) {
        this.equipmentContents.clear();
        if (equipmentContents != null) {
            for (Map.Entry<Integer, ItemStack> entry : equipmentContents.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.equipmentContents.put(entry.getKey(), entry.getValue().clone());
                }
            }
        }
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

    public ArmorTier getMinArmorTier() {
        return minArmorTier;
    }

    public ArmorTier getMaxArmorTier() {
        return maxArmorTier;
    }

    public void setArmorRange(ArmorTier minArmorTier, ArmorTier maxArmorTier) {
        validateArmorRange(minArmorTier, maxArmorTier);
        this.minArmorTier = minArmorTier;
        this.maxArmorTier = maxArmorTier;
        clampCurrentArmor();
    }

    public boolean isArmorTierAllowed(ArmorTier armorTier) {
        if (armorTier == null) {
            return false;
        }
        return armorTier.ordinal() >= minArmorTier.ordinal() && armorTier.ordinal() <= maxArmorTier.ordinal();
    }

    public ArmorTier clampArmorTier(ArmorTier armorTier) {
        if (armorTier == null) {
            return minArmorTier;
        }
        if (armorTier.ordinal() < minArmorTier.ordinal()) {
            return minArmorTier;
        }
        if (armorTier.ordinal() > maxArmorTier.ordinal()) {
            return maxArmorTier;
        }
        return armorTier;
    }

    public void setArmorType(ArmorTier armorTier) {
        ArmorTier clampedTier = clampArmorTier(armorTier);
        for (EquipmentSlot slot : com.monkey.mcbot.utils.equipment.EquipmentConverter.getArmorSlots()) {
            ItemStack currentPiece = armor.get(slot);
            ItemStack updated = currentPiece == null
                    ? new ItemStack(clampedTier.toMaterial(slot))
                    : currentPiece.withType(clampedTier.toMaterial(slot));
            armor.put(slot, updated);
        }
        applyAllTrimSelections();
    }

    public void clampCurrentArmor() {
        for (EquipmentSlot slot : com.monkey.mcbot.utils.equipment.EquipmentConverter.getArmorSlots()) {
            ItemStack currentPiece = armor.get(slot);
            ItemStack updated = currentPiece == null
                    ? new ItemStack(minArmorTier.toMaterial(slot))
                    : currentPiece.withType(ArmorCycle.clampArmor(currentPiece.getType(), slot, minArmorTier, maxArmorTier));
            armor.put(slot, updated);
        }
        applyAllTrimSelections();
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

    public boolean isChangeableFollow() {
        return changeableFollow;
    }

    public void setChangeableFollow(boolean changeableFollow) {
        this.changeableFollow = changeableFollow;
    }

    public boolean isChangeableCombat() {
        return changeableCombat;
    }

    public void setChangeableCombat(boolean changeableCombat) {
        this.changeableCombat = changeableCombat;
    }

    public boolean isChangeableBlast() {
        return changeableBlast;
    }

    public void setChangeableBlast(boolean changeableBlast) {
        this.changeableBlast = changeableBlast;
    }

    public boolean isChangeableArmor() {
        return changeableArmor;
    }

    public void setChangeableArmor(boolean changeableArmor) {
        this.changeableArmor = changeableArmor;
    }

    public boolean isChangeableTotem() {
        return changeableTotem;
    }

    public void setChangeableTotem(boolean changeableTotem) {
        this.changeableTotem = changeableTotem;
    }

    public boolean isChangeableRank() {
        return changeableRank;
    }

    public void setChangeableRank(boolean changeableRank) {
        this.changeableRank = changeableRank;
    }

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

    public String getTrimPatternKey(EquipmentSlot slot) {
        return trimPatternKeys.get(slot);
    }

    public void setTrimPatternKey(EquipmentSlot slot, String trimPatternKey) {
        if (slot == null) {
            return;
        }
        trimPatternKeys.put(slot, trimPatternKey);
        applyTrimSelection(slot);
    }

    public String getTrimMaterialKey(EquipmentSlot slot) {
        return trimMaterialKeys.get(slot);
    }

    public void setTrimMaterialKey(EquipmentSlot slot, String trimMaterialKey) {
        if (slot == null) {
            return;
        }
        trimMaterialKeys.put(slot, trimMaterialKey);
        applyTrimSelection(slot);
    }

    public boolean hasCompleteTrimSelection(EquipmentSlot slot) {
        return ArmorTrimUtils.isCompleteSelection(getTrimPatternKey(slot), getTrimMaterialKey(slot));
    }

    private void applyAllTrimSelections() {
        for (EquipmentSlot slot : com.monkey.mcbot.utils.equipment.EquipmentConverter.getArmorSlots()) {
            applyTrimSelection(slot);
        }
    }

    private void applyTrimSelection(EquipmentSlot slot) {
        ItemStack piece = armor.get(slot);
        if (piece == null) {
            return;
        }

        ItemStack updated = new ItemStack(piece.getType(), Math.max(1, piece.getAmount()));
        ArmorTrimUtils.applyTrim(updated, getTrimPatternKey(slot), getTrimMaterialKey(slot));
        armor.put(slot, updated);
    }

    public boolean isBlastProtection() {
        for (EquipmentSlot slot : com.monkey.mcbot.utils.equipment.EquipmentConverter.getArmorSlots()) {
            if (!blast.getOrDefault(slot, false)) {
                return false;
            }
        }
        return true;
    }

    public void setBlastProtection(boolean blastProtection) {
        setBlastProtection(blastProtection, blastProtection, blastProtection, blastProtection);
    }

    public void setBlastProtection(boolean bootsBlastEnabled,
                                   boolean leggingsBlastEnabled,
                                   boolean chestplateBlastEnabled,
                                   boolean helmetBlastEnabled) {
        blast.put(EquipmentSlot.FEET, bootsBlastEnabled);
        blast.put(EquipmentSlot.LEGS, leggingsBlastEnabled);
        blast.put(EquipmentSlot.CHEST, chestplateBlastEnabled);
        blast.put(EquipmentSlot.HEAD, helmetBlastEnabled);
    }

    public void setBlastProtection(int bootsBlastEnabled,
                                   int leggingsBlastEnabled,
                                   int chestplateBlastEnabled,
                                   int helmetBlastEnabled) {
        setBlastProtection(
                parseBlastBinary(bootsBlastEnabled, "boots"),
                parseBlastBinary(leggingsBlastEnabled, "leggings"),
                parseBlastBinary(chestplateBlastEnabled, "chestplate"),
                parseBlastBinary(helmetBlastEnabled, "helmet")
        );
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

    private static void validateArmorRange(ArmorTier minArmorTier, ArmorTier maxArmorTier) {
        if (minArmorTier == null || maxArmorTier == null) {
            throw new IllegalArgumentException("armor bounds cannot be null");
        }
        if (minArmorTier.ordinal() > maxArmorTier.ordinal()) {
            throw new IllegalArgumentException("min armor cannot be greater than max armor");
        }
    }

    private static boolean parseBlastBinary(int value, String fieldName) {
        if (value == 0) {
            return false;
        }
        if (value == 1) {
            return true;
        }
        throw new IllegalArgumentException(fieldName + " blast value must be 0 or 1");
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
