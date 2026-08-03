package com.monkey.ultimatebot.combat.mode;

import java.util.Objects;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

final class CombatBlockBreakSequence {
    private static final int BREAK_TICKS = 8;

    private @Nullable Location block;
    private int progressTicks;

    boolean tick(CombatModeContext context, Location candidate, int toolSlot) {
        Objects.requireNonNull(context, "context");
        Location targetBlock =
                Objects.requireNonNull(candidate, "candidate").getBlock().getLocation();
        if (!sameBlock(block, targetBlock)) {
            reset(context);
            block = targetBlock;
        }
        context.motion().stop();
        context.inventory().switchToSlot(toolSlot);
        progressTicks++;
        if (progressTicks == 1 || progressTicks % 3 == 0) {
            context.actions().swingMainHand();
        }
        context.showBlockBreakProgress(targetBlock, Math.min(0.95F, progressTicks / (float) BREAK_TICKS));
        if (progressTicks < BREAK_TICKS) {
            return true;
        }
        context.breakCombatBlock(targetBlock, toolSlot);
        reset(context);
        return true;
    }

    void reset(CombatModeContext context) {
        if (block != null) {
            context.showBlockBreakProgress(block, 0.0F);
        }
        block = null;
        progressTicks = 0;
    }

    private static boolean sameBlock(@Nullable Location first, Location second) {
        return first != null
                && Objects.equals(first.getWorld(), second.getWorld())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }
}
