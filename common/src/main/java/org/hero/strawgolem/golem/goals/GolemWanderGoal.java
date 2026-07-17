package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

public class GolemWanderGoal extends WaterAvoidingRandomStrollGoal {
    private int wanderLimit;
    private BlockPos startPos;
    public GolemWanderGoal(StrawGolem golem) {
        super(golem, StrawGolem.defaultWalkSpeed);
        wanderLimit = Constants.Golem.wanderRange;
    }
    // Eventually add wander limits again


    @Override
    public void start() {
        super.start();
        startPos = mob.blockPosition();
    }

    /**
     * Golems bound to a container idle near it: stroll targets are kept within
     * the tether radius of the chest, and a golem outside the radius walks back.
     * Unbound golems wander freely as before.
     */
    @Override
    protected Vec3 getPosition() {
        if (mob instanceof StrawGolem golem) {
            BlockPos chest = golem.getPriorityPos();
            if (chest.getX() != Integer.MAX_VALUE) {
                Vec3 anchor = Vec3.atCenterOf(chest);
                double tether = Constants.Golem.tetherRange;
                if (mob.position().distanceToSqr(anchor) > tether * tether) {
                    return LandRandomPos.getPosTowards(mob, (int) tether, 7, anchor);
                }
                for (int i = 0; i < 8; i++) {
                    Vec3 pos = super.getPosition();
                    if (pos != null && pos.distanceToSqr(anchor) <= tether * tether) {
                        return pos;
                    }
                }
                return null;
            }
        }
        return super.getPosition();
    }

    @Override
    public boolean canUse() {
        return this.mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && startPos.distManhattan(mob.blockPosition()) < wanderLimit;
    }
}
