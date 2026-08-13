package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.agent.AgentSmoke;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

/** Console/agent entrypoint: {@code /ubagent smoke}. */
public final class AgentSmokeCommand {

    private final UltimateBot plugin;

    public AgentSmokeCommand(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Command("ubagent smoke")
    @CommandPermission("ultimatebot.admin.use")
    public void smoke(BukkitCommandActor actor) {
        AgentSmoke.SmokeReport report = AgentSmoke.run(plugin);
        if (report.passed()) {
            actor.sender()
                    .sendMessage(ChatColorUtils.translate(
                            "&a[agent-smoke] PASS &7" + report.bridgeClass()));
        } else {
            actor.sender()
                    .sendMessage(ChatColorUtils.translate(
                            "&c[agent-smoke] FAIL &7" + report.error()));
        }
    }
}
