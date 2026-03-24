package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.VisionHelper;

public class GolemDepositGoal extends GolemMoveToBlockGoal {
    private StrawGolem golem;
    private boolean done = false;
    public GolemDepositGoal(StrawGolem golem) {
        super(golem, Golem.defaultWalkSpeed, Golem.searchRange, Golem.searchRangeVertical);
        this.golem = golem;
    }

    @Override
    public void start() {
        // Safety check in case getDeliverable fails
        if (blockPos == null) {
            Constants.LOG.error("Deposit Start Error!");
            stop();
            done = true;
        } else {
            moveMobToBlock();
            this.tryTicks = 0;
        }
    }

    @Override
    public void tick() {
        // Hopefully unneeded try-catch, but I want players to never have an unwanted crash.
        try {
            // This should never have been possible, but it is happening.
            if (this.blockPos == null) {
                Constants.LOG.error("Missing block position!");
                return;
            }
            super.tick();
            if (mob.hasItemInSlot(EquipmentSlot.MAINHAND) && ReachHelper.canReach(mob, blockPos)) {
                golem.deliverer.deliver(golem.level(), blockPos);
                tryPlaySound();
                golem.getNavigation().stop();
                done = true;
            } else if (shouldRecalculatePath() && golemCollision(golem)) {
                nudge(golem);
            }
        } catch (Throwable e) {
            Constants.LOG.error("Golem Deposit Goal has experienced an error: {}", e);
        }
    }

    @Override
    public boolean canUse() {
        if (blockPos == null) {
            blockPos = golem.deliverer.getDeliverable();
        }
        return mob.hasItemInSlot(EquipmentSlot.MAINHAND)
                && blockPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !done && ContainerHelper.isContainer(mob, blockPos)
                && canUse() && golem.getNavigation().getPath() != null;
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

    private SoundEvent tryParseSound() {
        try {
            BlockEntity block = golem.level().getBlockEntity(blockPos);
            if (block == null) return null;

            ResourceLocation location = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(block.getType());
            if (location == null) return null;
            return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse("block." + location.getPath() + ".open"));
        } catch (Throwable e) {
            return null;
        }
    }

    private void tryPlaySound() {
        SoundEvent event = tryParseSound();
        if (event == null) {
            event = SoundEvents.CHEST_OPEN;
        }
        golem.level().playSound(null, blockPos, event,
                SoundSource.BLOCKS, 0.5F, golem.level().random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        // Not sure which way is more efficient, or if the order even matters...
        return levelReader.getBlockEntity(blockPos) instanceof Container && VisionHelper.canSee(mob, mob.getOnPos());
    }

}
