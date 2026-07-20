package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.StockGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;

import java.util.EnumSet;

/**
 * Delivers whatever the stock golem is holding strictly to its bound destination
 * container (no nearest-chest fallback - couriers do not freelance).
 */
public class StockDeliverGoal extends Goal {
    private final StockGolem golem;

    public StockDeliverGoal(StockGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean destValid() {
        return golem.hasDestinationPos() && ContainerHelper.isContainer(golem, golem.getPriorityPos());
    }

    @Override
    public boolean canUse() {
        return !golem.getMainHandItem().isEmpty() && destValid();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        moveToDest();
    }

    @Override
    public void stop() {
        golem.getNavigation().stop();
    }

    private void moveToDest() {
        BlockPos dest = golem.getPriorityPos();
        golem.getNavigation().moveTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        BlockPos dest = golem.getPriorityPos();
        golem.getLookControl().setLookAt(dest.getX() + 0.5, dest.getY() + 0.5, dest.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, dest)) {
            moveToDest();
            return;
        }
        // Reuses the shared delivery logic (double chests, capability inventories,
        // stack-safe insertion, remainder kept in hand).
        golem.deliverer.deliver(golem.level(), dest);
    }
}
