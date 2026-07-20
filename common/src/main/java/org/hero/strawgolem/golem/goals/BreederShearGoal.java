package org.hero.strawgolem.golem.goals;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BreederGolem;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * When the breeder golem is holding shears, it shears any nearby animal that is
 * ready (sheep regrown wool, etc.). The dropped wool is collected by the pickup
 * goal and returned to the chest by the stash goal - a complete wool pipeline.
 */
public class BreederShearGoal extends Goal {
    private static final double SHEAR_DIST_SQ = 4.0;

    private final BreederGolem golem;
    private Animal target;

    public BreederShearGoal(BreederGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean holdingShears(BreederGolem golem) {
        return golem.getMainHandItem().getItem() instanceof ShearsItem;
    }

    public static Animal findShearable(BreederGolem golem) {
        return golem.level().getEntitiesOfClass(Animal.class,
                        golem.getBoundingBox().inflate(Golem.searchRange),
                        a -> a.isAlive() && a instanceof Shearable sh && sh.readyForShearing())
                .stream().min(Comparator.comparingDouble(golem::distanceToSqr)).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (!holdingShears(golem)) {
            return false;
        }
        target = findShearable(golem);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && holdingShears(golem)
                && target instanceof Shearable sh && sh.readyForShearing();
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(target, Golem.defaultWalkSpeed);
    }

    @Override
    public void stop() {
        target = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        golem.getLookControl().setLookAt(target);
        if (golem.distanceToSqr(target) > SHEAR_DIST_SQ) {
            golem.getNavigation().moveTo(target, Golem.defaultWalkSpeed);
            return;
        }
        if (target instanceof Shearable sh && sh.readyForShearing()) {
            sh.shear(SoundSource.NEUTRAL);
            ItemStack shears = golem.getMainHandItem();
            shears.hurtAndBreak(1, golem, EquipmentSlot.MAINHAND);
        }
        target = null;
    }
}
