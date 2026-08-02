package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;

abstract class AbstractCombatModeStrategy implements CombatModeStrategy {
    private final CombatMode mode;
    private final ModeKit kit;
    private long tick;
    private long nextSpecialActionTick;

    AbstractCombatModeStrategy(CombatMode mode, ModeKit kit) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.kit = Objects.requireNonNull(kit, "kit");
    }

    @Override
    public final CombatMode mode() {
        return mode;
    }

    @Override
    public void enter(CombatModeContext context) {
        context.applyKit(kit);
        tick = 0L;
        nextSpecialActionTick = 0L;
    }

    @Override
    public final void tick(CombatModeContext context, LivingEntity target) {
        tick++;
        context.entities().prune();
        execute(context, target);
    }

    @Override
    public void exit(CombatModeContext context) {
        if (context.bot().isUsingItem()) {
            context.bot().releaseUsingItem();
        }
        context.anchor().disable();
        context.crystal().setEnabled(false);
        context.motion().setSwimming(false);
        context.clearTransientState();
    }

    protected abstract void execute(CombatModeContext context, LivingEntity target);

    protected final boolean specialActionReady() {
        return tick >= nextSpecialActionTick;
    }

    protected final long currentTick() {
        return tick;
    }

    protected final void delaySpecialAction(CombatModeContext context) {
        nextSpecialActionTick = tick + context.tuning().specialActionCooldownTicks();
    }

    protected final void meleeOrMove(CombatModeContext context, LivingEntity target, int weaponSlot) {
        double distance = context.motion().distanceTo(target);
        if (distance <= context.tuning().attackRange()) {
            context.actions().attack(target, weaponSlot);
        }
        if (distance > 1.7D) {
            context.motion().approach(target, 1.35D);
        } else if (context.random().nextDouble() < context.tuning().strafeStrength()) {
            context.motion().strafe(target, 1.2D + context.tuning().strafeStrength());
        }
    }
}
