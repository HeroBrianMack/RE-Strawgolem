package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.MinerGolem;
import org.hero.strawgolem.golem.api.ReachHelper;

import java.util.EnumSet;

/**
 * Walks to the nearest filter-matching block in range and mines it over time
 * (with crack animation). Drops are computed with the held pickaxe (fortune and
 * silk touch apply) and deposited straight into the golem's bound chest.
 */
public class MinerMineGoal extends Goal {
    private static final int BREAK_TICKS = 40;
    private static final int SCAN_COOLDOWN = 40;

    private final MinerGolem golem;
    private BlockPos target;
    private int breakTicks;
    private int scanCooldown;

    public MinerMineGoal(MinerGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean holdingPickaxe(MinerGolem golem) {
        return golem.isMiningTool(golem.getMainHandItem());
    }

    /** Nearest matching, breakable block in the golem's working volume. */
    public static BlockPos findTargetBlock(MinerGolem golem) {
        if (!golem.hasMineFilter()) {
            return null;
        }
        BlockPos center = golem.blockPosition();
        int r = Golem.searchRange;
        int rv = Golem.searchRangeVertical;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -rv, -r), center.offset(r, rv, r))) {
            BlockState state = golem.level().getBlockState(pos);
            if (!golem.filterMatches(state.getBlock()) || state.getDestroySpeed(golem.level(), pos) < 0) {
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
        if (!holdingPickaxe(golem) || !golem.hasDepositChest()) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_COOLDOWN;
        target = findTargetBlock(golem);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && holdingPickaxe(golem)
                && golem.filterMatches(golem.level().getBlockState(target).getBlock());
    }

    @Override
    public void start() {
        breakTicks = 0;
        moveToTarget();
    }

    @Override
    public void stop() {
        if (target != null) {
            golem.level().destroyBlockProgress(golem.getId(), target, -1);
        }
        target = null;
        breakTicks = 0;
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
        if (!ReachHelper.canReach(golem, target) && target.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) > 9.0) {
            if (golem.getNavigation().isDone()) {
                moveToTarget();
            }
            breakTicks = 0;
            return;
        }
        breakTicks++;
        golem.level().destroyBlockProgress(golem.getId(), target, Math.min(9, breakTicks * 10 / BREAK_TICKS));
        if (breakTicks < BREAK_TICKS) {
            return;
        }
        // Break: compute drops with the pickaxe so enchantments apply, then deposit.
        if (golem.level() instanceof ServerLevel level) {
            BlockState state = level.getBlockState(target);
            ItemStack pick = golem.getMainHandItem();
            var drops = Block.getDrops(state, level, target, level.getBlockEntity(target), golem, pick);
            level.destroyBlock(target, false, golem);
            for (ItemStack drop : drops) {
                golem.depositToChest(drop);
            }
            pick.hurtAndBreak(1, golem, EquipmentSlot.MAINHAND);
        }
        golem.level().destroyBlockProgress(golem.getId(), target, -1);
        target = null;
        breakTicks = 0;
    }
}
