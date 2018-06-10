package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.GameMode;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.PotionEffectType;
import com.plushnode.atlacore.platform.User;

public class NearestPlayerTargetSelector implements TargetSelector {
    private ScriptedUser user;

    public NearestPlayerTargetSelector(ScriptedUser user) {
        this.user = user;
    }

    @Override
    public User getTarget() {
        double closestDistSq = Double.MAX_VALUE;
        Player closest = null;

        for (Player player : Game.getPlayerService().getOnlinePlayers()) {
            if (!player.getWorld().equals(user.getLocation().getWorld())) continue;
            if (player.getGameMode() != GameMode.SURVIVAL) continue;
            if (player.getPotionEffect(PotionEffectType.INVISIBILITY) != null) continue;

            double distSq = player.getLocation().distanceSquared(user.getLocation());
            if (distSq < closestDistSq) {
                closest = player;
                closestDistSq = distSq;
            }
        }

        return closest;
    }
}
