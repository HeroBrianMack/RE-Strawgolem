package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.BiPredicate;
import org.hero.strawgolem.golem.api.VisionHelper;

import java.util.Collections;
import java.util.Queue;

public class GolemHarvestGoal extends GolemMoveToBlockGoal {
    private StrawGolem golem;
    private Queue<BlockPos> queue;
    private int harvestTimer = 0;
    private ItemStack item;
    private BiPredicate predicate = (gol, pos) -> /*VisionHelper.canSee(gol, pos) && */isGrownPlant(gol.level(), pos) && ReachHelper.canPath(gol, pos);
    public GolemHarvestGoal(StrawGolem golem) {
        super(golem, Constants.Golem.defaultWalkSpeed, Constants.Golem.searchRange, Constants.Golem.searchRangeVertical);
        this.golem = golem;
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return VisionHelper.canSee(golem, blockPos) && predicate.filter(golem, blockPos);
    }

    @Override
    public void tick() {
        try {
            super.tick();
            // Begin harvest phase animation
            if (ReachHelper.canReach(mob, blockPos) && item == null) {
                // harvest time!
                item = harvest();
                golem.setPickupStatus(item);
                golem.getNavigation().stop();
            } else if (item != null && !item.isEmpty()) {
                // Phase Two of harvesting

                // Timer to wait for animation to finish
                harvestTimer++;
                // Another golem harvested block (TODO: Mark blocks with golem IDs, timestamp to avoid this confusion + Smarter harvesting)
                if (harvestTimer == 20 && predicate.filter(golem, blockPos) && ReachHelper.canReach(mob, blockPos)) {
                    // Double checking this, not going to let someone change a block
                    item = harvest();
                    blockReset(golem.level());
                    golem.setItemSlot(EquipmentSlot.MAINHAND, item);
                } else if (harvestTimer == 40) {
                    golem.setPickupStatus(0);
                } else if (harvestTimer < 20 && !predicate.filter(golem, blockPos)) {
                    golem.forceAnimationReset();
                    golem.setPickupStatus(0);
                    item = null;
                }
            } else if (shouldRecalculatePath() && golemCollision(golem)) {
                // Handling golem collisions.
                nudge(golem);
            }
        } catch (Throwable e) {
            // Using a try-catch to avoid all risk of player crashes.
            Constants.LOG.error(e.getMessage());
            stop();
        }


    }
    @Override
    public void start() {
        if (blockPos == null) blockPos = queue.poll();
        moveMobToBlock();
        this.tryTicks = 0;
        harvestTimer = 0;
        item = null;
    }

    @Override
    public void stop() {
        golem.setPickupStatus(0);
        // ToDo: Look into a more gradual stop
        golem.getNavigation().stop();
    }

    @Override
    protected void moveMobToBlock() {
        this.mob.getNavigation().moveTo((double)this.blockPos.getX() + 0.5,
                (double)(this.blockPos.getY()),
                (double)this.blockPos.getZ() + 0.5,
                0, this.speedModifier);
    }

    @Override
    public boolean canUse() {
        // maybe add a delay before allowing golem to harvest again... could solve lag
        // Creating the queue of blocks, prevents re-searching for targets every time
        if (golem.getMainHandItem().isEmpty() && queue == null) {
            queue = VisionHelper.nearbyBlocks(golem, predicate);
        } else if (queue != null && queue.isEmpty()) {
            // If the queue is empty, remake it, may combine if statements...
            queue = VisionHelper.nearbyBlocks(golem, predicate);
        }
        // No valid harvest locations or failed to create the queue.
        if (queue == null || queue.isEmpty()) return false;
        do {
            // Go through the queue until a valid target is found.
            blockPos = queue.poll();
        } while (!predicate.filter(golem, blockPos) && !queue.isEmpty());
        // Either the queue is empty or there is a block position given.
        // May give a blockpos out of sight range,
        // unsure how this will harm golem efficiency.
        return golem.getMainHandItem().isEmpty() && isValidTarget(golem.level(), blockPos);
    }

    @Override
    public boolean canContinueToUse() {
        return harvestTimer <= 38 && (golem.getMainHandItem().isEmpty() ||  harvestTimer <= 40) && isValidTarget(golem.level(), blockPos);
    }

    private boolean isPlant(LevelReader levelReader, BlockPos blockPos) {
        return levelReader != null && blockPos != null && levelReader.getBlockState(blockPos).getBlock() instanceof CropBlock;
    }

    // Checking if the blockpos on the level is a valid and grown plant.
    private boolean isGrownPlant(LevelReader levelReader, BlockPos blockPos) {
        if (levelReader == null || blockPos == null) return false;
        BlockState state = levelReader.getBlockState(blockPos);
        if (Constants.Golem.whitelistHarvest && !Constants.Golem.whitelist.contains(state.getBlock())) {
            return false;
        }
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        } else if (state.getBlock() instanceof BushBlock bush) {
            for (var prop : state.getProperties()) {
                // I wish there was a fruit-bearing bush class...
                if (prop instanceof IntegerProperty intProp && prop.getName().equals("age")
                        && state.getValue(intProp) >= Collections.max(intProp.getPossibleValues()
                )) {
                    return true;
                }
            }
        } else if (Constants.Golem.blockHarvest && state.getBlock()
                == Blocks.PUMPKIN || state.getBlock() == Blocks.MELON) { // Pumpkins and Melons
            // Just going to brute force this for now... don't care about efficiency currently
            try {
                for (Direction dir : Direction.values()) {
                    if (levelReader.getBlockState(blockPos.relative(dir))
                            .getBlock() instanceof AttachedStemBlock
                            && state.hasProperty(AttachedStemBlock.FACING) && blockPos.relative(dir)
                            .relative(state.getValue(AttachedStemBlock.FACING))
                            .equals(blockPos)) {
                        return true;
                    }
                }
            } catch (Throwable e) {
                Constants.LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    // Gets the item as if harvested from the plant.
    public ItemStack harvest() {
        if (golem.level() instanceof ServerLevel level) {
            BlockState state = level.getBlockState(blockPos);
            LootParams.Builder builder = new LootParams.Builder(level).
                    withParameter(LootContextParams.TOOL, ItemStack.EMPTY).
                    withParameter(LootContextParams.ORIGIN, mob.position());
            ItemStack drops = state.getDrops(builder).stream().filter(this::isCropDrop).findFirst()
                    .orElse(state.getDrops(builder).stream().findFirst().orElse(ItemStack.EMPTY));
            return drops;
        } else {
            Constants.LOG.error("Golem level not ServerLevel!");
            return ItemStack.EMPTY;
        }
    }

    // Resets the age of the crop.
    private void blockReset(Level level) {
        BlockState state = level.getBlockState(blockPos);
        for (Property<?> prop : state.getProperties()) {
            // Let's assume age is not a weird property...
            if (prop.getName().equalsIgnoreCase("age") && prop instanceof IntegerProperty intprop) {
                int value = state.getBlock().defaultBlockState().getValue(intprop);
                level.playSound(null, blockPos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS);
                state = state.setValue(intprop, value);
                level.setBlockAndUpdate(blockPos, state);
            }
        }
    }

    // Checking if an ItemStack is a crop drop and not seeds.
    private boolean isCropDrop(ItemStack item) {
        return !(item.getItem() instanceof ItemNameBlockItem)
                || item.getItem().components().has(DataComponents.FOOD);
    }

}
