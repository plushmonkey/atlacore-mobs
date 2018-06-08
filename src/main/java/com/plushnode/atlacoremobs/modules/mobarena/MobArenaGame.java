package com.plushnode.atlacoremobs.modules.mobarena;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.Arena;
import com.plushnode.atlacore.board.BendingBoard;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.conditionals.CompositeBendingConditional;
import com.plushnode.atlacore.platform.LocationWrapper;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacoremobs.AtlaCoreMobsPlugin;
import com.plushnode.atlacoremobs.commands.CommandDemultiplexer;
import com.plushnode.atlacoremobs.modules.mobarena.listeners.ArenaListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.function.Predicate;

public class MobArenaGame {
    private static final String BENDING_ARENA = "forest";
    private static MobArena mobArena = null;

    private ArenaListener arenaListener;
    private CommandDemultiplexer commandDemultiplexer;
    private Task updateTask;

    public MobArenaGame(AtlaCoreMobsPlugin plugin) {
        boolean enableMobArenaGame = plugin.getConfigRoot().getNode("mob-arena", "enabled").getBoolean(false);
        if (!enableMobArenaGame) {
            return;
        }

        Plugin mobArenaPlugin = Bukkit.getPluginManager().getPlugin("MobArena");
        if (mobArenaPlugin == null) {
            plugin.getLogger().warning("Failed to find MobArena plugin. Disabling MobArena game.");
            return;
        }

        mobArena = (MobArena)mobArenaPlugin;

        this.updateTask = Game.plugin.createTaskTimer(this::update, 1, 1);

        boolean hasProjectKorra = Bukkit.getPluginManager().getPlugin("ProjectKorra") != null;

        this.arenaListener = new ArenaListener(plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this.arenaListener, plugin);

        // Add MobArenaBendingConditional to all online players.
        // This needs to be done because this object could have been created during reload.
        for (Player player : Game.getPlayerService().getOnlinePlayers()) {
            CompositeBendingConditional cond = player.getBendingConditional();
            cond.removeType(MobArenaBendingConditional.class);
            cond.add(new MobArenaBendingConditional());
        }

        if (hasProjectKorra) {
            this.commandDemultiplexer = new CommandDemultiplexer("b", "pk", "atla", (p) -> isInArena(p.getLocation()));
            Bukkit.getServer().getPluginManager().registerEvents(this.commandDemultiplexer, plugin);
        }
    }

    public void stop() {
        setBoardsEnabled((p) -> true);
        Game.getPlayerService().getOnlinePlayers()
                .forEach((p) -> p.getBendingConditional().removeType(MobArenaBendingConditional.class));

        if (this.arenaListener != null) {
            HandlerList.unregisterAll(this.arenaListener);
        }

        if (this.commandDemultiplexer != null) {
            HandlerList.unregisterAll(this.commandDemultiplexer);
        }

        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
    }

    private void update() {
        setBoardsEnabled((p) -> isInArena(((LocationWrapper)p.getLocation()).getBukkitLocation()));
    }

    private void setBoardsEnabled(Predicate<Player> shouldEnable) {
        for (Player player : Game.getPlayerService().getOnlinePlayers()) {
            BendingBoard board = com.plushnode.atlacore.listeners.PlayerListener.boards.get(player.getName());

            if (board == null) continue;

            board.setEnabled(shouldEnable.test(player));
        }
    }

    public static boolean isInArena(Location location) {
        MobArena mobArena = MobArenaGame.getMobArena();
        Arena arena = mobArena.getArenaMaster().getArenaAtLocation(location);

        return arena != null && BENDING_ARENA.equalsIgnoreCase(arena.arenaName());
    }

    public static boolean isHooked() {
        return mobArena != null;
    }

    public static MobArena getMobArena() {
        return mobArena;
    }
}
