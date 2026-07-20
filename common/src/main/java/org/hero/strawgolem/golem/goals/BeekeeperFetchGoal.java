package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BeekeeperGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.platform.Services;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * When a full hive exists and the beekeeper's hand is empty, fetches glass
 * bottles (preferred) or shears from the bound chest.
 */
public class BeekeeperFetchGoal extends Goal {
    private static final int BOTTLE_COUNT = 16;
    private static final Predicate<ItemStack> BOTTLES = s -> s.is(Items.GLASS_BOTTLE);
    private static final Predicate<ItemStack> SHEARS = s -> s.getItem() instanceof ShearsItem;

    private final BeekeeperGolem golem;
    private boolean done;

    public BeekeeperFetchGoal(BeekeeperGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean chestHas(Predicate<ItemStack> pred) {
        Level level = golem.level();
        BlockPos chest = golem.getPriorityPos();
        if (level.getBlockEntity(chest) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (pred.test(container.getItem(i))) {
                    return true;
                }
            }
            return false;
        }
        return !Services.PLATFORM.extractMatching(level, chest, pred, 1, true).isEmpty();
    }

    @Override
    public boolean canUse() {
        return golem.getMainHandItem().isEmpty() && golem.hasDepositChest()
                && ContainerHelper.isContainer(golem, golem.getPriorityPos())
                && BeekeeperHarvestGoal.findFullHive(golem) != null
                && (chestHas(BOTTLES) || chestHas(SHEARS));
    }

    @Override
    public boolean canContinueToUse() {
        return !done && golem.getMainHandItem().isEmpty()
                && ContainerHelper.isContainer(golem, golem.getPriorityPos());
    }

    @Override
    public void start() {
        done = false;
        moveToChest();
    }

    @Override
    public void stop() {
        done = false;
        golem.getNavigation().stop();
    }

    private void moveToChest() {
        BlockPos chest = golem.getPriorityPos();
        golem.getNavigation().moveTo(chest.getX() + 0.5, chest.getY(), chest.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        BlockPos chest = golem.getPriorityPos();
        golem.getLookControl().setLookAt(chest.getX() + 0.5, chest.getY() + 0.5, chest.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, chest)) {
            moveToChest();
            return;
        }
        ItemStack got = withdraw(BOTTLES, BOTTLE_COUNT);
        if (got.isEmpty()) {
            got = withdraw(SHEARS, 1);
        }
        if (!got.isEmpty()) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, got);
        }
        done = true;
    }

    private ItemStack withdraw(Predicate<ItemStack> pred, int max) {
        Level level = golem.level();
        BlockPos chest = golem.getPriorityPos();
        if (level.getBlockEntity(chest) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (pred.test(slot)) {
                    return container.removeItem(i, Math.min(max, slot.getCount()));
                }
            }
            return ItemStack.EMPTY;
        }
        return Services.PLATFORM.extractMatching(level, chest, pred, max, false);
    }
}
