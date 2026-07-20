package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.LumberjackGolem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fells a tree from its base: chops the connected log cluster bottom-up (only
 * clusters with leaves attached count as trees, so builds are safe), deposits
 * the logs, then replants a sapling from the chest on the stump.
 */
public class LumberChopGoal extends Goal {
    private static final int CHOP_TICKS = 20;
    private static final int SCAN_COOLDOWN = 60;
    private static final int MAX_CLUSTER = 96;
    private static final int MIN_LEAVES = 3;

    private final LumberjackGolem golem;
    private BlockPos basePos;
    private List<BlockPos> cluster;
    private Block baseLog;
    private int chopTicks;
    private int scanCooldown;

    public LumberChopGoal(LumberjackGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean holdingAxe(LumberjackGolem golem) {
        return golem.getMainHandItem().getItem() instanceof AxeItem;
    }

    /** BFS over connected logs; returns null unless it looks like a real tree. */
    private static List<BlockPos> collectTree(LumberjackGolem golem, BlockPos base) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> logs = new ArrayList<>();
        int leaves = 0;
        queue.add(base);
        seen.add(base);
        while (!queue.isEmpty() && logs.size() < MAX_CLUSTER) {
            BlockPos pos = queue.poll();
            BlockState state = golem.level().getBlockState(pos);
            if (state.is(BlockTags.LOGS)) {
                logs.add(pos);
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos next = pos.offset(dx, dy, dz);
                            if (seen.add(next)) {
                                BlockState ns = golem.level().getBlockState(next);
                                if (ns.is(BlockTags.LOGS)) {
                                    queue.add(next);
                                } else if (ns.is(BlockTags.LEAVES)) {
                                    leaves++;
                                }
                            }
                        }
            }
        }
        if (leaves < MIN_LEAVES) {
            return null;
        }
        logs.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY)
                .thenComparingDouble(p -> p.distSqr(base)));
        return logs;
    }

    /** Nearest log standing on dirt whose cluster qualifies as a tree. */
    public static BlockPos findTreeBase(LumberjackGolem golem) {
        BlockPos center = golem.blockPosition();
        int r = Golem.searchRange;
        int rv = Golem.searchRangeVertical;
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -rv, -r), center.offset(r, rv, r))) {
            if (golem.level().getBlockState(pos).is(BlockTags.LOGS)
                    && golem.level().getBlockState(pos.below()).is(BlockTags.DIRT)) {
                candidates.add(pos.immutable());
            }
        }
        candidates.sort(Comparator.comparingDouble(p -> p.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ())));
        for (int i = 0; i < Math.min(5, candidates.size()); i++) {
            if (collectTree(golem, candidates.get(i)) != null) {
                return candidates.get(i);
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        if (!holdingAxe(golem) || !golem.hasDepositChest()) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_COOLDOWN;
        basePos = findTreeBase(golem);
        if (basePos == null) {
            return false;
        }
        cluster = collectTree(golem, basePos);
        if (cluster == null || cluster.isEmpty()) {
            basePos = null;
            return false;
        }
        baseLog = golem.level().getBlockState(basePos).getBlock();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return basePos != null && cluster != null && !cluster.isEmpty() && holdingAxe(golem);
    }

    @Override
    public void start() {
        chopTicks = 0;
        moveToBase();
    }

    @Override
    public void stop() {
        if (!cluster().isEmpty()) {
            golem.level().destroyBlockProgress(golem.getId(), cluster.get(0), -1);
        }
        basePos = null;
        cluster = null;
        chopTicks = 0;
        golem.getNavigation().stop();
    }

    private List<BlockPos> cluster() {
        return cluster == null ? List.of() : cluster;
    }

    private void moveToBase() {
        golem.getNavigation().moveTo(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        if (basePos == null || cluster == null || cluster.isEmpty()) {
            return;
        }
        golem.getLookControl().setLookAt(basePos.getX() + 0.5, basePos.getY() + 0.5, basePos.getZ() + 0.5);
        if (basePos.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) > 9.0) {
            if (golem.getNavigation().isDone()) {
                moveToBase();
            }
            chopTicks = 0;
            return;
        }
        BlockPos current = cluster.get(0);
        BlockState state = golem.level().getBlockState(current);
        if (!state.is(BlockTags.LOGS)) {
            cluster.remove(0);
            chopTicks = 0;
            return;
        }
        chopTicks++;
        golem.level().destroyBlockProgress(golem.getId(), current, Math.min(9, chopTicks * 10 / CHOP_TICKS));
        if (chopTicks < CHOP_TICKS) {
            return;
        }
        if (golem.level() instanceof ServerLevel level) {
            ItemStack axe = golem.getMainHandItem();
            var drops = Block.getDrops(state, level, current, level.getBlockEntity(current), golem, axe);
            level.destroyBlock(current, false, golem);
            for (ItemStack drop : drops) {
                golem.depositToChest(drop);
            }
            axe.hurtAndBreak(1, golem, EquipmentSlot.MAINHAND);
        }
        golem.level().destroyBlockProgress(golem.getId(), current, -1);
        cluster.remove(0);
        chopTicks = 0;
        if (cluster.isEmpty()) {
            replant();
            basePos = null;
        }
    }

    /** Plants a sapling from the chest on the stump: matching wood first, any sapling as fallback. */
    private void replant() {
        if (!golem.level().getBlockState(basePos).isAir()
                || !golem.level().getBlockState(basePos.below()).is(BlockTags.DIRT)) {
            return;
        }
        ItemStack sapling = ItemStack.EMPTY;
        ResourceLocation logId = BuiltInRegistries.BLOCK.getKey(baseLog);
        if (logId.getPath().endsWith("_log")) {
            ResourceLocation saplingId = ResourceLocation.tryBuild(logId.getNamespace(),
                    logId.getPath().substring(0, logId.getPath().length() - 4) + "_sapling");
            if (saplingId != null) {
                var item = BuiltInRegistries.ITEM.getOptional(saplingId);
                if (item.isPresent()) {
                    ItemStack want = new ItemStack(item.get());
                    sapling = golem.takeFromChest(s -> ItemStack.isSameItem(s, want), 1);
                }
            }
        }
        if (sapling.isEmpty()) {
            sapling = golem.takeFromChest(s -> s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof SaplingBlock, 1);
        }
        if (!sapling.isEmpty() && sapling.getItem() instanceof BlockItem blockItem) {
            golem.level().setBlockAndUpdate(basePos, blockItem.getBlock().defaultBlockState());
        }
    }
}
