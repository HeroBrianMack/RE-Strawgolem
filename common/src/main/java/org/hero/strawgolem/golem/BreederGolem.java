package org.hero.strawgolem.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.BreederBreedGoal;
import org.hero.strawgolem.golem.goals.BreederRestockGoal;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;

/**
 * A golem that keeps nearby animals bred: restocks breeding food from nearby
 * containers and feeds compatible adult animal pairs.
 */
public class BreederGolem extends StrawGolem {

    public BreederGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    /**
     * Unlike the harvester, the breeder must stand among livestock, so it only
     * keeps the Pillager avoidance and does not flee from animals.
     */
    @Override
    protected void generateAvoids() {
        goalSelector.addGoal(1, new org.hero.strawgolem.golem.goals.GolemAvoidEntityGoal<>(this,
                net.minecraft.world.entity.monster.Pillager.class,
                (e) -> e.getTarget() instanceof StrawGolem,
                Golem.fleeRange, Golem.defaultWalkSpeed, Golem.defaultRunSpeed,
                net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(1, new BreederRestockGoal(this));
        goalSelector.addGoal(1, new BreederBreedGoal(this));
        goalSelector.addGoal(1, new org.hero.strawgolem.golem.goals.BreederShearGoal(this));
        goalSelector.addGoal(2, new org.hero.strawgolem.golem.goals.BreederPickupGoal(this));
        goalSelector.addGoal(2, new org.hero.strawgolem.golem.goals.BreederStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
