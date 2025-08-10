package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.BiPredicate;
import org.hero.strawgolem.golem.api.VisionHelper;

import java.util.Collections;
import java.util.Queue;

public class GolemHarvestGoal extends MoveToBlockGoal {
    StrawGolem golem;
    Queue<BlockPos> queue;
    int harvestTimer = 0;
    ItemStack item;
    BiPredicate predicate = (gol, pos) -> /*VisionHelper.canSee(gol, pos) && */isGrownPlant(gol.level(), pos) && ReachHelper.canPath(gol, pos);
    public GolemHarvestGoal(StrawGolem golem) {
        super(golem, Constants.Golem.defaultWalkSpeed, Constants.Golem.searchRange, Constants.Golem.searchRangeVertical);
        this.golem = golem;
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return VisionHelper.canSee(golem, blockPos) && predicate.filter(golem, blockPos);
//        return VisionHelper.canSee(mob, blockPos) && ReachHelper.canPath(mob, blockPos) && isPlant(levelReader, blockPos);
    }

    @Override
    public void tick() {
//        super.tick();
//        if ()
//        System.out.println(blockPos);
//        if (golem.get)
//        System.out.println(blockPos.distToCenterSqr(mob.position()) + " " + (mob.getNavigation().getPath() == null ? true : mob.getNavigation().getPath().getDistToTarget()));
        if (ReachHelper.canReach(mob, blockPos) && item == null) {
            // harvest time!
            item = harvest();
            golem.setPickupStatus(item);
            golem.getNavigation().stop();
        } else if (item != null && !item.isEmpty()) {
            harvestTimer++;
            System.out.println(harvestTimer);
            if (harvestTimer == 20 && predicate.filter(golem, blockPos) && ReachHelper.canReach(mob, blockPos)) {
                // Double checking this, not going to let someone change a block
                System.out.println("HARVEST!");
                item = harvest();
                golem.setItemSlot(EquipmentSlot.MAINHAND, item);
            }
            if (harvestTimer == 38) {
                System.out.println("QUEUE");
                golem.setPickupStatus(0);
            }
        }


    }
    @Override
    public void start() {
//        System.out.println((blockPos != null) + " Start");
        if (blockPos == null) blockPos = queue.poll();
//        super.start();
        moveMobToBlock();
        this.tryTicks = 0;
        harvestTimer = 0;
        item = null;
//        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
//        this.mob.getNavigation().moveTo((double)this.blockPos.getX() + 0.5, (double)(this.blockPos.getY()), (double)this.blockPos.getZ() + 0.5, this.speedModifier);
    }

    @Override
    protected void moveMobToBlock() {
        this.mob.getNavigation().moveTo((double)this.blockPos.getX() + 0.5, (double)(this.blockPos.getY()), (double)this.blockPos.getZ() + 0.5, 0, this.speedModifier);
    }

    @Override
    public boolean canUse() {
        // maybe add a delay before allowing golem to harvest again... could solve lag
        if (golem.getMainHandItem().isEmpty() && queue == null) {
            System.out.println("can");
            queue = VisionHelper.nearbyBlocks(golem, predicate);
        } else if (queue != null && queue.isEmpty()) {
            queue = VisionHelper.nearbyBlocks(golem, predicate);
        }
        if (queue == null || queue.isEmpty()) return false;
        do {
            blockPos = queue.poll();
        } while (!predicate.filter(golem, blockPos) && !queue.isEmpty());
        System.out.println(blockPos + "pos");
        return golem.getMainHandItem().isEmpty() && blockPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return (golem.getMainHandItem().isEmpty() ||  harvestTimer <= 40) && blockPos != null;
    }

    private boolean isPlant(LevelReader levelReader, BlockPos blockPos) {
        return levelReader != null && blockPos != null && levelReader.getBlockState(blockPos).getBlock() instanceof CropBlock;
    }

    private boolean isGrownPlant(LevelReader levelReader, BlockPos blockPos) {
        if (levelReader == null || blockPos == null) return false;
        BlockState state = levelReader.getBlockState(blockPos);
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        } else if (state.getBlock() instanceof BushBlock bush) {
            for (var prop : state.getProperties()) {
                // I wish there was a fruit-bearing bush class...
                if (prop instanceof IntegerProperty intProp && prop.getName().equals("age") && state.getValue(intProp) >= Collections.max(intProp.getPossibleValues()) ) {
                    return true;
                }
            }
        } else if (state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.MELON) {
            // Just going to brute force this for now... don't care about efficiency
            for (Direction dir : Direction.values()) {
                if (levelReader.getBlockState(blockPos.relative(dir)).getBlock() instanceof AttachedStemBlock
                        && blockPos.relative(dir).relative(state.getValue(AttachedStemBlock.FACING)).equals(blockPos)) return true;
            }
        }
        return false;
    }

    public ItemStack harvest() {
        if (golem.level() instanceof ServerLevel level) {
            BlockState state = level.getBlockState(blockPos);
            LootParams.Builder builder = new LootParams.Builder(level).
                    withParameter(LootContextParams.TOOL, ItemStack.EMPTY).
                    withParameter(LootContextParams.ORIGIN, mob.position());
            return state.getDrops(builder).stream().filter(this::isCropDrop).findFirst().orElse(ItemStack.EMPTY);
        } else {
            Constants.LOG.error("Golem level not ServerLevel!!!");
            return ItemStack.EMPTY;
        }
    }

    private boolean isCropDrop(ItemStack item) {
        return !(item.getItem() instanceof ItemNameBlockItem) || item.getItem().components().has(DataComponents.FOOD);
    }

}
