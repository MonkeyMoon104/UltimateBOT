package com.example.ultimatebot;

import com.monkey.ultimatebot.api.extension.brain.BotBrainContext;
import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.brain.BotBrainSession;
import com.monkey.ultimatebot.api.extension.brain.BrainDescriptor;
import com.monkey.ultimatebot.api.extension.brain.BrainTick;
import com.monkey.ultimatebot.common.model.BrainCapability;
import com.monkey.ultimatebot.common.model.BrainKey;
import java.util.Objects;
import java.util.Set;

public final class ExampleBrainProvider implements BotBrainProvider {
    public static final BrainKey KEY = BrainKey.of("example", "adaptive-melee");

    @Override
    public BrainDescriptor descriptor() {
        return new BrainDescriptor(
                KEY,
                "Adaptive Melee",
                "Example full-control combat brain",
                Set.of(BrainCapability.FULL_CONTROL),
                false);
    }

    @Override
    public BotBrainSession create(BotBrainContext context) {
        return new Session(Objects.requireNonNull(context, "context"));
    }

    private record Session(BotBrainContext context) implements BotBrainSession {
        @Override
        public void tick(BrainTick tick) {
            tick.selectedTarget().ifPresent(target -> {
                context.control().lookAt(target);
                double distanceSquared = context.bot().getLocation().distanceSquared(target.getLocation());
                if (distanceSquared > 8.0D) {
                    context.control().moveTowards(target, 2.5D);
                } else {
                    context.control().stopMovement();
                    context.control().attack(target);
                }
            });
        }
    }
}
