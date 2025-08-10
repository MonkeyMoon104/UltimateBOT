package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.SandboxTraining;
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

    public BotOptions(SandboxTraining training, Map<EquipmentSlot, ItemStack> armor) {
        this.training = training;
        this.armor = armor;
        this.totems = training.getConfig().getInt("bot.default-totem-count", -1);
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

    public Map<EquipmentSlot, ItemStack> getArmor() {
        return armor;
    }

    public Map<EquipmentSlot, Boolean> getBlast() {
        return blast;
    }
}
