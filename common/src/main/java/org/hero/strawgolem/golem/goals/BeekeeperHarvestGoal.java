package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BeekeeperGolem;

import java.util.EnumSet;

/**
 * Walks to full beehives and harvests them with whatever the golem is holding:
 * glass bottles become honey bottles, shears take honeycomb. Products go
 * straight into the bound chest. The bees, recognizing a kindred spirit made
 * of straw, are never angered.
 */
public class BeekeeperHarvestGoal extends Goal {
    private static final int HARVEST_TICKS = 20;

    private final BeekeeperGolem golem;
    private BlockPos target;
    private int harvestTicks;

    public BeekeeperHarvestGoal(BeekeeperGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean holdingHarvestTool(BeekeeperGolem golem) {
        ItemStack held = golem.getMainHandItem();
        return held.is(Items.GLASS_BOTTLE) || held.getItem() instanceof ShearsItem;
    }

    /** Nearest full hive (vanilla beehive/bee nest, or anything extending BeehiveBlock). */
    public static BlockPos findFullHive(BeekeeperGolem golem) {
        BlockPos center = golem.blockPosition();
        int r = Golem.searchRange;
        int rv = Golem.searchRangeVertical;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -rv, -r), center.offset(r, rv, r))) {
            BlockState state = golem.level().getBlockState(pos);
            if (!(state.getBlock() instanceof BeehiveBlock)) {
                continue;
            }
            if (!state.hasProperty(BeehiveBlock.HONEY_LEVEL) || state.getValue(BeehiveBlock.HONEY_LEVEL) < 5) {
                continue;
            }
            double dist = pos.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    @Override
    public boolean canUse() {
        if (!holdingHarvestTool(golem) || !golem.hasDepositChest()) {
            return false;
        }
        target = findFullHive(golem);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !holdingHarvestTool(golem)) {
            return false;
        }
        BlockState state = golem.level().getBlockState(target);
        return state.getBlock() instanceof BeehiveBlock
                && state.hasProperty(BeehiveBlock.HONEY_LEVEL)
                && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5;
    }

    @Override
    public void start() {
        harvestTicks = 0;
        moveToTarget();
    }

    @Override
    public void stop() {
        target = null;
        harvestTicks = 0;
        golem.getNavigation().stop();
    }

    private void moveToTarget() {
        golem.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        if (target.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) > 9.0) {
            if (golem.getNavigation().isDone()) {
                moveToTarget();
            }
            harvestTicks = 0;
            return;
        }
        if (++harvestTicks < HARVEST_TICKS) {
            return;
        }
        BlockState state = golem.level().getBlockState(target);
        ItemStack held = golem.getMainHandItem();
        if (held.is(Items.GLASS_BOTTLE)) {
            held.shrink(1);
            golem.setItemSlot(EquipmentSlot.MAINHAND, held);
            golem.depositToChest(new ItemStack(Items.HONEY_BOTTLE));
            golem.level().playSound(null, target, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            golem.depositToChest(new ItemStack(Items.HONEYCOMB, 3));
            held.hurtAndBreak(1, golem, EquipmentSlot.MAINHAND);
            golem.level().playSound(null, target, SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        // Straw golems don't anger bees: just reset the honey level, no smoke needed.
        golem.level().setBlockAndUpdate(target, state.setValue(BeehiveBlock.HONEY_LEVEL, 0));
        target = null;
        harvestTicks = 0;
    }
}
