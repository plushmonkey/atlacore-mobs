package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacoremobs.util.VectorSmoother;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class VelocityTrackerService {
    private Map<Player, TrackingInfo> players = new HashMap<>();
    private Task task = null;
    private int interval;
    private int smoothing;

    public VelocityTrackerService(int interval, int smoothing) {
        this.interval = interval;
        this.smoothing = smoothing;
    }

    public void start() {
        stop();

        task = Game.plugin.createTaskTimer(this::run, this.interval, this.interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void addPlayer(Player player) {
        TrackingInfo info = new TrackingInfo();

        info.velocity = new VectorSmoother(smoothing);
        info.lastLocation = player.getLocation().clone();

        players.put(player, info);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public Vector3D getVelocity(Player player) {
        TrackingInfo info = players.get(player);

        if (info == null) {
            return Vector3D.ZERO;
        }

        return info.velocity.get();
    }

    public void run() {
        for (Map.Entry<Player, TrackingInfo> entry : players.entrySet()) {
            Player player = entry.getKey();
            TrackingInfo info = entry.getValue();

            // Reset velocity if the player changes worlds.
            if (player.getWorld() != info.lastLocation.getWorld()) {
                info.velocity.clear();
                info.lastLocation = player.getLocation();
            } else {
                Vector velocity = player.getLocation().clone().subtract(info.lastLocation).toVector();

                info.velocity.add(new Vector3D(velocity.getX(), velocity.getY(), velocity.getZ()));
                info.lastLocation = player.getLocation().clone();
            }
        }
    }

    private static class TrackingInfo {
        VectorSmoother velocity;
        Location lastLocation;
    }
}
