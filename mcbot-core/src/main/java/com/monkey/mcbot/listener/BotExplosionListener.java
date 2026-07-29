package com.monkey.mcbot.listener;

import com.monkey.mcbot.bot.ai.controllers.combat.BotExplosionContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/** Keeps bot explosions active while independently controlling terrain damage. */
public final class BotExplosionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        protectBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        protectBlocks(event.blockList());
    }

    private void protectBlocks(java.util.List<org.bukkit.block.Block> blocks) {
        if (BotExplosionContext.isBlockDamageSuppressed()) {
            blocks.clear();
        }
    }
}
