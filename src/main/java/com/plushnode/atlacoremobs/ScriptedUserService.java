package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacoremobs.generator.ScriptedUserGenerator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ScriptedUserService {
    private Map<UUID, ScriptedUser> bendingMobs = new HashMap<>();

    public ScriptedUserService() {
        Game.plugin.createTaskTimer(this::update, 1, 1);
    }

    private void update() {
        for (Iterator<Map.Entry<UUID, ScriptedUser>> iterator = bendingMobs.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<UUID,ScriptedUser> entry = iterator.next();
            ScriptedUser user = entry.getValue();

            if (!user.getBukkitEntity().isValid()) {
                Game.getAbilityInstanceManager().destroyPlayerInstances(user);
                //System.out.println("Destroying bending mob.");
                iterator.remove();
                continue;
            }

            user.tick();
        }
    }

    public ScriptedUser create(LivingEntity entity, ScriptedUserGenerator generator) {
        if (bendingMobs.containsKey(entity.getUniqueId())) {
            return bendingMobs.get(entity.getUniqueId());
        }

        ScriptedUser user = generator.generate(entity);

        //System.out.println("Adding new bending mob. Count: " + (bendingMobs.size() + 1));
        bendingMobs.put(entity.getUniqueId(), user);

        return user;
    }

    public void destroy(LivingEntity entity) {
        //System.out.println("Removing bending mob.");

        ScriptedUser user = bendingMobs.get(entity.getUniqueId());
        if (user != null) {
            Game.getAbilityInstanceManager().destroyPlayerInstances(user);
            bendingMobs.remove(entity.getUniqueId());
        }
    }

    public ScriptedUser get(Entity entity) {
        if (bendingMobs.containsKey(entity.getUniqueId())) {
            return bendingMobs.get(entity.getUniqueId());
        }

        return null;
    }
}
