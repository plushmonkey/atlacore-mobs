package com.plushnode.atlacoremobs.compatibility.projectkorra;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.TypeUtil;
import com.projectkorra.projectkorra.ability.CoreAbility;

import java.util.*;
import java.util.stream.Collectors;

// This is a fake AtlaCore ability that returns colliders for all active PK abilities.
// It acts as a compatibility bridge for the two collision systems.
public class ProjectKorraCollisionAbility implements Ability {
    private User user;
    private ProjectKorraHook pkh;
    private Map<Collider, CoreAbility> colliderMap = new HashMap<>();

    public ProjectKorraCollisionAbility(ProjectKorraHook pkh) {
        this.pkh = pkh;
    }

    @Override
    public boolean activate(User user, ActivationMethod activationMethod) {
        this.user = user;
        return false;
    }

    @Override
    public void recalculateConfig() {

    }

    @Override
    public UpdateResult update() {
        colliderMap.clear();

        // Don't bother creating colliders if there's no atlacore abilities to collide with.
        if (Game.getAbilityInstanceManager().getInstanceCount() == 0) {
            return UpdateResult.Continue;
        }

        for (CoreAbility ability : CoreAbility.getAbilitiesByInstances()) {
            // Convert all of the PK ability locations into atlacore Spheres.
            List<Collider> abilityColliders = ability.getLocations().stream()
                    .filter((loc) -> loc != null)
                    .map(TypeUtil::adapt)
                    .map((loc) -> new Sphere(loc.toVector(), ability.getCollisionRadius(), loc.getWorld()))
                    .collect(Collectors.toList());

            for (Collider collider : abilityColliders) {
                colliderMap.put(collider, ability);
            }
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "PKCompat";
    }

    @Override
    public Collection<Collider> getColliders() {
        return new ArrayList<>(colliderMap.keySet());
    }

    @Override
    public void handleCollision(Collision collision) {
        Collider collider = collision.getFirstCollider();
        CoreAbility pkAbility = colliderMap.get(collider);

        if (pkAbility == null) return;

        pkh.handleCollision(pkAbility, collision.getSecondAbility());
    }
}
