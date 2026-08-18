package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_17_1;

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

    @Override
    public Player getHandle() {
        return nativeHandle();
    }

    @Override
    public void setHandle(Player player) {
        super.setHandle(player);
    }

    @Override
    public String toString() {
        return "VersionedBotCraftPlayer{name=" + getName() + "}";
    }

    @Override
    public org.bukkit.entity.Player.Spigot spigot() {
        return new org.bukkit.entity.Player.Spigot() {};
    }

    @Override
    public java.util.Map<String, Object> serialize() {
        return java.util.Map.of();
    }

    @Override
    public net.kyori.adventure.text.Component displayName() {
        return net.kyori.adventure.text.Component.text(getName());
    }

    @Override
    public void displayName(net.kyori.adventure.text.Component arg0) {}

    @Override
    public java.lang.String getDisplayName() {
        return getName();
    }

    @Override
    public void setDisplayName(java.lang.String arg0) {}

    @Override
    public void playerListName(net.kyori.adventure.text.Component arg0) {}

    @Override
    public net.kyori.adventure.text.Component playerListName() {
        return net.kyori.adventure.text.Component.text(getName());
    }

    @Override
    public net.kyori.adventure.text.Component playerListHeader() {
        return null;
    }

    @Override
    public net.kyori.adventure.text.Component playerListFooter() {
        return null;
    }

    @Override
    public java.lang.String getPlayerListName() {
        return getName();
    }

    @Override
    public void setPlayerListName(java.lang.String arg0) {}

    @Override
    public java.lang.String getPlayerListHeader() {
        return null;
    }

    @Override
    public java.lang.String getPlayerListFooter() {
        return null;
    }

    @Override
    public void setPlayerListHeader(java.lang.String arg0) {}

    @Override
    public void setPlayerListFooter(java.lang.String arg0) {}

    @Override
    public void setPlayerListHeaderFooter(java.lang.String arg0, java.lang.String arg1) {}

    @Override
    public void setCompassTarget(org.bukkit.Location arg0) {}

    @Override
    public org.bukkit.Location getCompassTarget() {
        return getLocation();
    }

    @Override
    public java.net.InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public void sendRawMessage(java.lang.String arg0) {}

    @Override
    public void kickPlayer(java.lang.String arg0) {}

    @Override
    public void kick(net.kyori.adventure.text.Component arg0) {}

    @Override
    public void kick(net.kyori.adventure.text.Component arg0, org.bukkit.event.player.PlayerKickEvent.Cause arg1) {}

    @Override
    public void chat(java.lang.String arg0) {}

    @Override
    public boolean performCommand(java.lang.String arg0) {
        return false;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isSneaking() {
        return nativeHandle().isShiftKeyDown();
    }

    @Override
    public void setSneaking(boolean arg0) {
        nativeHandle().setShiftKeyDown(arg0);
    }

    @Override
    public boolean isSprinting() {
        return nativeHandle().isSprinting();
    }

    @Override
    public void setSprinting(boolean arg0) {
        nativeHandle().setSprinting(arg0);
    }

    @Override
    public void saveData() {}

    @Override
    public void loadData() {}

    @Override
    public void setSleepingIgnored(boolean arg0) {}

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Override
    public org.bukkit.Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public void setBedSpawnLocation(org.bukkit.Location arg0) {}

    @Override
    public void setBedSpawnLocation(org.bukkit.Location arg0, boolean arg1) {}

    @Override
    public void playNote(org.bukkit.Location arg0, byte arg1, byte arg2) {}

    @Override
    public void playNote(org.bukkit.Location arg0, org.bukkit.Instrument arg1, org.bukkit.Note arg2) {}

    @Override
    public void playSound(org.bukkit.Location arg0, org.bukkit.Sound arg1, float arg2, float arg3) {}

    @Override
    public void playSound(org.bukkit.Location arg0, java.lang.String arg1, float arg2, float arg3) {}

    @Override
    public void playSound(
            org.bukkit.Location arg0, org.bukkit.Sound arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4) {}

    @Override
    public void playSound(
            org.bukkit.Location arg0, java.lang.String arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4) {}

    @Override
    public void stopSound(org.bukkit.Sound arg0) {}

    @Override
    public void stopSound(java.lang.String arg0) {}

    @Override
    public void stopSound(org.bukkit.Sound arg0, org.bukkit.SoundCategory arg1) {}

    @Override
    public void stopSound(java.lang.String arg0, org.bukkit.SoundCategory arg1) {}

    @Override
    public void stopAllSounds() {}

    @Override
    public void playEffect(org.bukkit.Location arg0, org.bukkit.Effect arg1, int arg2) {}

    @Override
    public <T> void playEffect(org.bukkit.Location arg0, org.bukkit.Effect arg1, T arg2) {}

    @Override
    public boolean breakBlock(org.bukkit.block.Block arg0) {
        return false;
    }

    @Override
    public void sendBlockChange(org.bukkit.Location arg0, org.bukkit.Material arg1, byte arg2) {}

    @Override
    public void sendBlockChange(org.bukkit.Location arg0, org.bukkit.block.data.BlockData arg1) {}

    @Override
    public void sendBlockDamage(org.bukkit.Location arg0, float arg1) {}

    @Override
    public boolean sendChunkChange(org.bukkit.Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
        return false;
    }

    @Override
    public void sendSignChange(
            org.bukkit.Location arg0,
            java.util.List<net.kyori.adventure.text.Component> arg1,
            org.bukkit.DyeColor arg2,
            boolean arg3)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1, org.bukkit.DyeColor arg2)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void sendSignChange(
            org.bukkit.Location arg0, java.lang.String[] arg1, org.bukkit.DyeColor arg2, boolean arg3)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void sendMap(org.bukkit.map.MapView arg0) {}

    @Override
    public void sendActionBar(java.lang.String arg0) {}

    @Override
    public void sendActionBar(char arg0, java.lang.String arg1) {}

    @Override
    public void sendActionBar(net.md_5.bungee.api.chat.BaseComponent... arg0) {}

    @Override
    public void setPlayerListHeaderFooter(
            net.md_5.bungee.api.chat.BaseComponent[] arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1) {}

    @Override
    public void setPlayerListHeaderFooter(
            net.md_5.bungee.api.chat.BaseComponent arg0, net.md_5.bungee.api.chat.BaseComponent arg1) {}

    @Override
    public void setTitleTimes(int arg0, int arg1, int arg2) {}

    @Override
    public void setSubtitle(net.md_5.bungee.api.chat.BaseComponent[] arg0) {}

    @Override
    public void setSubtitle(net.md_5.bungee.api.chat.BaseComponent arg0) {}

    @Override
    public void showTitle(net.md_5.bungee.api.chat.BaseComponent[] arg0) {}

    @Override
    public void showTitle(net.md_5.bungee.api.chat.BaseComponent arg0) {}

    @Override
    public void showTitle(
            net.md_5.bungee.api.chat.BaseComponent[] arg0,
            net.md_5.bungee.api.chat.BaseComponent[] arg1,
            int arg2,
            int arg3,
            int arg4) {}

    @Override
    public void showTitle(
            net.md_5.bungee.api.chat.BaseComponent arg0,
            net.md_5.bungee.api.chat.BaseComponent arg1,
            int arg2,
            int arg3,
            int arg4) {}

    @Override
    public void sendTitle(com.destroystokyo.paper.Title arg0) {}

    @Override
    public void updateTitle(com.destroystokyo.paper.Title arg0) {}

    @Override
    public void hideTitle() {}

    @Override
    public void updateInventory() {}

    @Override
    public void setPlayerTime(long arg0, boolean arg1) {}

    @Override
    public long getPlayerTime() {
        return 0;
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
    public void resetPlayerTime() {}

    @Override
    public void setPlayerWeather(org.bukkit.WeatherType arg0) {}

    @Override
    public org.bukkit.WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {}

    @Override
    public void giveExp(int arg0, boolean arg1) {}

    @Override
    public int applyMending(int arg0) {
        return 0;
    }

    @Override
    public void giveExpLevels(int arg0) {}

    @Override
    public float getExp() {
        return 0;
    }

    @Override
    public void setExp(float arg0) {}

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int arg0) {}

    @Override
    public int getTotalExperience() {
        return 0;
    }

    @Override
    public void setTotalExperience(int arg0) {}

    @Override
    public void sendExperienceChange(float arg0) {}

    @Override
    public void sendExperienceChange(float arg0, int arg1) {}

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public void setAllowFlight(boolean arg0) {}

    @Override
    public void hidePlayer(org.bukkit.entity.Player arg0) {}

    @Override
    public void hidePlayer(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Player arg1) {}

    @Override
    public void showPlayer(org.bukkit.entity.Player arg0) {}

    @Override
    public void showPlayer(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Player arg1) {}

    @Override
    public boolean canSee(org.bukkit.entity.Player arg0) {
        return false;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public void setFlying(boolean arg0) {}

    @Override
    public void setFlySpeed(float arg0) throws java.lang.IllegalArgumentException {}

    @Override
    public void setWalkSpeed(float arg0) throws java.lang.IllegalArgumentException {}

    @Override
    public float getFlySpeed() {
        return 0;
    }

    @Override
    public float getWalkSpeed() {
        return 0.2f;
    }

    @Override
    public void setTexturePack(java.lang.String arg0) {}

    @Override
    public void setResourcePack(java.lang.String arg0) {}

    @Override
    public void setResourcePack(java.lang.String arg0, byte[] arg1) {}

    @Override
    public org.bukkit.scoreboard.Scoreboard getScoreboard() {
        return Objects.requireNonNull(Bukkit.getScoreboardManager(), "scoreboard manager")
                .getMainScoreboard();
    }

    @Override
    public void setScoreboard(org.bukkit.scoreboard.Scoreboard arg0)
            throws java.lang.IllegalArgumentException, java.lang.IllegalStateException {}

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean arg0) {}

    @Override
    public void setHealthScale(double arg0) throws java.lang.IllegalArgumentException {}

    @Override
    public double getHealthScale() {
        return 20.0;
    }

    @Override
    public org.bukkit.entity.Entity getSpectatorTarget() {
        return null;
    }

    @Override
    public void setSpectatorTarget(org.bukkit.entity.Entity arg0) {}

    @Override
    public void sendTitle(java.lang.String arg0, java.lang.String arg1) {}

    @Override
    public void sendTitle(java.lang.String arg0, java.lang.String arg1, int arg2, int arg3, int arg4) {}

    @Override
    public void resetTitle() {}

    @Override
    public void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2) {}

    @Override
    public void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4) {}

    @Override
    public <T> void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, T arg3) {}

    @Override
    public <T> void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {}

    @Override
    public void spawnParticle(
            org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5) {}

    @Override
    public void spawnParticle(
            org.bukkit.Particle arg0,
            double arg1,
            double arg2,
            double arg3,
            int arg4,
            double arg5,
            double arg6,
            double arg7) {}

    @Override
    public <T> void spawnParticle(
            org.bukkit.Particle arg0,
            org.bukkit.Location arg1,
            int arg2,
            double arg3,
            double arg4,
            double arg5,
            T arg6) {}

    @Override
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

    @Override
    public void spawnParticle(
            org.bukkit.Particle arg0,
            org.bukkit.Location arg1,
            int arg2,
            double arg3,
            double arg4,
            double arg5,
            double arg6) {}

    @Override
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

    @Override
    public <T> void spawnParticle(
            org.bukkit.Particle arg0,
            org.bukkit.Location arg1,
            int arg2,
            double arg3,
            double arg4,
            double arg5,
            double arg6,
            T arg7) {}

    @Override
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

    @Override
    public org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement arg0) {
        return null;
    }

    @Override
    public int getClientViewDistance() {
        return 0;
    }

    @Override
    public java.util.Locale locale() {
        return java.util.Locale.US;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    public java.lang.String getLocale() {
        return "en_US";
    }

    @Override
    public boolean getAffectsSpawning() {
        return false;
    }

    @Override
    public void setAffectsSpawning(boolean arg0) {}

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public void setViewDistance(int arg0) {}

    @Override
    public int getNoTickViewDistance() {
        return 0;
    }

    @Override
    public void setNoTickViewDistance(int arg0) {}

    @Override
    public int getSendViewDistance() {
        return 0;
    }

    @Override
    public void setSendViewDistance(int arg0) {}

    @Override
    public void updateCommands() {}

    @Override
    public void openBook(org.bukkit.inventory.ItemStack arg0) {}

    @Override
    public void setResourcePack(java.lang.String arg0, java.lang.String arg1) {}

    @Override
    public void setResourcePack(java.lang.String arg0, java.lang.String arg1, boolean arg2) {}

    @Override
    public void setResourcePack(
            java.lang.String arg0, java.lang.String arg1, boolean arg2, net.kyori.adventure.text.Component arg3) {}

    @Override
    public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        return null;
    }

    @Override
    public java.lang.String getResourcePackHash() {
        return null;
    }

    @Override
    public boolean hasResourcePack() {
        return false;
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return null;
    }

    @Override
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0) {}

    @Override
    public float getCooldownPeriod() {
        return 0;
    }

    @Override
    public float getCooledAttackStrength(float arg0) {
        return 0;
    }

    @Override
    public void resetCooldown() {}

    @Override
    public <T> T getClientOption(com.destroystokyo.paper.ClientOption<T> arg0) {
        return null;
    }

    @Override
    public org.bukkit.entity.Firework boostElytra(org.bukkit.inventory.ItemStack arg0) {
        return null;
    }

    @Override
    public void sendOpLevel(byte arg0) {}

    @Override
    public java.lang.String getClientBrandName() {
        return null;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(java.lang.String arg0) {}

    @Override
    public boolean beginConversation(org.bukkit.conversations.Conversation arg0) {
        return false;
    }

    @Override
    public void abandonConversation(org.bukkit.conversations.Conversation arg0) {}

    @Override
    public void abandonConversation(
            org.bukkit.conversations.Conversation arg0, org.bukkit.conversations.ConversationAbandonedEvent arg1) {}

    @Override
    public void sendRawMessage(
            @org.jetbrains.annotations.Nullable java.util.UUID arg0,
            @org.jetbrains.annotations.NotNull java.lang.String arg1) {}

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public java.lang.String getName() {
        return nativeHandle().getGameProfile().getName();
    }

    @Override
    public java.util.UUID getUniqueId() {
        return nativeHandle().getUUID();
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean arg0) {}

    @Override
    public org.bukkit.entity.Player getPlayer() {
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
    public long getLastLogin() {
        return 0;
    }

    @Override
    public long getLastSeen() {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException {}

    @Override
    public void decrementStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException {}

    @Override
    public void incrementStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException {}

    @Override
    public void decrementStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException {}

    @Override
    public void setStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException {}

    @Override
    public int getStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1)
            throws java.lang.IllegalArgumentException {}

    @Override
    public int getStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1)
            throws java.lang.IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void setStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1)
            throws java.lang.IllegalArgumentException {}

    @Override
    public int getStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1)
            throws java.lang.IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2)
            throws java.lang.IllegalArgumentException {}

    @Override
    public void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2) {}

    @Override
    public void setStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2) {}

    @Override
    public void sendPluginMessage(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, byte[] arg2) {}

    @Override
    public java.util.Set<java.lang.String> getListeningPluginChannels() {
        return java.util.Set.of();
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public java.net.InetSocketAddress getVirtualHost() {
        return null;
    }
}
