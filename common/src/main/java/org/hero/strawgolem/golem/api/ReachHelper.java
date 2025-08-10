package org.hero.strawgolem.golem.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import org.hero.strawgolem.Constants;

public final class ReachHelper {
    public static boolean canPath(Mob mob, BlockPos pos) {
        Path path = mob.getNavigation().createPath(pos, 1);
        return path != null && path.canReach();
    }

    public static boolean canReach(Mob mob, BlockPos pos) {
        return pos.closerToCenterThan(mob.position(), Constants.Golem.depositDistance);
    }

}
