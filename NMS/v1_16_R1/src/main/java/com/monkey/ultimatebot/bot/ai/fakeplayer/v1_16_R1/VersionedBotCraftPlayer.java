package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R1;

import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_16_R1;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public final class VersionedBotCraftPlayer extends CraftPlayer {

    public VersionedBotCraftPlayer(TrainingBot_v1_16_R1 bot) {
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
