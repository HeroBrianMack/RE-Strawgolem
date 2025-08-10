package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.VisionHelper;

public class GolemDepositGoal extends MoveToBlockGoal {
    StrawGolem golem;
    public GolemDepositGoal(StrawGolem golem) {
        super(golem, Golem.defaultWalkSpeed, Golem.searchRange, Golem.searchRangeVertical);
        this.golem = golem;
    }

    @Override
    public void start() {
        blockPos = golem.deliverer.getDeliverable();
        moveMobToBlock();
        this.tryTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
//        System.out.println(getMoveToTarget());
        if (ReachHelper.canReach(mob, blockPos)) {
            golem.deliverer.deliver(golem.level(), blockPos);
            golem.getNavigation().stop();
        }
        else if (golem.isPathFinding()){
//            System.out.println(golem.getNavigation().getPath().canReach());
        }
//        System.out.println(golem.getNavigation().createPath(blockPos.above(), 1).canReach());

    }

    @Override
    public boolean canUse() {
//        System.out.println(golem.deliverer.getDeliverable());
        return mob.hasItemInSlot(EquipmentSlot.MAINHAND) && golem.deliverer.getDeliverable() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && golem.getNavigation().getPath() != null;
    }

    @Override
    protected void moveMobToBlock() {
        this.mob.getNavigation().moveTo((double)this.blockPos.getX() + 0.5, (double)(this.blockPos.getY()), (double)this.blockPos.getZ() + 0.5, 0, this.speedModifier);
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        // Not sure which way is more efficient, or if the order even matters...
        return levelReader.getBlockEntity(blockPos) instanceof Container && VisionHelper.canSee(mob, mob.getOnPos());
    }

}
