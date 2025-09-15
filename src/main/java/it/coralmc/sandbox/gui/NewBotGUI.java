package it.coralmc.sandbox.gui;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.gui.impl.*;
import it.coralmc.sandbox.utils.armor.ArmorCycle;
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
    private final SandboxTraining training;

    public NewBotGUI(Player player, SandboxTraining training) {
        this.player = player;
        this.training = training;
    }

    public void open() {
        BotOptions options = training.getPlayerOptions().getOptions(player.getUniqueId());
        if (options == null) {
            options = new BotOptions(training, ArmorCycle.getDefaultArmorFromConfig(training.getConfig(), training));
        }

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

        for (EquipmentSlot value : EquipmentSlot.values()) {
            if (value == EquipmentSlot.HAND || value == EquipmentSlot.OFF_HAND || value == EquipmentSlot.BODY) continue;

            ArmorItem item = new ArmorItem(training, value, options.getArmor().get(value), options);

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
                .setTitle("ᴋɪᴛ ʀᴏᴏᴍ")
                .addCloseHandler(() -> {
                    UUID playerUUID = player.getUniqueId();
                    training.getPlayerOptions().put(playerUUID, finalOptions);
                })
                .build();

        window.open();
    }
}
