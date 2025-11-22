package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

public class GolemWanderGoal extends WaterAvoidingRandomStrollGoal {
    private int wanderLimit;
    private BlockPos startPos;
    public GolemWanderGoal(StrawGolem golem) {
        super(golem, StrawGolem.defaultWalkSpeed);
        wanderLimit = Constants.Golem.wanderRange;
        startPos = golem.blockPosition();
    }
    // Eventually add wander limits again

    @Override
    public boolean canUse() {
        return this.mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && startPos.distManhattan(mob.blockPosition()) < wanderLimit;
    }
}
