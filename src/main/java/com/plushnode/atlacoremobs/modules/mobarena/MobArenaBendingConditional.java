package com.plushnode.atlacoremobs.modules.mobarena;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.platform.LocationWrapper;
import com.plushnode.atlacore.platform.User;
import org.bukkit.Location;

public class MobArenaBendingConditional implements BendingConditional {
    @Override
    public boolean canBend(User user, AbilityDescription abilityDescription) {
        Location location = ((LocationWrapper)user.getLocation()).getBukkitLocation();

        return MobArenaGame.isInArena(location);
    }
}
