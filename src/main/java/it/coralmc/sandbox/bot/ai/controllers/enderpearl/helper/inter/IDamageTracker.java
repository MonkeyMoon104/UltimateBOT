package it.coralmc.sandbox.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IDamageTracker {

    void onDamageReceived(Player bot);

    void tick();

    boolean wasRecentlyDamaged();

    int getDamageComboCount();

    long getComboStartTime();

    void resetDamageState();
}