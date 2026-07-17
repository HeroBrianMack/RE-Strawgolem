package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.SmelterGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;

import java.util.EnumSet;

/**
 * Empties finished output from furnaces in range into the golem's hand; the
 * stash goal then carries it to the bound chest.
 */
public class SmelterCollectGoal extends Goal {
    private static final int SCAN_COOLDOWN = 40;

    private final SmelterGolem golem;
    private BlockPos furnacePos;
    private boolean done;
    private int scanCooldown;

    public SmelterCollectGoal(SmelterGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private BlockPos findOutput() {
        for (BlockPos pos : golem.findFurnaces()) {
            if (golem.level().getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace
                    && !furnace.getItem(2).isEmpty()) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        if (!golem.getMainHandItem().isEmpty() || !golem.hasDepositChest()
                || !ContainerHelper.isContainer(golem, golem.getPriorityPos())) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_COOLDOWN;
        furnacePos = findOutput();
        return furnacePos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !done && furnacePos != null && golem.getMainHandItem().isEmpty()
                && golem.level().getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity f
                && !f.getItem(2).isEmpty();
    }

    @Override
    public void start() {
        done = false;
        moveToFurnace();
    }

    @Override
    public void stop() {
        furnacePos = null;
        done = false;
        golem.getNavigation().stop();
    }

    private void moveToFurnace() {
        golem.getNavigation().moveTo(furnacePos.getX() + 0.5, furnacePos.getY(), furnacePos.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        if (furnacePos == null) {
            return;
        }
        golem.getLookControl().setLookAt(furnacePos.getX() + 0.5, furnacePos.getY() + 0.5, furnacePos.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, furnacePos)
                && furnacePos.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) > 9.0) {
            if (golem.getNavigation().isDone()) {
                moveToFurnace();
            }
            return;
        }
        if (golem.level().getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity furnace) {
            ItemStack out = furnace.getItem(2);
            if (!out.isEmpty()) {
                golem.setItemSlot(EquipmentSlot.MAINHAND, furnace.removeItem(2, out.getCount()));
            }
        }
        done = true;
    }
}
