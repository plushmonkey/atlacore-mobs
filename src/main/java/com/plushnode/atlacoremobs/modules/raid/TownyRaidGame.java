package com.plushnode.atlacoremobs.modules.raid;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.util.ChatColor;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.modules.raid.commands.ForceRaidCommand;
import com.plushnode.atlacoremobs.modules.raid.commands.PurgeOldCommand;
import com.plushnode.atlacoremobs.modules.raid.listeners.RaidEntityListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class TownyRaidGame {
    private AtlaCoreMobsPlugin plugin;
    private Towny towny;
    private Raid raid;
    private long nextRaidTime;
    private long raidDelayMin;
    private long raidDelayMax;
    private Random rand = new Random();
    private RaidEntityListener raidEntityListener;
    private Task updateTask;

    public TownyRaidGame(AtlaCoreMobsPlugin plugin) {
        this.plugin = plugin;

        boolean enableRaidGame = plugin.getConfigRoot().getNode("raid", "enabled").getBoolean(false);
        if (!enableRaidGame) {
            return;
        }

        Plugin townyPlugin = Bukkit.getPluginManager().getPlugin("Towny");
        if (townyPlugin == null) {
            plugin.getLogger().warning("Failed to find Towny plugin. Disabling Towny raid game.");
            return;
        }

        this.towny = (Towny)townyPlugin;
        this.raid = null;
        this.raidDelayMin = plugin.getConfigRoot().getNode("raid", "raid-delay-min").getInt(60 * 60 * 1000);
        this.raidDelayMax = plugin.getConfigRoot().getNode("raid", "raid-delay-max").getInt(90 * 60 * 1000);

        initializeRaidConfig();

        resetRaidTime();

        this.updateTask = Game.plugin.createTaskTimer(this::update, 1, 1);
        this.raidEntityListener = new RaidEntityListener(this);

        Bukkit.getPluginManager().registerEvents(this.raidEntityListener, plugin);

        plugin.getCommandMultiplexer().registerCommand(new ForceRaidCommand(plugin));
        plugin.getCommandMultiplexer().registerCommand(new PurgeOldCommand(plugin));
    }

    private void initializeRaidConfig() {
        plugin.getConfigRoot().getNode("raid", "spawn", "waves").getInt(30);
        plugin.getConfigRoot().getNode("raid", "spawn", "delay").getInt(10000);
        plugin.getConfigRoot().getNode("raid", "spawn", "amount-per-wave").getInt(2);
        plugin.getConfigRoot().getNode("raid", "spawn", "health").getDouble(20.0);
        plugin.getConfigRoot().getNode("raid", "nearby-town-range").getInt(2);
        plugin.getConfigRoot().getNode("raid", "end-delay").getInt(60000);
        plugin.getConfigRoot().getNode("raid", "kill-reward").getDouble(150.0);
    }

    public void stop() {
        if (raid != null) {
            raid.stop();
        }

        if (this.raidEntityListener != null) {
            HandlerList.unregisterAll(this.raidEntityListener);
        }

        if (this.updateTask != null) {
            this.updateTask.cancel();
        }

        plugin.getCommandMultiplexer().unregisterCommand("forceraid");
        plugin.getCommandMultiplexer().unregisterCommand("purgeold");
    }

    public Raid getRaid() {
        return raid;
    }

    private void resetRaidTime() {
        this.nextRaidTime = System.currentTimeMillis() + raidDelayMin + (long)((raidDelayMax - raidDelayMin) * rand.nextFloat());
    }

    private void update() {
        if (this.raid == null) {
            if (!createRaid()) {
                return;
            }
        }

        if (!raid.update()) {
            this.raid = null;
        }
    }

    public boolean createRaid(String townName) {
        Town town = towny.getTownyUniverse().getTownsMap().get(townName.toLowerCase());
        if (town == null) {
            plugin.getLogger().info("Selected town " + townName + " doesn't exist.");
            resetRaidTime();
            return false;
        }

        if (TownyUniverse.getOnlinePlayers(town).isEmpty()) {
            plugin.getLogger().info("Failed to create a raid on town " + townName + " because no players are online.");
            return false;
        }

        this.raid = new Raid(plugin, town);
        plugin.getLogger().info("Creating raid for town " + town.getName());
        resetRaidTime();

        List<Player> announceSet;
        if (town.hasNation()) {
            try {
                announceSet = TownyUniverse.getOnlinePlayers(town.getNation());
            } catch (Exception e) {
                announceSet = TownyUniverse.getOnlinePlayers(town);
            }
        } else {
            announceSet = TownyUniverse.getOnlinePlayers(town);
        }

        for (Player player : announceSet) {
            player.sendMessage(ChatColor.GOLD + town.getName() + ChatColor.DARK_RED +  " is under attack by the fire nation!");
        }

        return true;
    }

    private boolean createRaid() {
        long time = System.currentTimeMillis();

        if (time < nextRaidTime) {
            return false;
        }

        plugin.getLogger().info("Selecting town to raid...");

        Town town = selectTown();
        if (town == null) {
            plugin.getLogger().info("Didn't find a town to raid. Resetting timer.");
            resetRaidTime();
            return false;
        }

        return createRaid(town.getName());
    }

    private Town selectTown() {
        Hashtable<String, Town> townMap = towny.getTownyUniverse().getTownsMap();
        if (townMap == null) {
            return null;
        }

        List<Town> potential = new ArrayList<>();

        for (Town town : townMap.values()) {
            List<Player> players = TownyUniverse.getOnlinePlayers(town);

            if (players.isEmpty() || !town.hasHomeBlock()) {
                continue;
            }

            plugin.getLogger().info("Adding town " + town.getName() + " to potential town list.");

            potential.add(town);
        }

        plugin.getLogger().info("Total towns: " + townMap.size() + " Potentials: " + potential.size());

        if (potential.isEmpty()) {
            return null;
        }

        return potential.get(rand.nextInt(potential.size()));
    }

    public static TownBlock getTownBlock(Location location, TownyWorld townyWorld) {
        int x = (int)Math.floor(location.getX() / 16.0);
        int z = (int)Math.floor(location.getZ() / 16.0);

        return new TownBlock(x, z, townyWorld);
    }
}
