package com.monkey.mcbot.bot.ai.fakeplayer;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.PlayerGiveResult;
import io.papermc.paper.math.Position;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import net.minecraft.world.entity.player.Player;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BotCraftPlayer extends CraftHumanEntity implements org.bukkit.entity.Player {

    private final ITrainingBot trainingBot;

    public BotCraftPlayer(ITrainingBot trainingBot) {
        super((CraftServer) Bukkit.getServer(), trainingBot.asPlayer());
        this.trainingBot = trainingBot;
    }


    @Override
    public String toString() {
        return "BotCraftPlayer{name=" + getName() + "}";
    }

    @Override
    public org.bukkit.entity.Player.Spigot spigot() {
        return new org.bukkit.entity.Player.Spigot() {};
    }

    @Override
    public void sendEntityEffect(EntityEffect effect, Entity target) {

    }

    @Override
    public PlayerGiveResult give(Collection<ItemStack> items, boolean dropIfFull) {
        return null;
    }

    @Override
    public int getDeathScreenScore() {
        return 0;
    }

    @Override
    public void setDeathScreenScore(int score) {

    }

    @Override
    public Player getHandle() {
        return trainingBot.asPlayer();
    }

    @Override
    public @UnmodifiableView Iterable<? extends BossBar> activeBossBars() {
        return null;
    }

    @Override
    public Component displayName() {
        return null;
    }

    @Override
    public void displayName(@Nullable Component displayName) {

    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void setDisplayName(String name) {
    }

    @Override
    public void playerListName(@Nullable Component name) {

    }

    @Override
    public Component playerListName() {
        return null;
    }

    @Override
    public @Nullable Component playerListHeader() {
        return null;
    }

    @Override
    public @Nullable Component playerListFooter() {
        return null;
    }

    @Override
    public String getPlayerListName() {
        return getName();
    }

    @Override
    public void setPlayerListName(String name) {
    }

    @Override
    public int getPlayerListOrder() {
        return 0;
    }

    @Override
    public void setPlayerListOrder(int order) {

    }

    @Override
    public @Nullable String getPlayerListHeader() {
        return "";
    }

    @Override
    public @Nullable String getPlayerListFooter() {
        return "";
    }

    @Override
    public void setPlayerListHeader(@Nullable String header) {

    }

    @Override
    public void setPlayerListFooter(@Nullable String footer) {

    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable String header, @Nullable String footer) {

    }

    @Override
    public void setCompassTarget(org.bukkit.Location loc) {
    }

    @Override
    public org.bukkit.Location getCompassTarget() {
        return getLocation();
    }

    @Override
    public java.net.InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public @Nullable InetSocketAddress getVirtualHost() {
        return null;
    }

    @Override
    public @Nullable InetSocketAddress getHAProxyAddress() {
        return null;
    }

    @Override
    public boolean isTransferred() {
        return false;
    }

    @Override
    public CompletableFuture<byte[]> retrieveCookie(NamespacedKey key) {
        return null;
    }

    @Override
    public void storeCookie(NamespacedKey key, byte[] value) {

    }

    @Override
    public void transfer(String host, int port) {

    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String input) {
    }

    @Override
    public boolean beginConversation(org.bukkit.conversations.Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(org.bukkit.conversations.Conversation conversation) {
    }

    @Override
    public void abandonConversation(org.bukkit.conversations.Conversation conversation, org.bukkit.conversations.ConversationAbandonedEvent details) {
    }

    @Override
    public void sendRawMessage(String message) {
    }

    @Override
    public void sendRawMessage(@org.jetbrains.annotations.Nullable UUID sender, @NotNull String message) {

    }

    @Override
    public void kickPlayer(String message) {
    }

    @Override
    public void kick() {

    }

    @Override
    public void kick(@Nullable Component message) {

    }

    @Override
    public void kick(@Nullable Component message, PlayerKickEvent.Cause cause) {

    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Date expires, @Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Instant expires, @Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Duration duration, @Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public @Nullable BanEntry<InetAddress> banIp(@Nullable String reason, @Nullable Date expires, @Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public @Nullable BanEntry<InetAddress> banIp(@Nullable String reason, @Nullable Instant expires, @Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public @Nullable BanEntry<InetAddress> banIp(@Nullable String reason, @Nullable Duration duration, @Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public void chat(String msg) {
    }

    @Override
    public boolean performCommand(String command) {
        return false;
    }

    @Override
    public boolean isSneaking() {
        return trainingBot.asPlayer().isShiftKeyDown();
    }

    @Override
    public void setSneaking(boolean sneak) {
        trainingBot.asPlayer().setShiftKeyDown(sneak);
    }

    @Override
    public boolean isSprinting() {
        return trainingBot.asPlayer().isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        trainingBot.asPlayer().setSprinting(sprinting);
    }

    @Override
    public void saveData() {
    }

    @Override
    public void loadData() {
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
    }

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Override
    public org.bukkit.Location getBedSpawnLocation() {
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
    public void setBedSpawnLocation(org.bukkit.Location location) {
    }

    @Override
    public void setRespawnLocation(@Nullable Location location) {

    }

    @Override
    public void setBedSpawnLocation(org.bukkit.Location location, boolean force) {
    }

    @Override
    public void setRespawnLocation(@Nullable Location location, boolean force) {

    }

    @Override
    public Collection<EnderPearl> getEnderPearls() {
        return List.of();
    }

    @Override
    public Input getCurrentInput() {
        return null;
    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public void setAllowFlight(boolean flight) {
    }

    @Override
    public void setFlyingFallDamage(TriState flyingFallDamage) {

    }

    @Override
    public TriState hasFlyingFallDamage() {
        return null;
    }

    @Override
    public void hidePlayer(org.bukkit.entity.Player player) {
    }

    @Override
    public void hidePlayer(org.bukkit.plugin.Plugin plugin, org.bukkit.entity.Player player) {
    }

    @Override
    public void showPlayer(org.bukkit.entity.Player player) {
    }

    @Override
    public void showPlayer(org.bukkit.plugin.Plugin plugin, org.bukkit.entity.Player player) {
    }

    @Override
    public boolean canSee(org.bukkit.entity.Player player) {
        return true;
    }

    @Override
    public void hideEntity(Plugin plugin, Entity entity) {

    }

    @Override
    public void showEntity(Plugin plugin, Entity entity) {

    }

    @Override
    public boolean canSee(Entity entity) {
        return false;
    }

    @Override
    public boolean isListed(org.bukkit.entity.Player other) {
        return false;
    }

    @Override
    public boolean unlistPlayer(org.bukkit.entity.Player other) {
        return false;
    }

    @Override
    public boolean listPlayer(org.bukkit.entity.Player other) {
        return false;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public void setFlying(boolean value) {
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {
    }

    @Override
    public float getFlySpeed() {
        return 0;
    }

    @Override
    public float getWalkSpeed() {
        return 0.2f;
    }

    @Override
    public void setTexturePack(String url) {

    }

    @Override
    public void setResourcePack(String url) {

    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash) {

    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash, @Nullable String prompt) {

    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash, boolean force) {

    }

    @Override
    public void setResourcePack(String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {

    }

    @Override
    public void setResourcePack(UUID id, String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {

    }

    @Override
    public void setResourcePack(UUID uuid, String url, byte @Nullable [] hash, @Nullable Component prompt, boolean force) {

    }

    @Override
    public PlayerResourcePackStatusEvent.@Nullable Status getResourcePackStatus() {
        return null;
    }

    @Override
    public void addResourcePack(UUID id, String url, @Nullable byte[] hash, @Nullable String prompt, boolean force) {

    }

    @Override
    public void removeResourcePack(UUID id) {

    }

    @Override
    public void removeResourcePacks() {

    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public @Nullable WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public void setWorldBorder(@Nullable WorldBorder border) {

    }

    @Override
    public void sendHealthUpdate(double health, int foodLevel, float saturation) {

    }

    @Override
    public void sendHealthUpdate() {

    }

    @Override
    public void playNote(org.bukkit.Location loc, org.bukkit.Instrument instrument, org.bukkit.Note note) {
    }

    @Override
    public void playSound(org.bukkit.Location location, org.bukkit.Sound sound, float volume, float pitch) {
    }

    @Override
    public void playSound(org.bukkit.Location location, String sound, float volume, float pitch) {
    }

    @Override
    public void playSound(org.bukkit.Location location, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playSound(org.bukkit.Location location, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSound(Entity entity, Sound sound, float volume, float pitch) {

    }

    @Override
    public void playSound(Entity entity, String sound, float volume, float pitch) {

    }

    @Override
    public void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void stopSound(org.bukkit.Sound sound) {
    }

    @Override
    public void stopSound(String sound) {
    }

    @Override
    public void stopSound(org.bukkit.Sound sound, org.bukkit.SoundCategory category) {
    }

    @Override
    public void stopSound(String sound, org.bukkit.SoundCategory category) {
    }

    @Override
    public void stopSound(SoundCategory category) {

    }

    @Override
    public void stopAllSounds() {

    }

    @Override
    public void playEffect(org.bukkit.Location loc, org.bukkit.Effect effect, int data) {
    }

    @Override
    public <T> void playEffect(org.bukkit.Location loc, org.bukkit.Effect effect, T data) {
    }

    @Override
    public boolean breakBlock(org.bukkit.block.Block block) {
        return false;
    }


    @Override
    public void sendBlockChange(org.bukkit.Location loc, org.bukkit.block.data.BlockData block) {
    }

    @Override
    public void sendBlockChanges(Collection<BlockState> blocks) {

    }

    @Override
    public void sendBlockChanges(Collection<BlockState> blocks, boolean suppressLightUpdates) {

    }

    @Override
    public void sendBlockDamage(org.bukkit.Location loc, float progress) {
    }

    @Override
    public void sendMultiBlockChange(Map<? extends Position, BlockData> blockChanges) {

    }

    @Override
    public void sendBlockDamage(Location loc, float progress, Entity source) {

    }

    @Override
    public void sendBlockDamage(Location loc, float progress, int sourceId) {

    }

    @Override
    public void sendEquipmentChange(LivingEntity entity, EquipmentSlot slot, @Nullable ItemStack item) {

    }

    @Override
    public void sendEquipmentChange(LivingEntity entity, Map<EquipmentSlot, @Nullable ItemStack> items) {

    }

    @Override
    public void sendSignChange(Location loc, @Nullable List<? extends Component> lines, DyeColor dyeColor, boolean hasGlowingText) throws IllegalArgumentException {

    }


    @Override
    public void sendSignChange(Location loc, @Nullable String[] lines, DyeColor dyeColor) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(Location loc, @Nullable String[] lines, DyeColor dyeColor, boolean hasGlowingText) throws IllegalArgumentException {

    }

    @Override
    public void sendBlockUpdate(Location loc, TileState tileState) throws IllegalArgumentException {

    }

    @Override
    public void sendPotionEffectChange(LivingEntity entity, PotionEffect effect) {

    }

    @Override
    public void sendPotionEffectChangeRemove(LivingEntity entity, PotionEffectType type) {

    }

    @Override
    public void sendMap(org.bukkit.map.MapView map) {
    }

    @Override
    public void showWinScreen() {

    }

    @Override
    public boolean hasSeenWinScreen() {
        return false;
    }

    @Override
    public void setHasSeenWinScreen(boolean hasSeenWinScreen) {

    }

    @Override
    public void sendActionBar(String message) {

    }

    @Override
    public void sendActionBar(char alternateChar, String message) {

    }

    @Override
    public void sendActionBar(BaseComponent... message) {

    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent @Nullable [] header, BaseComponent @Nullable [] footer) {

    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable BaseComponent header, @Nullable BaseComponent footer) {

    }

    @Override
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void setSubtitle(BaseComponent[] subtitle) {

    }

    @Override
    public void setSubtitle(BaseComponent subtitle) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent[] title) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent title) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent[] title, @Nullable BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void sendTitle(Title title) {

    }

    @Override
    public void updateTitle(Title title) {

    }

    @Override
    public void hideTitle() {

    }

    @Override
    public void sendHurtAnimation(float yaw) {

    }

    @Override
    public void sendLinks(ServerLinks links) {

    }

    @Override
    public void addCustomChatCompletions(Collection<String> completions) {

    }

    @Override
    public void removeCustomChatCompletions(Collection<String> completions) {

    }

    @Override
    public void setCustomChatCompletions(Collection<String> completions) {

    }

    @Override
    public void updateInventory() {
    }

    @Override
    public @Nullable GameMode getPreviousGameMode() {
        return null;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic statistic) throws IllegalArgumentException {
    }

    @Override
    public void decrementStatistic(org.bukkit.Statistic statistic) throws IllegalArgumentException {
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic statistic, int amount) throws IllegalArgumentException {
    }

    @Override
    public void decrementStatistic(org.bukkit.Statistic statistic, int amount) throws IllegalArgumentException {
    }

    @Override
    public void setStatistic(org.bukkit.Statistic statistic, int newValue) throws IllegalArgumentException {
    }

    @Override
    public int getStatistic(org.bukkit.Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic statistic, org.bukkit.Material material) throws IllegalArgumentException {
    }

    @Override
    public void decrementStatistic(org.bukkit.Statistic statistic, org.bukkit.Material material) throws IllegalArgumentException {
    }

    @Override
    public int getStatistic(org.bukkit.Statistic statistic, org.bukkit.Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic statistic, org.bukkit.Material material, int amount) throws IllegalArgumentException {
    }

    @Override
    public void decrementStatistic(org.bukkit.Statistic statistic, org.bukkit.Material material, int amount) throws IllegalArgumentException {
    }

    @Override
    public void setStatistic(org.bukkit.Statistic statistic, org.bukkit.Material material, int newValue) throws IllegalArgumentException {
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic statistic, org.bukkit.entity.EntityType entityType) throws IllegalArgumentException {
    }

    @Override
    public void decrementStatistic(org.bukkit.Statistic statistic, org.bukkit.entity.EntityType entityType) throws IllegalArgumentException {
    }

    @Override
    public int getStatistic(org.bukkit.Statistic statistic, org.bukkit.entity.EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic statistic, org.bukkit.entity.EntityType entityType, int amount) throws IllegalArgumentException {
    }

    @Override
    public void decrementStatistic(org.bukkit.Statistic statistic, org.bukkit.entity.EntityType entityType, int amount) {
    }

    @Override
    public void setStatistic(org.bukkit.Statistic statistic, org.bukkit.entity.EntityType entityType, int newValue) {
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
    }

    @Override
    public long getPlayerTime() {
        return getWorld().getTime();
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public void resetPlayerTime() {
    }

    @Override
    public void setPlayerWeather(org.bukkit.WeatherType type) {
    }

    @Override
    public org.bukkit.WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {
    }

    @Override
    public void giveExp(int amount) {
    }

    @Override
    public int getExpCooldown() {
        return 0;
    }

    @Override
    public void setExpCooldown(int ticks) {

    }

    @Override
    public void giveExp(int amount, boolean applyMending) {

    }

    @Override
    public int applyMending(int amount) {
        return 0;
    }

    @Override
    public void giveExpLevels(int amount) {
    }

    @Override
    public float getExp() {
        return 0;
    }

    @Override
    public void setExp(float exp) {
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {
    }

    @Override
    public int getTotalExperience() {
        return 0;
    }

    @Override
    public void setTotalExperience(int exp) {
    }

    @Override
    public @Range(from = 0L, to = 2147483647L) int calculateTotalExperiencePoints() {
        return 0;
    }

    @Override
    public void setExperienceLevelAndProgress(@Range(from = 0L, to = 2147483647L) int totalExperience) {

    }

    @Override
    public int getExperiencePointsNeededForNextLevel() {
        return 0;
    }

    @Override
    public void sendExperienceChange(float progress) {
    }

    @Override
    public void sendExperienceChange(float progress, int level) {
    }

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean scale) {
    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {
    }

    @Override
    public double getHealthScale() {
        return 20.0;
    }

    @Override
    public org.bukkit.entity.Entity getSpectatorTarget() {
        return null;
    }

    @Override
    public void setSpectatorTarget(org.bukkit.entity.Entity entity) {
    }

    @Override
    public void sendTitle(String title, String subtitle) {
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    }

    @Override
    public void resetTitle() {
    }

    @Override
    public void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count) {
    }

    @Override
    public void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count) {
    }

    @Override
    public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, T data) {
    }

    @Override
    public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, T data) {
    }

    @Override
    public void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ) {
    }

    @Override
    public void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    }

    @Override
    public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
    }

    @Override
    public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
    }

    @Override
    public void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
    }

    @Override
    public void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
    }

    @Override
    public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
    }

    @Override
    public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {

    }

    @Override
    public org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement advancement) {
        return null;
    }

    @Override
    public int getClientViewDistance() {
        return 10;
    }

    @Override
    public Locale locale() {
        return null;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    public String getLocale() {
        return "en_US";
    }

    @Override
    public boolean getAffectsSpawning() {
        return false;
    }

    @Override
    public void setAffectsSpawning(boolean affects) {

    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public void setViewDistance(int viewDistance) {

    }

    @Override
    public int getSimulationDistance() {
        return 0;
    }

    @Override
    public void setSimulationDistance(int simulationDistance) {

    }

    @Override
    public int getSendViewDistance() {
        return 0;
    }

    @Override
    public void setSendViewDistance(int viewDistance) {

    }

    @Override
    public void updateCommands() {
    }

    @Override
    public void openBook(org.bukkit.inventory.ItemStack book) {
    }

    @Override
    public void openSign(Sign sign) {

    }

    @Override
    public void showDemoScreen() {

    }

    @Override
    public boolean isAllowingServerListings() {
        return false;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
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
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Date expires, @Nullable String source) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Instant expires, @Nullable String source) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Duration duration, @Nullable String source) {
        return null;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean value) {

    }

    @Override
    public org.bukkit.entity.@Nullable Player getPlayer() {
        return null;
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
        return false;
    }

    @Override
    public void setPlayerProfile(PlayerProfile profile) {

    }

    @Override
    public float getCooldownPeriod() {
        return 0;
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        return 0;
    }

    @Override
    public void resetCooldown() {

    }

    @Override
    public <T> T getClientOption(ClientOption<T> option) {
        return null;
    }

    @Override
    public void sendOpLevel(byte level) {

    }

    @Override
    public void addAdditionalChatCompletions(Collection<String> completions) {

    }

    @Override
    public void removeAdditionalChatCompletions(Collection<String> completions) {

    }

    @Override
    public @Nullable String getClientBrandName() {
        return "";
    }

    @Override
    public void lookAt(Entity entity, LookAnchor playerAnchor, LookAnchor entityAnchor) {

    }

    @Override
    public void showElderGuardian(boolean silent) {

    }

    @Override
    public int getWardenWarningCooldown() {
        return 0;
    }

    @Override
    public void setWardenWarningCooldown(int cooldown) {

    }

    @Override
    public int getWardenTimeSinceLastWarning() {
        return 0;
    }

    @Override
    public void setWardenTimeSinceLastWarning(int time) {

    }

    @Override
    public int getWardenWarningLevel() {
        return 0;
    }

    @Override
    public void setWardenWarningLevel(int warningLevel) {

    }

    @Override
    public void increaseWardenWarningLevel() {

    }

    @Override
    public Duration getIdleDuration() {
        return null;
    }

    @Override
    public void resetIdleDuration() {

    }

    @Override
    public @Unmodifiable Set<Long> getSentChunkKeys() {
        return Set.of();
    }

    @Override
    public @Unmodifiable Set<Chunk> getSentChunks() {
        return Set.of();
    }

    @Override
    public boolean isChunkSent(long chunkKey) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof org.bukkit.entity.Player) {
            return getUniqueId().equals(((org.bukkit.entity.Player) obj).getUniqueId());
        }
        return super.equals(obj);
    }

    @Override
    @Deprecated
    public void playNote(org.bukkit.Location loc, byte instrument, byte note) {
    }

    @Override
    @Deprecated
    public void sendBlockChange(org.bukkit.Location loc, org.bukkit.Material material, byte data) {
    }


    @Override
    @Deprecated
    public void sendSignChange(org.bukkit.Location loc, String[] lines) {
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, byte @NotNull [] message) {

    }

    @Override
    public @NotNull Set<String> getListeningPluginChannels() {
        return Set.of();
    }
}