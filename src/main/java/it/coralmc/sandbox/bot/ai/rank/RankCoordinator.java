package it.coralmc.sandbox.bot.ai.rank;

public class RankCoordinator {

    public static RAPVPConfig buildRAPVPConfig(BotRank rank) {
        return switch (rank) {
            case EASY -> new RAPVPConfig.Builder()
                    .maxDistance(4)
                    .predictionTicks(2)
                    .minMovement(0.01)
                    .minSafeDistance(1.0)
                    .anchorSearchCooldownMillis(200L)
                    .build();
            case NORMAL -> new RAPVPConfig.Builder()
                    .maxDistance(5)
                    .predictionTicks(3)
                    .minMovement(0.01)
                    .minSafeDistance(2.0)
                    .anchorSearchCooldownMillis(100L)
                    .build();
            case MEDIUM -> new RAPVPConfig.Builder()
                    .maxDistance(8)
                    .predictionTicks(8)
                    .minMovement(0.1)
                    .minSafeDistance(3.0)
                    .anchorSearchCooldownMillis(60L)
                    .build();
            case HARD -> new RAPVPConfig.Builder()
                    .maxDistance(8)
                    .predictionTicks(10)
                    .minMovement(0.01)
                    .minSafeDistance(4.0)
                    .anchorSearchCooldownMillis(30L)
                    .build();
            case GOD -> new RAPVPConfig.Builder()
                    .maxDistance(14)
                    .predictionTicks(20)
                    .minMovement(0.01)
                    .minSafeDistance(4.0)
                    .anchorSearchCooldownMillis(0L)
                    .build();
            default -> new RAPVPConfig.Builder().build();
        };
    }
}
