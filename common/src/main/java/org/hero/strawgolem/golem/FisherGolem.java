package org.hero.strawgolem.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.FisherFetchGoal;
import org.hero.strawgolem.golem.goals.FisherFishGoal;
import org.hero.strawgolem.golem.goals.FisherStashGoal;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;

/**
 * A fisher golem that borrows a rod from its bound chest, finds open water,
 * and patiently pulls catches from the fishing loot table into the chest.
 */
public class FisherGolem extends StrawGolem {

    public FisherGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
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
        goalSelector.addGoal(1, new FisherFetchGoal(this));
        goalSelector.addGoal(1, new FisherFishGoal(this));
        goalSelector.addGoal(2, new FisherStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
