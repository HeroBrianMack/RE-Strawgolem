package org.hero.strawgolem.golem.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

import java.util.function.Predicate;

public class GolemAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    double tmp1;
    double tmp2;

//    public GolemAvoidEntityGoal(PathfinderMob pMob, Class pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
//        super(pMob, pEntityClassToAvoid, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier);
//    }

    public GolemAvoidEntityGoal(StrawGolem pMob, Class<T> pEntityClassToAvoid, Predicate<Mob> pAvoidPredicate, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier, Predicate pPredicateOnAvoidEntity) {
        super(pMob, pEntityClassToAvoid, (living) -> {
            if (living instanceof Mob mob) {
                return pAvoidPredicate.test(mob);
            }
            return false;
        }, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier, pPredicateOnAvoidEntity);
        tmp1 = pSprintSpeedModifier;
        tmp2 = pWalkSpeedModifier;
    }

//    public GolemAvoidEntityGoal(PathfinderMob pMob, Class pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier, Predicate pPredicateOnAvoidEntity) {
//        super(pMob, pEntityClassToAvoid, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier, pPredicateOnAvoidEntity);
//    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }
}
