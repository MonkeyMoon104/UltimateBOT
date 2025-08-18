package it.coralmc.sandbox.gui.impl;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TeleportItem extends AbstractItem {

	private final SandboxTraining training;

	public TeleportItem(SandboxTraining training) {
		this.training = training;
	}

	@Override
	public ItemProvider getItemProvider() {
		Material mat = Material.valueOf(training.getConfig().getString("gui.teleport-button.material"));
		String name = training.getConfig().getString("gui.teleport-button.name");
		var lore = training.getConfig().getStringList("gui.teleport-button.lore");

		ItemBuilder builder = new ItemBuilder(mat);
		builder.setDisplayName(ChatColorUtils.translate(name));
		for (String line : lore) {
			builder.addLoreLines(ChatColorUtils.translate(line));
		}
		return builder;
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (!training.getBotManager().isBotSpawned(player.getUniqueId())) return;

		TrainingBot bot = training.getBotManager().getBot(player.getUniqueId());
		if (bot == null) return;

		bot.moveTo(player.getX(), player.getY(), player.getZ());
	}
}
