package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.MinerGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.platform.Services;

import java.util.EnumSet;

/**
 * When mining work exists but the miner's hand is empty, fetches a pickaxe
 * from the bound chest.
 */
public class MinerFetchGoal extends Goal {
    private final MinerGolem golem;
    private boolean done;

    public MinerFetchGoal(MinerGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean chestHasPickaxe() {
        Level level = golem.level();
        BlockPos chest = golem.getPriorityPos();
        if (level.getBlockEntity(chest) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (golem.isMiningTool(container.getItem(i))) {
                    return true;
                }
            }
            return false;
        }
        return !Services.PLATFORM.extractMatching(level, chest,
                golem::isMiningTool, 1, true).isEmpty();
    }

    @Override
    public boolean canUse() {
        return golem.getMainHandItem().isEmpty() && golem.hasMineFilter() && golem.hasDepositChest()
                && ContainerHelper.isContainer(golem, golem.getPriorityPos())
                && MinerMineGoal.findTargetBlock(golem) != null && chestHasPickaxe();
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
        ItemStack pick = withdrawPickaxe();
        if (!pick.isEmpty()) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, pick);
        }
        done = true;
    }

    private ItemStack withdrawPickaxe() {
        Level level = golem.level();
        BlockPos chest = golem.getPriorityPos();
        if (level.getBlockEntity(chest) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (golem.isMiningTool(container.getItem(i))) {
                    return container.removeItem(i, 1);
                }
            }
            return ItemStack.EMPTY;
        }
        return Services.PLATFORM.extractMatching(level, chest,
                golem::isMiningTool, 1, false);
    }
}
