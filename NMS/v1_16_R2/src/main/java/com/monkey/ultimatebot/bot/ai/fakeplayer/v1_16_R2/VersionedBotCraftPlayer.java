package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R2;

import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_16_R2;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullUnmarked;

/**
 * 1.16 {@link CraftPlayer#attack} delegates to {@code EntityLiving.attackEntity}, which only swings
 * and returns false. Crystals need {@code EntityHuman.attack} — same as 1.16.5.
 */
@NullUnmarked
public final class VersionedBotCraftPlayer extends CraftPlayer {

    public VersionedBotCraftPlayer(TrainingBot_v1_16_R2 bot) {
        super((CraftServer) Bukkit.getServer(), bot);
    }

    @Override
    public void attack(Entity entity) {
        if (entity == null) {
            return;
        }
        getHandle().attack(((CraftEntity) entity).getHandle());
    }
}
