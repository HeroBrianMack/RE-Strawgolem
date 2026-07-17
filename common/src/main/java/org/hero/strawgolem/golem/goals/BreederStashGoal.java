package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BreederGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.VisionHelper;

import java.util.EnumSet;

/**
 * When the breeder golem is holding food nobody currently wants (everyone is on
 * breeding cooldown, or the animals that eat it are gone), it returns the food
 * to a container after a short grace period so it can fetch something useful
 * later - instead of hugging a stack of wheat forever.
 */
public class BreederStashGoal extends Goal {
    /** Grace period (ticks) of "no takers" before giving the food back. */
    private static final int IDLE_BEFORE_STASH = 100;
    /** Much shorter wait for items that are not food for any nearby species. */
    private static final int IDLE_BEFORE_STASH_NONFOOD = 20;

    private final BreederGolem golem;
    private BlockPos chestPos;
    private int idleTicks;

    public BreederStashGoal(BreederGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private BlockPos findStashContainer() {
        BlockPos prio = golem.getPriorityPos();
        if (prio.getX() != Integer.MAX_VALUE && ContainerHelper.isContainer(golem, prio)
                && ReachHelper.canPath(golem, prio)) {
            return prio;
        }
        org.hero.strawgolem.golem.api.BiPredicate<BlockPos> pred = (gol, pos) ->
                ContainerHelper.isContainer(gol, pos) && ReachHelper.canPath(gol, pos);
        return VisionHelper.findNearestBlock(golem, pred);
    }

    @Override
    public boolean canUse() {
        if (golem.getMainHandItem().isEmpty()) {
            idleTicks = 0;
            return false;
        }
        if (BreederBreedGoal.findTarget(golem, golem.getMainHandItem()) != null) {
            idleTicks = 0;
            return false;
        }
        if (BreederShearGoal.holdingShears(golem) && BreederShearGoal.findShearable(golem) != null) {
            idleTicks = 0;
            return false;
        }
        boolean foodForSomeone = !golem.level().getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class,
                golem.getBoundingBox().inflate(org.hero.strawgolem.Constants.Golem.searchRange),
                a -> a.isAlive() && a.isFood(golem.getMainHandItem())).isEmpty();
        if (++idleTicks < (foodForSomeone ? IDLE_BEFORE_STASH : IDLE_BEFORE_STASH_NONFOOD)) {
            return false;
        }
        chestPos = findStashContainer();
        return chestPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return chestPos != null && !golem.getMainHandItem().isEmpty()
                && ContainerHelper.isContainer(golem, chestPos);
    }

    @Override
    public void start() {
        moveToChest();
    }

    @Override
    public void stop() {
        chestPos = null;
        idleTicks = 0;
        golem.getNavigation().stop();
    }

    private void moveToChest() {
        golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        golem.getLookControl().setLookAt(chestPos.getX() + 0.5, chestPos.getY() + 0.5, chestPos.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, chestPos)) {
            moveToChest();
            return;
        }
        // Shared delivery logic handles insertion + keeps any remainder in hand.
        golem.deliverer.deliver(golem.level(), chestPos);
    }
}
