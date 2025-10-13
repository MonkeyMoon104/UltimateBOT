package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.rank.BotRank;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BotOptions {

    private final SandboxTraining training;
    private final Map<EquipmentSlot, ItemStack> armor;
    private final Map<EquipmentSlot, Boolean> blast = new HashMap<>();
    private int totems;
    private boolean follow = false;
    private boolean combat = false;
    private BotRank rank = BotRank.EASY;
    private boolean isEventBot = false;

    public BotOptions(SandboxTraining training, Map<EquipmentSlot, ItemStack> armor) {
        this.training = training;
        this.armor = armor;
        this.totems = training.getConfig().getInt("bot.default-totem-count", -1);
    }

    public boolean isEventBot() {
        return isEventBot;
    }

    public void setEventBot(boolean eventBot) {
        isEventBot = eventBot;
    }

    public BotRank getRank() {
        return rank;
    }

    public void setRank(BotRank rank) {
        this.rank = rank;
    }


    public int getTotems() {
        return totems;
    }

    public void setTotems(int totems) {
        this.totems = totems;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isCombat() {return combat; }
    public void setCombat(boolean combat) {this.combat = combat; }

    public Map<EquipmentSlot, ItemStack> getArmor() {
        return armor;
    }

    public Map<EquipmentSlot, Boolean> getBlast() {
        return blast;
    }
}
