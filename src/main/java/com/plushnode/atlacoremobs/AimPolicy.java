package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;

public interface AimPolicy {
    Location getTarget(ScriptedUser user, User target);
}
