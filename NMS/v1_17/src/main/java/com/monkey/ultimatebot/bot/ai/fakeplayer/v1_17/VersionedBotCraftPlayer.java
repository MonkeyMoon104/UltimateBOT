package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_17;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.Objects;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHumanEntity;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
@SuppressWarnings({"deprecation", "rawtypes", "unchecked", "TypeParameterUnusedInFormals"})
public final class VersionedBotCraftPlayer extends CraftHumanEntity implements org.bukkit.entity.Player {

    private final ITrainingBot trainingBot;

    public VersionedBotCraftPlayer(ITrainingBot trainingBot) {
        super((CraftServer) Bukkit.getServer(), (Player) trainingBot);
        this.trainingBot = trainingBot;
    }

    private Player nativeHandle() {

        return trainingBot != null ? (Player) trainingBot : super.getHandle();
    }

    public Player getHandle() {
        return nativeHandle();
    }

    public void setHandle(Player player) {
        super.setHandle(player);
    }

    public String toString() {
        return "VersionedBotCraftPlayer{name=" + getName() + "}";
    }

    public org.bukkit.entity.Player.Spigot spigot() {
        return new org.bukkit.entity.Player.Spigot() {};
    }

    public java.util.Map<String, Object> serialize() {
        return java.util.Map.of();
    }

    public net.kyori.adventure.text.Component displayName() {
        return net.kyori.adventure.text.Component.text(getName());
    }

    public void displayName(net.kyori.adventure.text.Component arg0) {}

    public java.lang.String getDisplayName() {
        return getName();
    }

    public void setDisplayName(java.lang.String arg0) {}

    public void playerListName(net.kyori.adventure.text.Component arg0) {}

    public net.kyori.adventure.text.Component playerListName() {
        return net.kyori.adventure.text.Component.text(getName());
    }

    public net.kyori.adventure.text.Component playerListHeader() {
        return null;
    }

    public net.kyori.adventure.text.Component playerListFooter() {
        return null;
    }

    public java.lang.String getPlayerListName() {
        return getName();
    }

    public void setPlayerListName(java.lang.String arg0) {}

    public java.lang.String getPlayerListHeader() {
        return null;
    }

    public java.lang.String getPlayerListFooter() {
        return null;
    }

    public void setPlayerListHeader(java.lang.String arg0) {}

    public void setPlayerListFooter(java.lang.String arg0) {}

    public void setPlayerListHeaderFooter(java.lang.String arg0, java.lang.String arg1) {}

    public void setCompassTarget(org.bukkit.Location arg0) {}

    public org.bukkit.Location getCompassTarget() {
        return getLocation();
    }

    public java.net.InetSocketAddress getAddress() {
        return null;
    }

    public void sendRawMessage(java.lang.String arg0) {}

    public void kickPlayer(java.lang.String arg0) {}

    public void kick(net.kyori.adventure.text.Component arg0) {}

    public void chat(java.lang.String arg0) {}

    public boolean performCommand(java.lang.String arg0) {
        return false;
    }

    public boolean isOnGround() {
        return false;
    }

    public boolean isSneaking() {
        return nativeHandle().isShiftKeyDown();
    }

    public void setSneaking(boolean arg0) {
        nativeHandle().setShiftKeyDown(arg0);
    }

    public boolean isSprinting() {
        return nativeHandle().isSprinting();
    }

    public void setSprinting(boolean arg0) {
        nativeHandle().setSprinting(arg0);
    }

    public void saveData() {}

    public void loadData() {}

    public void setSleepingIgnored(boolean arg0) {}

    public boolean isSleepingIgnored() {
        return false;
    }

    public org.bukkit.Location getBedSpawnLocation() {
        return null;
    }

    public void setBedSpawnLocation(org.bukkit.Location arg0) {}

    public void setBedSpawnLocation(org.bukkit.Location arg0, boolean arg1) {}

    public void playNote(org.bukkit.Location arg0, byte arg1, byte arg2) {}

    public void playNote(org.bukkit.Location arg0, org.bukkit.Instrument arg1, org.bukkit.Note arg2) {}

    public void playSound(org.bukkit.Location arg0, org.bukkit.Sound arg1, float arg2, float arg3) {}

