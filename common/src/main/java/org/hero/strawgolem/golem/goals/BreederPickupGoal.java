package org.hero.strawgolem.golem.goals;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BreederGolem;
import org.hero.strawgolem.golem.api.ReachHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Lets the breeder golem tidy its pen: any dropped item in range (chicken eggs,
 * modded animal by-products, whatever) gets picked up. Food it can use stays in
 * hand for breeding; everything else is returned to the chest by the stash goal.
 */
public class BreederPickupGoal extends Goal {
    private final BreederGolem golem;
    private ItemEntity target;
    private int pickupTicks;
    private boolean acquired;

    public BreederPickupGoal(BreederGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private ItemEntity findItem() {
        List<ItemEntity> items = golem.level().getEntitiesOfClass(ItemEntity.class,
                golem.getBoundingBox().inflate(Golem.searchRange, Golem.searchRangeVertical, Golem.searchRange),
                e -> e.isAlive() && !e.hasPickUpDelay() && !e.getItem().isEmpty()
                        && ReachHelper.canPath(golem, e.blockPosition()));
        return items.stream().min(Comparator.comparingDouble(golem::distanceToSqr)).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (!golem.getMainHandItem().isEmpty() || golem.carryStatus() != 0) {
            return false;
        }
        target = findItem();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !acquired && golem.getMainHandItem().isEmpty();
    }

    @Override
    public void start() {
        pickupTicks = 0;
        acquired = false;
        moveToTarget();
    }

    @Override
    public void stop() {
        target = null;
        pickupTicks = 0;
        acquired = false;
        golem.setPickupStatus(0);
        golem.getNavigation().stop();
    }

    private void moveToTarget() {
        golem.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }
        golem.getLookControl().setLookAt(target);
        if (!ReachHelper.canReach(golem, target.blockPosition())) {
            if (golem.getNavigation().isDone()) {
                moveToTarget();
            }
            pickupTicks = 0;
            golem.setPickupStatus(0);
            return;
        }
        // In reach: play the little pickup animation, then take the stack.
        if (pickupTicks == 0) {
            target.setPickUpDelay(40);
            golem.setPickupStatus(target.getItem());
            golem.stopInPlace();
        }
        pickupTicks++;
        if (pickupTicks >= 20) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, target.getItem().copy());
            target.setItem(net.minecraft.world.item.ItemStack.EMPTY);
            golem.setPickupStatus(0);
            acquired = true;
        }
    }
}
