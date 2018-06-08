package com.plushnode.atlacoremobs.modules.raid.commands;

import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.commands.MultiplexableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ForceRaidCommand implements MultiplexableCommand {
    private String aliases[] = { "forceraid", "fr" };
    private AtlaCoreMobsPlugin plugin;

    public ForceRaidCommand(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/acmobs forceraid [town]");
            return true;
        }

        if (plugin.getRaidGame().getRaid() == null) {
            String targetTown = args[1];

            if (plugin.getRaidGame().createRaid(targetTown)) {
                sender.sendMessage(ChatColor.GOLD + "Raid successfully forced on town " + targetTown + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to force raid on town " + targetTown + ".");
            }
        } else {
            String town = plugin.getRaidGame().getRaid().getTown().getName();
            sender.sendMessage(ChatColor.RED + "Failed to force raid. A raid is already happening on town " + town + ".");
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Forces a raid upon a town.";
    }

    @Override
    public String getPermission() {
        return "acmobs.command.forceraid";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