    public void playSound(org.bukkit.Location arg0, java.lang.String arg1, float arg2, float arg3) {}

    public void playSound(
            org.bukkit.Location arg0, org.bukkit.Sound arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4) {}

    public void playSound(
            org.bukkit.Location arg0, java.lang.String arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4) {}

    public void stopSound(org.bukkit.Sound arg0) {}

    public void stopSound(java.lang.String arg0) {}

    public void stopSound(org.bukkit.Sound arg0, org.bukkit.SoundCategory arg1) {}

    public void stopSound(java.lang.String arg0, org.bukkit.SoundCategory arg1) {}

    public void stopAllSounds() {}

    public void playEffect(org.bukkit.Location arg0, org.bukkit.Effect arg1, int arg2) {}

    public <T> void playEffect(org.bukkit.Location arg0, org.bukkit.Effect arg1, T arg2) {}

    public boolean breakBlock(org.bukkit.block.Block arg0) {
        return false;
    }

    public void sendBlockChange(org.bukkit.Location arg0, org.bukkit.Material arg1, byte arg2) {}

    public void sendBlockChange(org.bukkit.Location arg0, org.bukkit.block.data.BlockData arg1) {}

    public void sendBlockDamage(org.bukkit.Location arg0, float arg1) {}

    public boolean sendChunkChange(org.bukkit.Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
        return false;
    }

    public void sendSignChange(
            org.bukkit.Location arg0,
            java.util.List<net.kyori.adventure.text.Component> arg1,
            org.bukkit.DyeColor arg2,
            boolean arg3)
            throws java.lang.IllegalArgumentException {}

