package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_14_R1;

import com.monkey.ultimatebot.bot.ai.TrainingBot_v1_14_R1;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.jspecify.annotations.NullUnmarked;

/**
 * Dedicated CraftPlayer for the fake EntityPlayer. 1.14 has no {@code CraftLivingEntity#attack};
 * crystal hits go through {@code PlayerAttackAccess} → NMS {@code EntityHuman#attack}.
 */
@NullUnmarked
public final class VersionedBotCraftPlayer extends CraftPlayer {

    public VersionedBotCraftPlayer(TrainingBot_v1_14_R1 bot) {
        super((CraftServer) Bukkit.getServer(), bot);
    }
}
