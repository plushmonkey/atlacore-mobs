package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.internal.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacoremobs.decision.*;
import com.plushnode.atlacoremobs.util.PathfinderUtil;
import com.plushnode.atlacoremobs.util.VectorSmoother;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

public class ScriptedUser extends BukkitBendingUser {
    private static final List<String> SMOOTHED_ABILITIES = Arrays.asList("FireJet", "JetBlast", "JetBlaze", "Combustion");

    private User target;
    private TargetPolicy targetPolicy;
    private DecisionTreeNode decisionTree;
    private DecisionAction currentAction;
    private boolean sneaking = false;
    private int selectedIndex = 1;
    private VectorSmoother directionSmoother = new VectorSmoother(20);
    private Vector3D direction = Vector3D.PLUS_J;
    private AimPolicy aimPolicy;
    private AttributeModifier sneakModifier;

    public ScriptedUser(LivingEntity entity) {
        super(entity);

        sneakModifier = new AttributeModifier("generic.movementSpeed", -0.4, AttributeModifier.Operation.ADD_SCALAR);

        decisionTree = new RandomAbilityDecision(this, 1500);
        targetPolicy = new NearestPlayerTargetPolicy(this);

        PathfinderUtil.disableAI(entity);
        PathfinderUtil.setDefaultAI(entity);

        this.aimPolicy = new GaussianAimPolicy(0.0, 0.4, 40.0, true);
    }

    public AimPolicy getAimPolicy() {
        return this.aimPolicy;
    }

    public void setAimPolicy(AimPolicy module) {
        this.aimPolicy = module;
    }

    public void setDecisionTree(DecisionTreeNode tree) {
        this.decisionTree = tree;
    }

    public void setTargetPolicy(TargetPolicy targetPolicy) {
        this.targetPolicy = targetPolicy;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    @Override
    public AbilityDescription getSelectedAbility() {
        return getSlotAbility(selectedIndex);
    }

    public int getAbilityIndex(String abilityName) {
        for (int slotIndex = 1; slotIndex <= 9; ++slotIndex) {
            AbilityDescription desc = getSlotAbility(slotIndex);
            if (desc == null) continue;

            if (desc.getName().equalsIgnoreCase(abilityName)) {
                return slotIndex;
            }
        }
        return -1;
    }

    public void setScriptedDirection(Vector3D direction) {
        this.direction = direction;
    }

    @Override
    public Vector3D getDirection() {
        if (target == null) {
            return super.getDirection();
        }

        return direction;
    }

    public void setTarget(User newTarget) {
        if (this.target == newTarget) {
            return;
        }

        target = newTarget;

        if (target != null) {
            if (entity instanceof Creature) {
                org.bukkit.entity.Entity targetEntity = ((EntityWrapper) target).getBukkitEntity();
                if (targetEntity instanceof LivingEntity) {
                    ((Creature) entity).setTarget((LivingEntity) targetEntity);
                }
            }
        } else {
            if (entity instanceof Creature) {
                ((Creature)entity).setTarget(null);
            }

            setSneaking(false);
        }
    }

    public User getTarget() {
        return target;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;

        AttributeInstance instance = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        if (instance != null) {
            if (sneaking && !instance.getModifiers().contains(sneakModifier)) {
                instance.addModifier(sneakModifier);
            } else if (!sneaking) {
                instance.removeModifier(sneakModifier);
            }
        }
    }

    public void tick() {
        User newTarget = targetPolicy.getTarget();

        setTarget(newTarget);

        boolean nearby = (target == null) || (target.getLocation().distanceSquared(getLocation()) < 64 * 64);

        if (target == null || !nearby) {
            setTarget(null);
            directionSmoother.clear();
            Game.getAbilityInstanceManager().destroyInstanceType(this, AirScooter.class);
            return;
        }

        Vector3D toTarget = target.getLocation().subtract(getLocation()).toVector();
        Vector3D dir = VectorUtil.normalizeOrElse(toTarget, Vector3D.PLUS_I);
        setDirection(dir);

        if (target.getWorld().equals(getWorld())) {
            Location aimTarget = aimPolicy.getTarget(this, target);

            Vector3D d = aimTarget.subtract(getEyeLocation()).toVector();

            if (d.getNormSq() == 0) {
                d = Vector3D.PLUS_I;
            } else {
                d = d.normalize();
            }

            this.direction = this.directionSmoother.add(d);

            if (!isSmoothed()) {
                this.direction = d;
            }
        }

        if (currentAction == null || currentAction.isDone()) {
            currentAction = decisionTree.decide();
            if (currentAction != null) {
                //System.out.println("New action: " + currentAction.getName());
            }
        }

        if (currentAction != null) {
            //System.out.println("Action: " + currentAction.getName());
            currentAction.act();
        }
    }

    private boolean isSmoothed() {
        return SMOOTHED_ABILITIES.stream().anyMatch((name) -> {
            return Game.getAbilityInstanceManager().hasAbility(this, Game.getAbilityRegistry().getAbilityByName(name));
        });
    }
}
