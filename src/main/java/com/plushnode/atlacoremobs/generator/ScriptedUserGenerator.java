package com.plushnode.atlacoremobs.generator;

import com.plushnode.atlacoremobs.ScriptedUser;
import org.bukkit.entity.LivingEntity;

public interface ScriptedUserGenerator {
    ScriptedUser generate(LivingEntity entity);
}
