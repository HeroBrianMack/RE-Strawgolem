package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BreederGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.VisionHelper;
import org.hero.strawgolem.platform.Services;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * When the breeder golem's hand is empty, finds a nearby container holding food
 * that the local animals will eat and withdraws a small stack of it.
 */
public class BreederRestockGoal extends Goal {
    private static final int WITHDRAW_COUNT = 16;

    private final BreederGolem golem;
    private BlockPos targetPos;
    private Predicate<ItemStack> foodFilter;

    public BreederRestockGoal(BreederGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private Predicate<ItemStack> buildFoodFilter() {
        List<Animal> animals = golem.level().getEntitiesOfClass(Animal.class,
                golem.getBoundingBox().inflate(Golem.searchRange), Animal::isAlive);
        if (animals.isEmpty()) {
            return null;
        }
        // Only fetch food that some currently-breedable pair would actually eat
        // (so the golem never hoards food with no takers), or shears whenever an
        // animal in range has wool ready.
        boolean shearWork = BreederShearGoal.findShearable(golem) != null;
        return stack -> !stack.isEmpty() && (BreederBreedGoal.findTarget(golem, stack) != null
                || (shearWork && stack.getItem() instanceof net.minecraft.world.item.ShearsItem));
    }

    private boolean containerHasFood(BlockPos pos) {
        Level level = golem.level();
        if (level.getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (foodFilter.test(container.getItem(i))) {
                    return true;
                }
            }
            return false;
        }
        return !Services.PLATFORM.extractMatching(level, pos, foodFilter, WITHDRAW_COUNT, true).isEmpty();
    }

    @Override
    public boolean canUse() {
        if (!golem.getMainHandItem().isEmpty()) {
            return false;
        }
        foodFilter = buildFoodFilter();
        if (foodFilter == null) {
            return false;
        }
        // A player-bound chest (golem orderer) always wins if it has usable food.
        BlockPos prio = golem.getPriorityPos();
        if (prio.getX() != Integer.MAX_VALUE && ContainerHelper.isContainer(golem, prio)
                && containerHasFood(prio) && ReachHelper.canPath(golem, prio)) {
            targetPos = prio;
            return true;
        }
        org.hero.strawgolem.golem.api.BiPredicate<BlockPos> pred = (gol, pos) ->
                ContainerHelper.isContainer(gol, pos) && containerHasFood(pos) && ReachHelper.canPath(gol, pos);
        targetPos = VisionHelper.findNearestBlock(golem, pred);
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null && golem.getMainHandItem().isEmpty()
                && ContainerHelper.isContainer(golem, targetPos);
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void stop() {
        targetPos = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            return;
        }
        golem.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, targetPos)) {
            golem.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, Golem.defaultWalkSpeed);
            return;
        }
        ItemStack got = withdraw();
        if (!got.isEmpty()) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, got);
        }
        targetPos = null;
    }

    private ItemStack withdraw() {
        Level level = golem.level();
        if (level.getBlockEntity(targetPos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (foodFilter.test(slot)) {
                    return container.removeItem(i, Math.min(WITHDRAW_COUNT, slot.getCount()));
                }
            }
            return ItemStack.EMPTY;
        }
        return Services.PLATFORM.extractMatching(level, targetPos, foodFilter, WITHDRAW_COUNT, false);
    }
}
