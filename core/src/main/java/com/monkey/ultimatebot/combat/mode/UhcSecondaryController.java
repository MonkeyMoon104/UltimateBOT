package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;

final class UhcSecondaryController {
    private static final int LAVA_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int WEB_SLOT = BotInventoryController.ANCHOR_SLOT;

    private final Set<WebTrapPlanner.Position> placedWebs = new HashSet<>();
    private Action action = Action.WEB;
    private int targetWebCount;

    void start(CombatModeContext context, LivingEntity target) {
        reset();
        CobwebCombatAwareness.Containment containment = CobwebCombatAwareness.inspect(target);
        if (containment.inside() && !containment.nearlyExiting()) {
            action = Action.NONE;
            return;
        }
        action = containment.nearlyExiting() || context.random().nextDouble() < 0.82D ? Action.WEB : Action.LAVA;
        if (action == Action.WEB) {
            context.inventory().switchToSlot(WEB_SLOT);
            targetWebCount = 4 + context.random().nextInt(3);
        } else {
            placeLava(context, target);
        }
    }

    boolean tick(CombatModeContext context, LivingEntity target, int phaseTicks) {
        CobwebCombatAwareness.Containment containment = CobwebCombatAwareness.inspect(target);
        if (action == Action.WEB
                && containment.inside()
                && !containment.nearlyExiting()
                && placedWebs.size() >= targetWebCount) {
            return true;
        }
        if (action == Action.WEB && phaseTicks % 2 == 1 && phaseTicks <= 15 && placedWebs.size() < targetWebCount) {
            placeNextWeb(context, target);
        }
        return action == Action.NONE
                || phaseTicks >= (action == Action.WEB ? 17 : 7)
                || placedWebs.size() >= targetWebCount;
    }

    void reset() {
        placedWebs.clear();
        targetWebCount = 0;
    }

    private void placeNextWeb(CombatModeContext context, LivingEntity target) {
        if (!(target.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity livingTarget)) {
            return;
        }
        Location targetLocation = Objects.requireNonNull(livingTarget.getLocation(), "target location");
        for (Location candidate : WebTrapPlanner.plan(
                targetLocation,
                livingTarget.getVelocity(),
                livingTarget.isOnGround(),
                targetLocation.getDirection(),
                context.random())) {
            WebTrapPlanner.Position position = WebTrapPlanner.Position.from(candidate);
            if (placedWebs.contains(position) || !context.canPlaceCombatBlock(candidate, Material.COBWEB)) {
                continue;
            }
            if (context.placeCombatBlock(candidate, Material.COBWEB, WEB_SLOT)) {
                placedWebs.add(position);
                context.actions().swingMainHand();
                return;
            }
        }
    }

    private void placeLava(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(LAVA_SLOT);
        if (!context.inventory().consumeItem(LAVA_SLOT)) {
            return;
        }
        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
        target.getBukkitEntity()
                .getWorld()
                .spawnParticle(
                        org.bukkit.Particle.LAVA,
                        Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location"),
                        12,
                        0.35D,
                        0.15D,
                        0.35D,
                        0.02D);
    }

    private enum Action {
        NONE,
        WEB,
        LAVA
    }
}
