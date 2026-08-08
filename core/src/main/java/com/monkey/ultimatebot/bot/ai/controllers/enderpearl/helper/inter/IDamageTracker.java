package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IDamageTracker {

    void onDamageReceived(ITrainingBot bot);

    void tick();

    boolean wasRecentlyDamaged();

    int getDamageComboCount();

    long getComboStartTime();

    void resetDamageState();
}
