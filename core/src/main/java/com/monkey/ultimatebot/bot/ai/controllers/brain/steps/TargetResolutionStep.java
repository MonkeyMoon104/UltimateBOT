package com.monkey.ultimatebot.bot.ai.controllers.brain.steps;

import com.monkey.ultimatebot.access.entity.EntityCoordsAccess;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.services.TargetingService;
import com.monkey.ultimatebot.common.model.bot.BotTargetMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class TargetResolutionStep {

    @FunctionalInterface
    public interface TargetAvailability {
        boolean isAvailable(@Nullable Player player);
    }

    private final ITrainingBot bot;
    private final BotOptions botOptions;
    private final TargetingService targetingService;
    private final double eventTargetRange;
    private final double allyRange;
    private final double teamAllyRange;
    private final boolean follow;
    private final boolean combat;
    private final @Nullable Player targetPlayer;
    private final TargetAvailability targetAvailability;

    public TargetResolutionStep(
            ITrainingBot bot,
            BotOptions botOptions,
            TargetingService targetingService,
            double eventTargetRange,
            double allyRange,
            double teamAllyRange,
            boolean follow,
            boolean combat,
            @Nullable Player targetPlayer,
            TargetAvailability targetAvailability) {
        this.bot = bot;
        this.botOptions = botOptions;
        this.targetingService = targetingService;
        this.eventTargetRange = eventTargetRange;
        this.allyRange = allyRange;
        this.teamAllyRange = teamAllyRange;
        this.follow = follow;
        this.combat = combat;
        this.targetPlayer = targetPlayer;
        this.targetAvailability = targetAvailability;
    }

    public @Nullable LivingEntity resolve() {
        BotTargetMode mode = botOptions.getTargetMode();

        Player player = mode.allowsPlayers() && targetAvailability.isAvailable(targetPlayer) ? targetPlayer : null;

        LivingEntity mob = null;
        if (mode.allowsMobs()) {
            mob = targetingService.findClosestMob(bot, getMobTargetRange());
            if (mob != null && (!mob.isValid() || mob.isDead())) {
                mob = null;
            }
        }

        if (player == null) {
            return mob;
        }
        if (mob == null) {
            return player;
        }

        if (follow && !combat) {
            return player;
        }
        double mobDist = distanceSquaredFromBot(mob);
        double playerDist = distanceSquaredFromBot(player);

        if (mobDist + 1.0D < playerDist) {
            return mob;
        }
        return player;
    }

    private double getMobTargetRange() {
        switch (botOptions.getBotType()) {
            case EVENT:
                return eventTargetRange;
            case ALLY:
                return allyRange;
            case TEAM_ALLY:
                return teamAllyRange;
            default:
                return botOptions.getAutoTargetRange();
        }
    }

    private double distanceSquaredFromBot(LivingEntity entity) {
        double dx = EntityCoordsAccess.getX(entity) - EntityCoordsAccess.getX(bot.asBukkitPlayer());
        double dy = EntityCoordsAccess.getY(entity) - EntityCoordsAccess.getY(bot.asBukkitPlayer());
        double dz = EntityCoordsAccess.getZ(entity) - EntityCoordsAccess.getZ(bot.asBukkitPlayer());
        return dx * dx + dy * dy + dz * dz;
    }
}
