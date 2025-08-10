package org.hero.strawgolem.golem.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.StrawGolem;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class VisionHelper {
    public static boolean canSee(Entity pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            // Yes this is overengineered...
            String error = "";
            if (pos1 == null) error += "Entity";
            if (pos2 == null) error += "BlockPos";
            Constants.LOG.error("VisonHelper error! " + error);
            return false;
        }
        if (Math.abs(pos1.getY() - pos2.getY()) > Golem.searchRangeVertical) return false;
        return Math.sqrt(Math.pow(pos1.getX() - pos2.getX(), 2) + Math.pow(pos1.getZ() - pos2.getZ(), 2)) <= Golem.searchRange;
    }

    public static BlockPos findNearestBlock(StrawGolem golem, BiPredicate test) {
//        if (storagePos != null && VisionHelper.canSee(StrawGolem.this, storagePos)) return storagePos;
        int range = Constants.Golem.searchRange;
        BlockPos closest = null;
        BlockPos query = golem.blockPosition();
        for (int x = -range; x <= range; ++x) {
            for (int y = -range / 2; y <= range / 2; ++y) {
                for (int z = -range; z <= range; ++z) {
                    BlockPos pos = query.offset(x, y, z);
//                        System.out.println(ContainerHelper.isContainer(StrawGolem.this.level(), pos) + " " + VisionHelper.canSee(StrawGolem.this, pos));
                    if (test.filter(golem, pos)
                        /*&& !invalidContainers.contains(pos)*/) {
//                        golem.getNavigation()
                        // Should find the closest deliverable...
                        closest = closest == null || query.distManhattan(pos) < query.distManhattan(closest) ? pos : closest;
//                            containerSet.add(pos);
                    }
                }
            }
        }
//        storagePos = storagePos == null ? closest : storagePos;
        return closest;
//        return null;
    }

    public static Queue<BlockPos> nearbyBlocks(StrawGolem golem, BiPredicate test) {
//        if (storagePos != null && VisionHelper.canSee(StrawGolem.this, storagePos)) return storagePos;
        int range = Constants.Golem.searchRange;
        Queue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingInt(pos -> pos.distManhattan(golem.blockPosition())));
        BlockPos closest = null;
        BlockPos query = golem.blockPosition();
        for (int x = -range; x <= range; ++x) {
            for (int y = -range / 2; y <= range / 2; ++y) {
                for (int z = -range; z <= range; ++z) {
                    BlockPos pos = query.offset(x, y, z);
//                        System.out.println(ContainerHelper.isContainer(StrawGolem.this.level(), pos) + " " + VisionHelper.canSee(StrawGolem.this, pos));
                    if (test.filter(golem, pos)
                        /*&& !invalidContainers.contains(pos)*/) {
//                        golem.getNavigation()
                        // Should find the closest deliverable...
                        closest = closest == null || query.distManhattan(pos) < query.distManhattan(closest) ? pos : closest;
                            queue.add(pos);
                    }
                }
            }
        }
//        storagePos = storagePos == null ? closest : storagePos;
        return queue;
//        return null;
    }
}
