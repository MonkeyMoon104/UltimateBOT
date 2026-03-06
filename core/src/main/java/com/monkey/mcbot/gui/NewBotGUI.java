package com.monkey.mcbot.gui;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.gui.impl.*;
import com.monkey.mcbot.utils.armor.ArmorCycle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewBotGUI {

    private final Player player;
    private final MinecraftBot training;
    private final boolean isEventBot;

    public NewBotGUI(Player player, MinecraftBot training) {
        this(player, training, false);
    }

    public NewBotGUI(Player player, MinecraftBot training, boolean isEventBot) {
        this.player = player;
        this.training = training;
        this.isEventBot = isEventBot;
    }

    public void open() {
        BotOptions options = training.getPlayerOptions().getOptions(player.getUniqueId());
        if (options == null) {
            options = new BotOptions(training, ArmorCycle.getDefaultArmorFromConfig(training.getConfig(), training));
        }

        options.setEventBot(isEventBot);

        clampTotemCount(options);

        CombatItem combatItem = new CombatItem(training, options);
        FollowItem followItem = new FollowItem(training, options, combatItem);

        RankItem rankItem = new RankItem(training, options);

        Gui gui = Gui.normal()
                .setStructure(
                        ". . . . . . . . .",
                        ". . b b . b b . .",
                        ". . a a t a a . .",
                        ". . . . . . . . .",
                        ". . . s g f . . .",
                        "r . . . . . . . c"
                )
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('t', new TotemItem(options, training))
                .addIngredient('f', followItem)
                .addIngredient('s', new SpawnItem(training, player, options))
                .addIngredient('g', training.getBotManager().isBotSpawned(player.getUniqueId())
                    ? new TeleportItem(training) : new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('c', combatItem)
                .addIngredient('r', rankItem)
                .build();

        Map<EquipmentSlot, ArmorItem> armors = new HashMap<>();
        Map<EquipmentSlot, ToggleBlastItem> blasts = new HashMap<>();

        for (EquipmentSlot value : com.monkey.mcbot.utils.equipment.EquipmentConverter.getArmorSlots()) {
            ItemStack piece = options.getArmor().get(value);
            ArmorItem item = new ArmorItem(training, value, piece, options);
            armors.put(value, item);
            blasts.put(value, new ToggleBlastItem(training, options, value, item));
        }

        for (ToggleBlastItem value : blasts.values()) {
            gui.addItems(value);
        }

        for (ArmorItem value : armors.values()) {
            gui.addItems(value);
        }

        BotOptions finalOptions = options;
        Window window = Window.single()
                .setGui(gui)
                .setViewer(player)
                .setTitle(isEventBot ? "ᴋɪᴛ ʀᴏᴏᴍ ᴇᴠᴇɴᴛ" : "ᴋɪᴛ ʀᴏᴏᴍ")
                .addCloseHandler(() -> {
                    UUID playerUUID = player.getUniqueId();
                    training.getPlayerOptions().put(playerUUID, finalOptions);
                })
                .build();

        window.open();
    }

    private void clampTotemCount(BotOptions options) {
        int maxTotem = getMaxTotemCount(options);
        int currentTotem = options.getTotems();

        if (currentTotem == -1) {
            return;
        }

        if (currentTotem > maxTotem) {
            options.setTotems(maxTotem);
        }
    }

    private int getMaxTotemCount(BotOptions options) {
        int normalMax = training.getConfig().getInt("bot.max-totem-normal", 37);
        int eventMax = training.getConfig().getInt("bot.max-totem-event", 74);
        return options.isEventBot() ? eventMax : normalMax;
    }
}
