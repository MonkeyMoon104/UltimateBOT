package com.example.ultimatebot;

import com.monkey.ultimatebot.api.extension.combat.CombatModeDescriptor;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.api.extension.combat.ModeKit;
import com.monkey.ultimatebot.common.model.combat.CombatCapability;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.CombatTuning;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ExampleModeProvider implements CombatModeProvider {
    private static final CombatMode MODE = CombatMode.of("example", "adaptive-duels");

    @Override
    public CombatModeDescriptor descriptor() {
        CombatModeDescriptor.Builder descriptor =
                CombatModeDescriptor.builder(MODE, "Adaptive Duels", Material.DIAMOND_SWORD)
                        .description(List.of("Example addon-provided mode"))
                        .permission("ultimatebot.mode.example.adaptive-duels")
                        .order(100)
                        .capabilities(Set.of(CombatCapability.MELEE, CombatCapability.MOB_TARGETS))
                        .kit(ModeKit.builder()
                                .inventoryItem(0, new ItemStack(Material.DIAMOND_SWORD))
                                .build())
                        .brain(ExampleBrainProvider.KEY);
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            descriptor.profile(difficulty, CombatTuning.builder().build());
        }
        return descriptor.build();
    }
}
