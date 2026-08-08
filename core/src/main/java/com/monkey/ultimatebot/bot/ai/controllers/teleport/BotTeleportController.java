package com.monkey.ultimatebot.bot.ai.controllers.teleport;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.BasicTeleportValidator;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.BesideTargetStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.SafeTeleportStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class BotTeleportController {

    private final ITrainingBot bot;
    private final ITeleportValidator validator;

    private boolean teleporting = false;
    private org.bukkit.entity.@Nullable Player currentTarget;

    private int suffocationCount = 0;
    private long lastSuffocationTime = 0;
    private static final long SUFFOCATION_RESET_TIME = 5000;
    private static final int MAX_SUFFOCATION_BEFORE_TELEPORT = 2;

    public BotTeleportController(ITrainingBot bot) {
        this.bot = bot;
        this.validator = new BasicTeleportValidator();
    }

    public boolean teleportTo(Vector pos) {
        if (teleporting || pos == null) return false;
        teleporting = true;
        Location destination = new Location(bot.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        bot.asBukkitPlayer().teleport(destination);
        teleporting = false;
        return true;
    }

    public boolean teleportBeside(Player target) {
        if (target == null) return false;

        ITeleportStrategy strategy = new BesideTargetStrategy(1.5);
        Vector pos = strategy.findTeleportPosition(bot, target);
        if (pos == null) return false;
        return teleportTo(pos);
    }

    public boolean teleportSafeNear(Player target) {
        if (target == null) return false;

        ITeleportStrategy strategy = new SafeTeleportStrategy(validator);
        Vector pos = strategy.findTeleportPosition(bot, target);
        if (pos == null) return false;
        return teleportTo(pos);
    }

    public boolean teleportNearTarget() {
        if (currentTarget == null || !currentTarget.isOnline()) return false;
        return teleportSafeNear(currentTarget);
    }

    public void handleSuffocationDamage() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSuffocationTime > SUFFOCATION_RESET_TIME) {
            suffocationCount = 0;
        }

        suffocationCount++;
        lastSuffocationTime = currentTime;

        if (suffocationCount >= MAX_SUFFOCATION_BEFORE_TELEPORT) {
            teleportNearTarget();
            suffocationCount = 0;
        }
    }

    public int getSuffocationCount() {
        return suffocationCount;
    }

    public long getLastSuffocationTime() {
        return lastSuffocationTime;
    }

    public void resetSuffocationCounter() {
        suffocationCount = 0;
        lastSuffocationTime = 0;
    }

    public boolean isSuffocating() {
        return validator.isSuffocationDamage(bot);
    }

    public boolean isTeleporting() {
        return teleporting;
    }

    public void setTarget(org.bukkit.entity.@Nullable Player target) {
        this.currentTarget = target;
    }

    public org.bukkit.entity.@Nullable Player getCurrentTarget() {
        return currentTarget;
    }
}
