package org.hero.strawgolem.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.BeekeeperFetchGoal;
import org.hero.strawgolem.golem.goals.BeekeeperHarvestGoal;
import org.hero.strawgolem.golem.goals.BeekeeperStashGoal;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;

/**
 * A beekeeper golem that harvests full beehives using tools from its bound
 * chest: glass bottles yield honey bottles, shears yield honeycomb. Being made
 * of straw, it never angers the bees.
 */
public class BeekeeperGolem extends StrawGolem {

    public BeekeeperGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    public boolean hasDepositChest() {
        return getPriorityPos().getX() != Integer.MAX_VALUE;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(1, new BeekeeperFetchGoal(this));
        goalSelector.addGoal(1, new BeekeeperHarvestGoal(this));
        goalSelector.addGoal(2, new BeekeeperStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
