package com.plushnode.atlacoremobs.modules.trainingarena;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KillTracker {
    private Map<Player, Integer> killCounts = new HashMap<>();

    public void addKill(Player player) {
        Integer kills = killCounts.get(player);

        if (kills == null) {
            kills = 0;
        }

        killCounts.put(player, kills + 1);
    }

    public int getAndReset(Player player) {
        int kills = killCounts.getOrDefault(player, 0);

        killCounts.remove(player);

        return kills;
    }

    // Clears any players in the map that aren't in this list.
    public void acceptOnly(List<Player> players) {
        for (Iterator<Map.Entry<Player, Integer>> iter = killCounts.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Player, Integer> entry = iter.next();

            Player player = entry.getKey();

            if (!players.contains(player)) {
                iter.remove();
            }
        }
    }
}
