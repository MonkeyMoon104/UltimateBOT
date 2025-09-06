package it.coralmc.sandbox.bot.ai.controllers.cpvp.crystal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CrystalAttacker {

    private final Player bot;
    private final Level level;

    public CrystalAttacker(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public boolean attackCrystal(EndCrystal crystal, double crystalAttackRange) {
        if (crystal == null || !crystal.isAlive()) return false;

        double distance = bot.position().distanceTo(crystal.position());
        if (distance > crystalAttackRange) return false;

        try {
            bot.attack(crystal);
            bot.swing(InteractionHand.MAIN_HAND);

            return true;
        } catch (Exception e) {
            System.err.println("Errore nell'attacco al crystal: " + e.getMessage());
            return false;
        }
    }
}