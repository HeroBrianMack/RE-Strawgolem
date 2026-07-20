package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.StockGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.platform.Services;

import java.util.EnumSet;

/**
 * Withdraws from the stock golem's bound source container the first stack that
 * the bound destination container can (at least partially) accept.
 */
public class StockWithdrawGoal extends Goal {
    private final StockGolem golem;
    private boolean done;

    public StockWithdrawGoal(StockGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean bindingsValid() {
        return golem.hasSourcePos() && golem.hasDestinationPos()
                && ContainerHelper.isContainer(golem, golem.getSourcePos())
                && ContainerHelper.isContainer(golem, golem.getPriorityPos());
    }

    /** How many of this stack the destination could take right now. */
    private int destFit(ItemStack stack) {
        Level level = golem.level();
        BlockPos dest = golem.getPriorityPos();
        if (level.getBlockEntity(dest) instanceof Container container) {
            int fit = 0;
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (slot.isEmpty()) {
                    fit += Math.min(container.getMaxStackSize(), stack.getMaxStackSize());
                } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                    fit += Math.max(0, Math.min(container.getMaxStackSize(), slot.getMaxStackSize()) - slot.getCount());
                }
                if (fit >= stack.getCount()) {
                    return stack.getCount();
                }
            }
            return fit;
        }
        ItemStack remainder = Services.PLATFORM.insertItemSimulate(level, dest, stack);
        return stack.getCount() - remainder.getCount();
    }

    private boolean sourceHasMoveable() {
        Level level = golem.level();
        BlockPos src = golem.getSourcePos();
        if (level.getBlockEntity(src) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (!slot.isEmpty() && golem.filterAllows(slot) && destFit(slot) > 0) {
                    return true;
                }
            }
            return false;
        }
        return !Services.PLATFORM.extractMatching(level, src,
                s -> !s.isEmpty() && golem.filterAllows(s) && destFit(s) > 0, 1, true).isEmpty();
    }

    @Override
    public boolean canUse() {
        return golem.getMainHandItem().isEmpty() && bindingsValid() && sourceHasMoveable();
    }

    @Override
    public boolean canContinueToUse() {
        return !done && golem.getMainHandItem().isEmpty() && bindingsValid();
    }

    @Override
    public void start() {
        done = false;
        moveToSource();
    }

    @Override
    public void stop() {
        done = false;
        golem.getNavigation().stop();
    }

    private void moveToSource() {
        BlockPos src = golem.getSourcePos();
        golem.getNavigation().moveTo(src.getX() + 0.5, src.getY(), src.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        BlockPos src = golem.getSourcePos();
        golem.getLookControl().setLookAt(src.getX() + 0.5, src.getY() + 0.5, src.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, src)) {
            moveToSource();
            return;
        }
        ItemStack got = withdraw();
        if (!got.isEmpty()) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, got);
        }
        done = true;
    }

    private ItemStack withdraw() {
        Level level = golem.level();
        BlockPos src = golem.getSourcePos();
        if (level.getBlockEntity(src) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (slot.isEmpty() || !golem.filterAllows(slot)) {
                    continue;
                }
                int fit = destFit(slot);
                if (fit > 0) {
                    return container.removeItem(i, Math.min(fit, slot.getCount()));
                }
            }
            return ItemStack.EMPTY;
        }
        // Capability path: find a candidate, then extract only what fits.
        ItemStack candidate = Services.PLATFORM.extractMatching(level, src,
                s -> !s.isEmpty() && golem.filterAllows(s) && destFit(s) > 0, Integer.MAX_VALUE, true);
        if (candidate.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int fit = destFit(candidate);
        if (fit <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack finalCandidate = candidate;
        return Services.PLATFORM.extractMatching(level, src,
                s -> ItemStack.isSameItemSameComponents(s, finalCandidate), fit, false);
    }
}
