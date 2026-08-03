package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;

public interface CombatModeStrategy {
    CombatMode mode();

    void enter(CombatModeContext context);

    void tick(CombatModeContext context, LivingEntity target);

    void exit(CombatModeContext context);

    default boolean controlsNavigation(CombatModeContext context, LivingEntity target) {
        return false;
    }
}
