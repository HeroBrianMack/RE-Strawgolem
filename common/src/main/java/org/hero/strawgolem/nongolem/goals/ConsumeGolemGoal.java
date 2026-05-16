package org.hero.strawgolem.nongolem.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A custom version of the MoveToTargetGoal, with the difference is in the targeting mechanism.
 * My version will search for a target rather than checking current target.
 */
public class ConsumeGolemGoal extends Goal {
    private final PathfinderMob mob;
    @Nullable
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;

    public ConsumeGolemGoal(PathfinderMob pMob, double pSpeedModifier, float pWithin) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.within = pWithin;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse() {
        // Not perfect precision, but good enough

        target = mob.level().getEntitiesOfClass(StrawGolem.class,
                mob.getBoundingBox().inflate(
                        Constants.Golem.searchRange,
                        Constants.Golem.searchRangeVertical,
                        Constants.Golem.searchRange
                )).stream().sorted((entity1, entity2) ->
                (int) (10.0f * (mob.distanceTo(entity2) - mob.distanceTo(entity1)))).findFirst().orElse(null);
    if (this.target == null) {
            return false;
        } else if (this.target.distanceToSqr(this.mob) > (double)(this.within * this.within)) {
            return false;
        } else {
            Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, 16, 7, this.target.position(), 1.5707963705062866);
            if (vec3 == null) {
                return false;
            } else {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                return true;
            }
        }
    }

    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceToSqr(this.mob) < (double)(this.within * this.within);
    }

    public void stop() {
        this.target = null;
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}
