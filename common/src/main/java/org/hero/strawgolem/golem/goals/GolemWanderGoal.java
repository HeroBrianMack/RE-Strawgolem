package org.hero.strawgolem.golem.goals;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.hero.strawgolem.golem.StrawGolem;

public class GolemWanderGoal extends WaterAvoidingRandomStrollGoal {

    public GolemWanderGoal(StrawGolem golem) {
        super(golem, StrawGolem.defaultWalkSpeed);
    }
    // Eventually add wander limits again
    @Override
    public boolean canUse() {
        return this.mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && super.canUse();
    }
}
