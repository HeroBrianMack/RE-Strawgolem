package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.VisionHelper;

public class GolemDepositGoal extends GolemMoveToBlockGoal {
    private StrawGolem golem;
    public GolemDepositGoal(StrawGolem golem) {
        super(golem, Golem.defaultWalkSpeed, Golem.searchRange, Golem.searchRangeVertical);
        this.golem = golem;
    }

    @Override
    public void start() {
        blockPos = golem.deliverer.getDeliverable();
        // Safety check in case getDeliverable fails
        if (blockPos == null) {
            System.out.println("START ERROR");
            stop();
            return;
        }
        moveMobToBlock();
        this.tryTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
//        System.out.println(getMoveToTarget());
        if (this.blockPos == null){
            Constants.LOG.error("Missing block position!");
        } else if (ReachHelper.canReach(mob, blockPos)) {
            golem.deliverer.deliver(golem.level(), blockPos);
            golem.getNavigation().stop();
        } else if (shouldRecalculatePath() && golemCollision(golem)) {
            nudge(golem);
        }
    }

    @Override
    public boolean canUse() {
        return mob.hasItemInSlot(EquipmentSlot.MAINHAND) && golem.deliverer.getDeliverable() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.blockPos != null && canUse() && golem.getNavigation().getPath() != null;
    }

    @Override
    protected void moveMobToBlock() {
        try {
            this.mob.getNavigation().moveTo((double) this.blockPos.getX() + 0.5, (double) (this.blockPos.getY()), (double) this.blockPos.getZ() + 0.5, 0, this.speedModifier);
        } catch(Exception e) {
            stop();
            Constants.LOG.error(e.getMessage());
        }

    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        // Not sure which way is more efficient, or if the order even matters...
        return levelReader.getBlockEntity(blockPos) instanceof Container && VisionHelper.canSee(mob, mob.getOnPos());
    }

}
