package com.plushnode.atlacoremobs.modules.spawn.commands;

import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.commands.MultiplexableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ClearSpawnsCommand implements MultiplexableCommand {
    private AtlaCoreMobsPlugin plugin;
    private String[] aliases = { "clearspawns", "cs" };

    public ClearSpawnsCommand(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int count = plugin.getSpawnManager().destroySpawns();

        sender.sendMessage(ChatColor.GOLD + "Cleared " + count + " spawned mobs.");

        return true;
    }

    @Override
    public String getDescription() {
        return "Clears all existing bending mobs.";
    }

    @Override
    public String getPermission() {
        return "acmobs.command.clearspawns";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
