package com.plushnode.atlacoremobs.actions.earth;

import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import com.plushnode.atlacoremobs.ScriptedUser;
import com.plushnode.atlacoremobs.decision.DecisionAction;

import java.util.Arrays;
import java.util.Collection;

public class EarthBlastAction extends DecisionAction {
    private ScriptedUser user;
    private AbilityDescription desc;
    private boolean done;
    private boolean sourced;
    private long sourcedTime;

    public EarthBlastAction(ScriptedUser user) {
        this.user = user;
        this.desc = Game.getAbilityRegistry().getAbilityByName("EarthBlast");
        this.done = false;
        this.sourced = false;
    }

    @Override
    public void act() {
        if (this.desc == null || this.done) {
            this.done = true;
            return;
        }

        int index = user.getAbilityIndex("EarthBlast");
        if (index == -1) {
            this.done = true;
            return;
        }

        if (!user.isOnCooldown(this.desc)) {
            user.setSelectedIndex(index);

            Ability ability = desc.createAbility();

            if (!sourced) {
                Block source = findSource();

                if (source == null) {
                    this.done = true;
                    return;
                }

                Vector3D toSource = source.getLocation().subtract(user.getEyeLocation()).toVector();
                toSource = VectorUtil.normalizeOrElse(toSource, Vector3D.PLUS_I);
                user.setScriptedDirection(toSource);

                if (ability.activate(user, ActivationMethod.Sneak)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                    this.sourced = true;
                    this.sourcedTime = System.currentTimeMillis();
                }
            } else if (System.currentTimeMillis() >= this.sourcedTime + 500) {
                User target = user.getTarget();
                if (target == null) {
                    this.done = true;
                    return;
                }

                Vector3D direction = target.getLocation().add(0.0, 1.0, 0.0).subtract(user.getLocation()).toVector();
                direction = VectorUtil.normalizeOrElse(direction, Vector3D.PLUS_I);

                user.setScriptedDirection(direction);

                if (ability.activate(user, ActivationMethod.Punch)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                }

                this.done = true;
            }
        }
    }

    private Block findSource() {
        Collection<Block> nearby = WorldUtil.getNearbyBlocks(user.getLocation(), 5.0,
                Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR));

        // TODO: Avoid block beneath player.
        for (Block block : nearby) {
            Vector3D toBlock = block.getLocation().subtract(user.getLocation()).toVector();
            if (toBlock.dotProduct(user.getDirection()) <= 0) continue;

            if (MaterialUtil.isEarthbendable(block)) {
                Ray ray = new Ray(user.getEyeLocation(), VectorUtil.normalizeOrElse(toBlock, Vector3D.PLUS_I));
                Block target = RayCaster.blockCast(user.getWorld(), ray, 6.0, true);

                if (target == null) continue;

                if (target.equals(block)) {
                    return block;
                }

                if (MaterialUtil.isEarthbendable(target)) {
                    return target;
                }
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return "EarthBlastAction";
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
