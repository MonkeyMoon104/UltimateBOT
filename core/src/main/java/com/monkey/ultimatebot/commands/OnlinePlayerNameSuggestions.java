package com.monkey.ultimatebot.commands;

import java.util.Collection;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.monkey.ultimatebot.libs.lamp.autocomplete.SuggestionProvider;
import com.monkey.ultimatebot.libs.lamp.bukkit.actor.BukkitCommandActor;
import com.monkey.ultimatebot.libs.lamp.node.ExecutionContext;

public final class OnlinePlayerNameSuggestions implements SuggestionProvider<BukkitCommandActor> {

    @Override
    public @NotNull Collection<String> getSuggestions(@NotNull ExecutionContext<BukkitCommandActor> context) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
