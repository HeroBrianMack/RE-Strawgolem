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
 * Keeps furnaces working: fetches fuel for stalled furnaces (input but no fuel)
 * and smeltable input for hungry ones, carrying stacks from the bound chest to
 * the furnace by hand.
 */
public class SmelterSupplyGoal extends Goal {
    private static final int SCAN_COOLDOWN = 40;

    private final SmelterGolem golem;
    private BlockPos furnacePos;
    private boolean wantFuel;
    private boolean fetched;
    private boolean done;
    private int scanCooldown;

    public SmelterSupplyGoal(SmelterGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private AbstractFurnaceBlockEntity furnaceAt(BlockPos pos) {
        return golem.level().getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity f ? f : null;
    }

    /** Finds a furnace needing fuel (preferred - it is stalled) or input the chest can provide. */
    private boolean plan() {
        BlockPos inputTarget = null;
        for (BlockPos pos : golem.findFurnaces()) {
            AbstractFurnaceBlockEntity furnace = furnaceAt(pos);
            if (furnace == null) {
                continue;
            }
            ItemStack input = furnace.getItem(0);
            ItemStack fuel = furnace.getItem(1);
            if (!input.isEmpty() && fuel.isEmpty()
                    && golem.chestHas(AbstractFurnaceBlockEntity::isFuel)) {
                furnacePos = pos;
                wantFuel = true;
                return true;
            }
            if (inputTarget == null && (input.isEmpty() || input.getCount() < input.getMaxStackSize())) {
                final AbstractFurnaceBlockEntity f = furnace;
                final ItemStack current = input.copy();
                boolean hasMatch = golem.chestHas(s -> golem.smeltableIn(f, s)
                        && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, s)));
                if (hasMatch) {
                    inputTarget = pos;
                }
            }
        }
        if (inputTarget != null) {
            furnacePos = inputTarget;
            wantFuel = false;
            return true;
        }
        return false;
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
        return plan();
    }

    @Override
    public boolean canContinueToUse() {
        return !done && furnacePos != null && furnaceAt(furnacePos) != null
                && ContainerHelper.isContainer(golem, golem.getPriorityPos());
    }

    @Override
    public void start() {
        fetched = false;
        done = false;
        moveTo(golem.getPriorityPos());
    }

    @Override
    public void stop() {
        furnacePos = null;
        fetched = false;
        done = false;
        golem.getNavigation().stop();
    }

    private void moveTo(BlockPos pos) {
        golem.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        if (furnacePos == null) {
            return;
        }
        if (!fetched) {
            BlockPos chest = golem.getPriorityPos();
            golem.getLookControl().setLookAt(chest.getX() + 0.5, chest.getY() + 0.5, chest.getZ() + 0.5);
            if (!ReachHelper.canReach(golem, chest)) {
                if (golem.getNavigation().isDone()) {
                    moveTo(chest);
                }
                return;
            }
            AbstractFurnaceBlockEntity furnace = furnaceAt(furnacePos);
            if (furnace == null) {
                done = true;
                return;
            }
            ItemStack got;
            if (wantFuel) {
                got = golem.takeFromChest(AbstractFurnaceBlockEntity::isFuel, 16);
            } else {
                final ItemStack current = furnace.getItem(0).copy();
                got = golem.takeFromChest(s -> golem.smeltableIn(furnace, s)
                        && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, s)), 64);
            }
            if (got.isEmpty()) {
                done = true;
                return;
            }
            golem.setItemSlot(EquipmentSlot.MAINHAND, got);
            fetched = true;
            moveTo(furnacePos);
            return;
        }
        golem.getLookControl().setLookAt(furnacePos.getX() + 0.5, furnacePos.getY() + 0.5, furnacePos.getZ() + 0.5);
        if (!ReachHelper.canReach(golem, furnacePos)
                && furnacePos.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) > 9.0) {
            if (golem.getNavigation().isDone()) {
                moveTo(furnacePos);
            }
            return;
        }
        AbstractFurnaceBlockEntity furnace = furnaceAt(furnacePos);
        ItemStack held = golem.getMainHandItem();
        if (furnace != null && !held.isEmpty()) {
            int slot = wantFuel ? 1 : 0;
            ItemStack current = furnace.getItem(slot);
            if (current.isEmpty()) {
                furnace.setItem(slot, held.copy());
                held = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(current, held)) {
                int move = Math.min(held.getCount(), current.getMaxStackSize() - current.getCount());
                if (move > 0) {
                    current.grow(move);
                    furnace.setItem(slot, current);
                    held.shrink(move);
                }
            }
            golem.setItemSlot(EquipmentSlot.MAINHAND, held);
        }
        done = true;
    }
}
