package com.monkey.mcbot.bot.ai.controllers.movement.helper.interf;

public interface IObstacleHandler {
    boolean handleObstacles(double dx, double dz, double botX, double botY, double botZ, double moveX, double moveZ);

    boolean hasComplexTerrain(net.minecraft.world.phys.Vec3 position);

    boolean hasObstacles(net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to);

    boolean isDiverting();

    void setUnderFire(boolean underFire);

    void clearDiversion();
}
