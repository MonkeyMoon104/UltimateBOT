package it.coralmc.sandbox.bot.ai.controllers.inventory;

import it.coralmc.sandbox.bot.ai.controllers.inventory.inter.IEquipmentBroadcaster;
import it.coralmc.sandbox.bot.ai.controllers.inventory.inter.IItemManager;
import it.coralmc.sandbox.bot.ai.controllers.inventory.inter.IResourceReplenisher;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Map;

public class ItemManager implements IItemManager {

    public static final int ENDERPEARL_SLOT = 1;
    public static final int TOTEM_SLOT = 2;

    private final Player bot;
    private final Map<Integer, ItemStack> hotbarSlots;
    private final IResourceReplenisher resourceReplenisher;
    private final IEquipmentBroadcaster equipmentBroadcaster;
    private final int currentSlot;

    public ItemManager(Player bot, Map<Integer, ItemStack> hotbarSlots, IResourceReplenisher resourceReplenisher, IEquipmentBroadcaster equipmentBroadcaster, int currentSlot) {
        this.bot = bot;
        this.hotbarSlots = hotbarSlots;
        this.resourceReplenisher = resourceReplenisher;
        this.equipmentBroadcaster = equipmentBroadcaster;
        this.currentSlot = currentSlot;
    }

    @Override
    public void addEnderpearls(int count) {
        ItemStack enderpearlStack = hotbarSlots.get(ENDERPEARL_SLOT);
        if (enderpearlStack == null || enderpearlStack.isEmpty()) {
            hotbarSlots.put(ENDERPEARL_SLOT, new ItemStack(Items.ENDER_PEARL, count));
        } else {
            enderpearlStack.grow(count);
        }
    }

    @Override
    public void updateTotemSlot(ItemStack totemStack) {
        hotbarSlots.put(TOTEM_SLOT, totemStack);
    }

    @Override
    public void onItemUsed(int slot) {
        resourceReplenisher.onItemUsed(hotbarSlots, slot);

        if (currentSlot == slot) {
            ItemStack stack = hotbarSlots.get(slot);
            bot.setItemSlot(EquipmentSlot.MAINHAND, stack);
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }
}