package com.plushnode.atlacoremobs.modules.raid.commands;

import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.commands.MultiplexableCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class PurgeOldCommand implements MultiplexableCommand {
    private String aliases[] = { "purgeold", "po" };
    private AtlaCoreMobsPlugin plugin;

    public PurgeOldCommand(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/acmobs purgeold [world]");
            return true;
        }

        String worldName = args[1];

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World could not be found.");
            return true;
        }

        int removeCount = 0;
        for (Entity entity : world.getLivingEntities()) {
            String customName = entity.getCustomName();

            if (customName != null && customName.contains("Fire Raider")) {
                entity.remove();
                ++removeCount;
            }
        }

        if (removeCount > 0) {
            sender.sendMessage("Successfully removed " + removeCount + " raiders.");
        } else {
            sender.sendMessage("No raiders found.");
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Removes any old raider entities.";
    }

    @Override
    public String getPermission() {
        return "acmobs.command.purgeold";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
