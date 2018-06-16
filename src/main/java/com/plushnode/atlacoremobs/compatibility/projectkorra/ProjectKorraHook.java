package com.plushnode.atlacoremobs.compatibility.projectkorra;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.*;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.User;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.firebending.Illumination;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class ProjectKorraHook {
    private static final List<Class<? extends CoreAbility>> PASSIVE_ABILITIES = Arrays.asList(Illumination.class, Tremorsense.class);
    private static boolean enabled = false;

    private User pkUser;
    private ProjectKorraCollisionAbility ability;
    private List<Collision> collidables = new ArrayList<>();

    public ProjectKorraHook() {
        World world = Bukkit.getWorlds().get(0);
        ArmorStand entity = (ArmorStand)world.spawnEntity(new Location(world, 0, 0, 0), EntityType.ARMOR_STAND);
        pkUser = new ProjectKorraUser(entity);
        entity.remove();

        AbilityDescription desc = new GenericAbilityDescription<>("PKCompat", Elements.FIRE, 0, ProjectKorraCollisionAbility.class, ActivationMethod.Passive);
        desc.setHidden(true);
        desc.setHarmless(true);
        Game.getAbilityRegistry().registerAbility(desc);

        enabled = true;
    }

    public void createAbility() {
        boolean exists = Game.getAbilityInstanceManager().getInstances().stream()
                .anyMatch((ability) -> ability.getClass().equals(ProjectKorraCollisionAbility.class));

        if (!exists) {
            ability = new ProjectKorraCollisionAbility(this);
            ability.activate(pkUser, ActivationMethod.Passive);
            Game.getAbilityInstanceManager().addAbility(pkUser, ability);

            collidables = getRegisteredCollisions();

            for (AbilityDescription desc : Game.getAbilityRegistry().getAbilities()) {
                Game.getCollisionService().registerCollision(desc, Game.getAbilityRegistry().getAbilityByName("PKCompat"), false, false);
            }
        }
    }

    public void handleCollision(CoreAbility pkAbility, Ability other) {
        Optional<Collision> result = collidables.stream()
                .filter((rc) -> {
                    return rc.getAbilityFirst().getName().equalsIgnoreCase(pkAbility.getName()) ||
                        rc.getAbilitySecond().getName().equalsIgnoreCase(pkAbility.getName());
                })
                .filter((rc) -> {
                    AbilityDescription desc = other.getDescription();
                    String name = desc.getName();

                    return rc.getAbilityFirst().getName().equalsIgnoreCase(name) ||
                            rc.getAbilitySecond().getName().equalsIgnoreCase(name);
                }).findAny();

        if (result.isPresent()) {
            Collision rc = result.get();
            String firstName = rc.getAbilityFirst().getName();
            boolean removePK = false;
            boolean removeAtla = false;

            if (firstName.equalsIgnoreCase(pkAbility.getName())) {
                if (rc.isRemovingFirst()) {
                    removePK = true;
                }

                if (rc.isRemovingSecond()) {
                    removeAtla = true;
                }
            } else {
                if (rc.isRemovingFirst()) {
                    removeAtla = true;
                }

                if (rc.isRemovingSecond()) {
                    removePK = true;
                }
            }

            if (removeAtla) {
                Game.getAbilityInstanceManager().destroyInstance(other.getUser(), other);
            }

            if (removePK) {
                pkAbility.remove();
            }
        }
    }

    public List<Collision> getRegisteredCollisions() {
        return ProjectKorra.getCollisionManager().getCollisions();
    }

    // Attempts to remove flight from all of the players in the arena if they aren't using any abilities.
    public static void fixFlight(List<Player> players) {
        if (!enabled) return;

        Collection<CoreAbility> abilities = CoreAbility.getAbilitiesByInstances();

        for (Player player : players) {
            GameMode gm = player.getGameMode();

            if (gm != GameMode.SURVIVAL) continue;
            if (!player.isFlying() && !player.getAllowFlight()) continue;

            boolean hasAbility = abilities.parallelStream()
                    .anyMatch((ability) -> ability.getPlayer() == player && isRealAbility(ability));

            if (!hasAbility) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    private static boolean isRealAbility(CoreAbility ability) {
        return !(ability instanceof com.projectkorra.projectkorra.ability.PassiveAbility) && !PASSIVE_ABILITIES.contains(ability.getClass());
    }

    public static boolean hasAbility(Player player, String name) {
        if (!enabled) return false;

        CoreAbility ability = CoreAbility.getAbility(name);

        return ability != null && CoreAbility.hasAbility(player, ability.getClass());
    }
}
