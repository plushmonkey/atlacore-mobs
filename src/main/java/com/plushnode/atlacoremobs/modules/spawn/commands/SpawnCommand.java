package com.plushnode.atlacoremobs.modules.spawn.commands;

import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.commands.MultiplexableCommand;
import com.plushnode.atlacoremobs.compatibility.DisguiseUtil;
import com.plushnode.atlacoremobs.generator.*;
import com.plushnode.atlacoremobs.modules.spawn.SpawnManager;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SpawnCommand implements MultiplexableCommand {
    private AtlaCoreMobsPlugin plugin;
    private String[] aliases = { "spawn", "s" };

    public SpawnCommand(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be ran as a player.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GREEN + "/acmobs spawn [[element:]type] <amount> <name>");
            return true;
        }

        Player player = (Player)sender;

        SpawnManager spawnManager = plugin.getSpawnManager();

        ScriptedUserGenerator userGenerator = new ScriptedFirebenderGenerator();
        EntityType type;

        String typeString = args[1];

        if (typeString.contains(":")) {
            String[] typeTokens = typeString.split(":", 2);

            String elementStr = typeTokens[0];

            if (!elementStr.isEmpty()) {
                switch (elementStr.toLowerCase().charAt(0)) {
                    case 'f':
                        userGenerator = new ScriptedFirebenderGenerator();
                    break;
                    case 'a':
                        userGenerator = new ScriptedAirbenderGenerator();
                    break;
                    case 'e':
                        userGenerator = new ScriptedEarthbenderGenerator();
                    break;
                    case 'n':
                        userGenerator = new ScriptedNullGenerator();
                    break;
                    default:
                        userGenerator = new ScriptedFirebenderGenerator();
                }
            }

            typeString = typeTokens[1];
        }

        try {
            type = EntityType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Failed to spawn mob. Illegal type specified.");
            return true;
        }

        if ((!type.isSpawnable() || !type.isAlive()) && type != EntityType.PLAYER) {
            sender.sendMessage(ChatColor.RED + "Failed to spawn mob. Type must be a LivingEntity.");
            return true;
        }

        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                // pass
            }
        }

        String name = "";
        if (args.length > 3) {
            name = args[3];
        }

        int numSpawned = 0;

        for (int i = 0; i < amount; ++i) {
            EntityType spawnType = type;

            if (spawnType == EntityType.PLAYER) {
                spawnType = EntityType.VILLAGER;
            }

            ScriptedUser user = spawnManager.spawn(player, spawnType, userGenerator);
            if (user != null) {
                ++numSpawned;

                if (spawnType == EntityType.VILLAGER) {
                    // Drop villagers down near player sprint speed.
                    LivingEntity living = (LivingEntity)user.getBukkitEntity();
                    living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                            .addModifier(new AttributeModifier("generic.movementSpeed", -0.2, AttributeModifier.Operation.ADD_SCALAR));
                }

                if (!name.isEmpty()) {
                    user.getBukkitEntity().setCustomName(name);
                    user.getBukkitEntity().setCustomNameVisible(true);

                    if (type == EntityType.PLAYER) {
                        String skin = name;

                        if (args.length > 4) {
                            skin = args[4];
                        }

                        DisguiseUtil.disguise(user.getBukkitEntity(), name, skin);
                    }
                }
            }
        }

        if (numSpawned > 0) {
            sender.sendMessage(ChatColor.GOLD + Integer.toString(numSpawned) + " mobs spawned at your location.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to spawn mobs.");
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Spawns bending mobs at your location.";
    }

    @Override
    public String getPermission() {
        return "acmobs.command.spawn";
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
