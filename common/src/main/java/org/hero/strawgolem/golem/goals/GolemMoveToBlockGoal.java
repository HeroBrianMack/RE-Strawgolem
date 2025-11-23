package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import org.hero.strawgolem.golem.StrawGolem;

import java.util.List;

public abstract class GolemMoveToBlockGoal extends MoveToBlockGoal {
    public GolemMoveToBlockGoal(PathfinderMob pMob, double pSpeedModifier, int pSearchRange) {
        super(pMob, pSpeedModifier, pSearchRange);
    }
    public GolemMoveToBlockGoal(PathfinderMob pMob, double pSpeedModifier, int pSearchRange, int pVerticalSearchRange) {
        super(pMob, pSpeedModifier, pSearchRange, pVerticalSearchRange);
    }

    // Method to check if there is a collision between straw golems.
    protected boolean golemCollision(StrawGolem golem) {
        return !golem.level().getEntitiesOfClass(StrawGolem.class,
                golem.getBoundingBox().inflate(0.3),
                (gol) -> !gol.position().equals(golem.position())).isEmpty();
    }

    // Method to nudge a golem to the side depending on its direction.
    // North: West
    // South: East
    // West: North
    // East: South
    protected void nudge(StrawGolem golem) {
        Vec3 nudgeDir = Vec3.ZERO;
        Direction dir = golem.getDirection();
        if (dir == Direction.NORTH) {
            nudgeDir = Vec3.atLowerCornerOf(Direction.WEST.getNormal());
        } else if (dir == Direction.SOUTH) {
            nudgeDir = Vec3.atLowerCornerOf(Direction.EAST.getNormal());
        } else if (dir == Direction.WEST) {
            nudgeDir = Vec3.atLowerCornerOf(Direction.NORTH.getNormal());
        } else if (dir == Direction.EAST) {
            nudgeDir = Vec3.atLowerCornerOf(Direction.SOUTH.getNormal());
        }
        // Multiplier to reduce the push force from 1 to 0.05.
        double multiplier = 0.05;
        golem.push(nudgeDir.multiply(multiplier, multiplier, multiplier));
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return false;
    }
}
