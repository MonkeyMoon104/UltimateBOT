package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import org.bukkit.util.Vector;

public interface IObstacleHandler {
    boolean handleObstacles(double dx, double dz, double botX, double botY, double botZ, double moveX, double moveZ);

    boolean hasComplexTerrain(Vector position);

    boolean hasObstacles(Vector from, Vector to);

    boolean isDiverting();

    void setUnderFire(boolean underFire);

    void clearDiversion();
}
