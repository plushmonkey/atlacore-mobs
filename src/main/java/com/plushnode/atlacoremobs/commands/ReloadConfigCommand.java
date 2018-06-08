package com.plushnode.atlacoremobs.commands;

import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class ReloadConfigCommand implements MultiplexableCommand {
    private AtlaCoreMobsPlugin plugin;
    private String[] aliases = { "reload" };

    public ReloadConfigCommand(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            plugin.reload();
            sender.sendMessage(ChatColor.GOLD + "atlacore-mobs successfully reloaded.");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.DARK_RED + "atlacore-mobs failed to reloaded.");
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin.";
    }

    @Override
    public String getPermission() {
        return "acmobs.command.reload";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
