package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.SmelterGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;

import java.util.EnumSet;

/**
 * Carries whatever the smelter is holding (usually furnace output) to the
 * bound chest promptly - its hand is pure transport.
 */
public class SmelterStashGoal extends Goal {
    private static final int IDLE_BEFORE_STASH = 20;

    private final SmelterGolem golem;
    private int idleTicks;

    public SmelterStashGoal(SmelterGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.getMainHandItem().isEmpty() || !golem.hasDepositChest()
                || !ContainerHelper.isContainer(golem, golem.getPriorityPos())) {
            idleTicks = 0;
            return false;
        }
        return ++idleTicks >= IDLE_BEFORE_STASH;
    }

    @Override
    public boolean canContinueToUse() {
        return !golem.getMainHandItem().isEmpty()
                && ContainerHelper.isContainer(golem, golem.getPriorityPos());
    }

    @Override
    public void stop() {
        idleTicks = 0;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        BlockPos chest = golem.getPriorityPos();
        golem.getLookControl().setLookAt(chest.getX() + 0.5, chest.getY() + 0.5, chest.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, chest)) {
            golem.getNavigation().moveTo(chest.getX() + 0.5, chest.getY(), chest.getZ() + 0.5, Golem.defaultWalkSpeed);
            return;
        }
        ItemStack held = golem.getMainHandItem();
        golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        golem.depositToChest(held);
    }
}