    public void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1)
            throws java.lang.IllegalArgumentException {}

    public void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1, org.bukkit.DyeColor arg2)
            throws java.lang.IllegalArgumentException {}

    public void sendSignChange(
            org.bukkit.Location arg0, java.lang.String[] arg1, org.bukkit.DyeColor arg2, boolean arg3)
            throws java.lang.IllegalArgumentException {}

    public void sendMap(org.bukkit.map.MapView arg0) {}

    public void sendActionBar(java.lang.String arg0) {}

    public void sendActionBar(char arg0, java.lang.String arg1) {}

    public void sendActionBar(net.md_5.bungee.api.chat.BaseComponent... arg0) {}

    public void setPlayerListHeaderFooter(
            net.md_5.bungee.api.chat.BaseComponent[] arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1) {}

    public void setPlayerListHeaderFooter(
            net.md_5.bungee.api.chat.BaseComponent arg0, net.md_5.bungee.api.chat.BaseComponent arg1) {}

    public void setTitleTimes(int arg0, int arg1, int arg2) {}

    public void setSubtitle(net.md_5.bungee.api.chat.BaseComponent[] arg0) {}

    public void setSubtitle(net.md_5.bungee.api.chat.BaseComponent arg0) {}

    public void showTitle(net.md_5.bungee.api.chat.BaseComponent[] arg0) {}

    public void showTitle(net.md_5.bungee.api.chat.BaseComponent arg0) {}

    public void showTitle(
            net.md_5.bungee.api.chat.BaseComponent[] arg0,
            net.md_5.bungee.api.chat.BaseComponent[] arg1,
            int arg2,
            int arg3,
            int arg4) {}

    public void showTitle(
            net.md_5.bungee.api.chat.BaseComponent arg0,
            net.md_5.bungee.api.chat.BaseComponent arg1,
            int arg2,
            int arg3,
            int arg4) {}

    public void sendTitle(com.destroystokyo.paper.Title arg0) {}

    public void updateTitle(com.destroystokyo.paper.Title arg0) {}

    public void hideTitle() {}

    public void updateInventory() {}

    public void setPlayerTime(long arg0, boolean arg1) {}

    public long getPlayerTime() {
        return 0;
    }

    public long getPlayerTimeOffset() {
        return 0;
    }

    public boolean isPlayerTimeRelative() {
        return false;
    }

    public void resetPlayerTime() {}

    public void setPlayerWeather(org.bukkit.WeatherType arg0) {}

    public org.bukkit.WeatherType getPlayerWeather() {
        return null;
    }

    public void resetPlayerWeather() {}

    public void giveExp(int arg0) {}

    public int applyMending(int arg0) {
        return 0;
    }

    public void giveExpLevels(int arg0) {}

    public float getExp() {
        return 0;
    }

    public void setExp(float arg0) {}

    public int getLevel() {
        return 0;
    }

    public void setLevel(int arg0) {}

    public int getTotalExperience() {
        return 0;
    }

    public void setTotalExperience(int arg0) {}

    public void sendExperienceChange(float arg0) {}

    public void sendExperienceChange(float arg0, int arg1) {}

    public boolean getAllowFlight() {
        return false;
    }

    public void setAllowFlight(boolean arg0) {}

    public void hidePlayer(org.bukkit.entity.Player arg0) {}

    public void hidePlayer(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Player arg1) {}

    public void showPlayer(org.bukkit.entity.Player arg0) {}

    public void showPlayer(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Player arg1) {}

    public boolean canSee(org.bukkit.entity.Player arg0) {
        return false;
    }

    public boolean isFlying() {
        return false;
    }

    public void setFlying(boolean arg0) {}

    public void setFlySpeed(float arg0) throws java.lang.IllegalArgumentException {}

    public void setWalkSpeed(float arg0) throws java.lang.IllegalArgumentException {}

    public float getFlySpeed() {
        return 0;
    }

    public float getWalkSpeed() {
        return 0.2f;
    }

    public void setTexturePack(java.lang.String arg0) {}

    public void setResourcePack(java.lang.String arg0) {}

    public void setResourcePack(java.lang.String arg0, byte[] arg1) {}

    public org.bukkit.scoreboard.Scoreboard getScoreboard() {
        return Objects.requireNonNull(Bukkit.getScoreboardManager(), "scoreboard manager")
                .getMainScoreboard();
    }

    public void setScoreboard(org.bukkit.scoreboard.Scoreboard arg0)
            throws java.lang.IllegalArgumentException, java.lang.IllegalStateException {}

    public boolean isHealthScaled() {
        return false;
    }

    public void setHealthScaled(boolean arg0) {}

    public void setHealthScale(double arg0) throws java.lang.IllegalArgumentException {}

    public double getHealthScale() {
        return 20.0;
    }

    public org.bukkit.entity.Entity getSpectatorTarget() {
        return null;
    }

    public void setSpectatorTarget(org.bukkit.entity.Entity arg0) {}

    public void sendTitle(java.lang.String arg0, java.lang.String arg1) {}

    public void sendTitle(java.lang.String arg0, java.lang.String arg1, int arg2, int arg3, int arg4) {}

    public void resetTitle() {}

    public void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2) {}

    public void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4) {}

    public <T> void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, T arg3) {}

    public <T> void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {}

    public void spawnParticle(
            org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5) {}

    public void spawnParticle(
            org.bukkit.Particle arg0,
            double arg1,
            double arg2,
            double arg3,
            int arg4,
            double arg5,
            double arg6,
            double arg7) {}

    public <T> void spawnParticle(
            org.bukkit.Particle arg0,
            org.bukkit.Location arg1,
            int arg2,
            double arg3,
            double arg4,
            double arg5,
            T arg6) {}

    public <T> void spawnParticle(
            org.bukkit.Particle arg0,
            double arg1,
            double arg2,
            double arg3,
            int arg4,
            double arg5,
            double arg6,
            double arg7,
            T arg8) {}

    public void spawnParticle(
            org.bukkit.Particle arg0,
            org.bukkit.Location arg1,
            int arg2,
            double arg3,
            double arg4,
            double arg5,
            double arg6) {}

    public void spawnParticle(
            org.bukkit.Particle arg0,
            double arg1,
            double arg2,
            double arg3,
            int arg4,
            double arg5,
            double arg6,
            double arg7,
            double arg8) {}

    public <T> void spawnParticle(
            org.bukkit.Particle arg0,
            org.bukkit.Location arg1,
            int arg2,
            double arg3,
            double arg4,
            double arg5,
            double arg6,
            T arg7) {}

    public <T> void spawnParticle(
            org.bukkit.Particle arg0,
            double arg1,
            double arg2,
            double arg3,
            int arg4,
            double arg5,
            double arg6,
            double arg7,
            double arg8,
            T arg9) {}

    public org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement arg0) {
        return null;
    }

    public int getClientViewDistance() {
        return 0;
    }

    public java.util.Locale locale() {
        return java.util.Locale.US;
    }

    public int getPing() {
        return 0;
    }

    public java.lang.String getLocale() {
        return "en_US";
    }

    public boolean getAffectsSpawning() {
        return false;
    }

    public void setAffectsSpawning(boolean arg0) {}

    public int getViewDistance() {
        return 0;
    }

    public void setViewDistance(int arg0) {}

    public int getNoTickViewDistance() {
        return 0;
    }

    public void setNoTickViewDistance(int arg0) {}

    public int getSendViewDistance() {
        return 0;
    }

    public void setSendViewDistance(int arg0) {}

    public void updateCommands() {}

    public void openBook(org.bukkit.inventory.ItemStack arg0) {}

    public void setResourcePack(java.lang.String arg0, java.lang.String arg1) {}

    public void setResourcePack(java.lang.String arg0, java.lang.String arg1, boolean arg2) {}

    public void setResourcePack(
            java.lang.String arg0, java.lang.String arg1, boolean arg2, net.kyori.adventure.text.Component arg3) {}

    public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        return null;
    }

    public java.lang.String getResourcePackHash() {
        return null;
    }

    public boolean hasResourcePack() {
        return false;
    }

    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return null;
    }

    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0) {}

    public float getCooldownPeriod() {
        return 0;
    }

    public float getCooledAttackStrength(float arg0) {
        return 0;
    }

    public void resetCooldown() {}

    public <T> T getClientOption(com.destroystokyo.paper.ClientOption<T> arg0) {
        return null;
    }

    public org.bukkit.entity.Firework boostElytra(org.bukkit.inventory.ItemStack arg0) {
        return null;
    }

    public void sendOpLevel(byte arg0) {}

    public java.lang.String getClientBrandName() {
        return null;
    }

    public boolean isConversing() {
        return false;
    }

    public void acceptConversationInput(java.lang.String arg0) {}

    public boolean beginConversation(org.bukkit.conversations.Conversation arg0) {
        return false;
    }

    public void abandonConversation(org.bukkit.conversations.Conversation arg0) {}

    public void abandonConversation(
            org.bukkit.conversations.Conversation arg0, org.bukkit.conversations.ConversationAbandonedEvent arg1) {}

    public void sendRawMessage(
            @org.jetbrains.annotations.Nullable java.util.UUID arg0,
            @org.jetbrains.annotations.NotNull java.lang.String arg1) {}

    public boolean isOnline() {
        return false;
    }

    public java.lang.String getName() {
        return nativeHandle().getGameProfile().getName();
    }

    public java.util.UUID getUniqueId() {

        return nativeHandle().getGameProfile().getId();
    }

    public boolean isBanned() {
        return false;
    }

    public boolean isWhitelisted() {
        return false;
    }

    public void setWhitelisted(boolean arg0) {}

    public org.bukkit.entity.Player getPlayer() {
        return null;
    }

    public long getFirstPlayed() {
        return 0;
    }

    public long getLastPlayed() {
        return 0;
    }

    public boolean hasPlayedBefore() {
        return false;
    }

    public long getLastLogin() {
        return 0;
    }

    public long getLastSeen() {
        return 0;
    }

    public void incrementStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException {}

    public void decrementStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException {}

    public void incrementStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException {}

    public void decrementStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException {}

    public void setStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException {}

    public int getStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException {
        return 0;
    }

    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1)
            throws java.lang.IllegalArgumentException {}

    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1)
            throws java.lang.IllegalArgumentException {}

    public int getStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1)
            throws java.lang.IllegalArgumentException {
        return 0;
    }

    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    public void setStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1)
            throws java.lang.IllegalArgumentException {}

    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1)
            throws java.lang.IllegalArgumentException {}

    public int getStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1)
            throws java.lang.IllegalArgumentException {
        return 0;
    }

    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2) {}

    public void setStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2) {}

    public void sendPluginMessage(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, byte[] arg2) {}

    public java.util.Set<java.lang.String> getListeningPluginChannels() {
        return java.util.Set.of();
    }

    public int getProtocolVersion() {
        return 0;
    }

    public java.net.InetSocketAddress getVirtualHost() {
        return null;
    }
}
