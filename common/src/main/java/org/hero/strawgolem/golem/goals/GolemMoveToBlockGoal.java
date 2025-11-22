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

    protected boolean golemCollision(StrawGolem golem) {
        return !golem.level().getEntitiesOfClass(StrawGolem.class,
                golem.getBoundingBox().inflate(0.4),
                (gol) -> !gol.position().equals(golem.position())).isEmpty();
    }

    protected void nudge(StrawGolem golem, Vec3 pos) {
        // Normalizing the golem's pos with the other pos
        Vec3 dim = golem.position().subtract(pos).normalize();
        double multiplier = 0.05;
        golem.push(dim.multiply(multiplier, multiplier, multiplier));
    }

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
        double multiplier = 0.05;
        golem.push(nudgeDir.multiply(0.05, 0.05, 0.05));
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return false;
    }
}
