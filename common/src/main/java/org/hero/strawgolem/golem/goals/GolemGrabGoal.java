package org.hero.strawgolem.golem.goals;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.golem.api.BiPredicate;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.VisionHelper;
import org.hero.strawgolem.mixinInterfaces.StemFruit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GolemGrabGoal extends GolemMoveToBlockGoal {
    List<ItemEntity> items;
    static Set<Item> validItems;
    ItemEntity currentTarget;
    StrawGolem golem;
    int pickupTicks = 0;
    boolean pickUp = false;
    boolean itemAcquired = false;
    boolean stop = false;
    // May just make this into a regular predicate
    BiPredicate<ItemEntity> predicate = (gol, entity) -> isInValidItems(entity.getItem().getItem()) && !entity.hasPickUpDelay() && ReachHelper.canPath(golem, entity.blockPosition());


    public GolemGrabGoal(StrawGolem pMob) {
        super(pMob, Constants.Golem.defaultWalkSpeed, Constants.Golem.searchRange, Constants.Golem.searchRangeVertical);
        golem = pMob;
        constructValidItems();
    }

    private void updateNearbyItems() {
        items = golem.level().getEntitiesOfClass(ItemEntity.class,
                golem.getBoundingBox().inflate(
                    Constants.Golem.searchRange,
                    Constants.Golem.searchRangeVertical,
                    Constants.Golem.searchRange
                )).stream().filter((entity ->
                    predicate.filter(golem, entity)
                ))
                .collect(Collectors.toCollection(ArrayList::new));
        items.sort((item1, item2) ->
                item2.getAge() - item1.getAge()
        );
    }

    // Method to check if there is a collision between straw golems.
    protected boolean hasNearbyItem() {
        if (items == null || items.isEmpty()) {
            updateNearbyItems();
        }
        return !items.isEmpty();
    }

    @Override
    public void stop() {
        // ToDo: Look into a more gradual stop
        mob.getNavigation().stop();
        stop = true;
        itemAcquired = false;
    }

    @Override
    public void tick() {
        if (!itemAcquired) {
            if (!(currentTarget != null && currentTarget.isAlive())) {
                updateNearbyItems();
                if (items.isEmpty()) {
                    // no items, don't continue
                    stop();
                    return;
                }
                this.currentTarget = items.getFirst();
                blockPos = currentTarget.blockPosition();
                moveMobToBlock();
            }
        }
        if (mob.distanceTo(currentTarget) >= Constants.Golem.depositDistance && golem.getNavigation().isDone()) {
            moveMobToBlock();
        }
        // ToDo: Switch off of currentTarget.hasPickUpDelay() causes issues if golem flickers in and out of range.
        if (!itemAcquired && !pickUp && ReachHelper.canReach(golem, currentTarget.blockPosition())
                && golem.carryStatus() == 0 && currentTarget.isAlive() && !currentTarget.hasPickUpDelay() && golem.onGround()) {
            currentTarget.setPickUpDelay(40);
            golem.lookAt(currentTarget, 50f, 50f);
//            golem.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).setBaseValue(0);
            golem.setPickupStatus(currentTarget.getItem());
            pickUp = true;
        } else if (itemAcquired || (pickUp && currentTarget.isAlive() && golem.carryStatus() == 0
                && ReachHelper.canReach(golem, currentTarget.blockPosition()))) {
            if (pickupTicks == 5) golem.stopInPlace();
            if (pickupTicks == 20) {
                golem.setItemSlot(EquipmentSlot.MAINHAND, currentTarget.getItem());
                itemAcquired = true;
                // Let the itemstack discard on its own
                currentTarget.setItem(ItemStack.EMPTY);
            } else if (pickupTicks >= 40) {
                pickUp = false;
                golem.setPickupStatus(0);
                stop();
            }
            pickupTicks++;
        } else if (pickupTicks != 0 || pickUp) {
            itemAcquired = false;
            pickUp = false;
            golem.setPickupStatus(0);
            pickupTicks = 0;
            moveMobToBlock();
        }
    }


    @Override
    public void start() {
        updateNearbyItems();
        stop = false;
        if (items.isEmpty()) return;
        currentTarget = items.getFirst();
        moveMobToBlock();
    }

    @Override
    protected void moveMobToBlock() {
        if (currentTarget != null) {
            this.mob.getNavigation().moveTo(currentTarget.getX(),
                    (currentTarget.getY()),
                    currentTarget.getZ(),
                    0, this.speedModifier);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !stop && (golem.carryStatus() == 0 || pickupTicks <= 40) && hasNearbyItem();
    }

    @Override
    public boolean canUse() {
        return golem.carryStatus() == 0 && hasNearbyItem() && VisionHelper.canSee(golem, items.getFirst().blockPosition());
    }

    private boolean isInValidItems(Item item) {
        if (validItems == null || validItems.isEmpty()) return false;
        return validItems.contains(item);
    }
    private void constructValidItems() {
        if (validItems != null) return;
        validItems = new HashSet<>();
        ResourceLocation location = ResourceLocation.tryParse("crops");
        if (location == null) return;
        try {
            for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK,
                    location)).get().stream().toList()) {
                if (blockHolder.value() instanceof StemFruit fruitStem) {
                    Item item = fruitStem.strawgolemRewrite$getFruit().asItem();
                    if (item != Items.AIR) {
                        validItems.add(fruitStem.strawgolemRewrite$getFruit().asItem());
                    }
                } else if (blockHolder.value() instanceof CropBlock crop && golem.level() instanceof ServerLevel level) {
                    BlockState state = crop.getStateForAge(crop.getMaxAge());
                    LootParams.Builder builder = new LootParams.Builder(level).
                            withParameter(LootContextParams.TOOL, ItemStack.EMPTY).
                            withParameter(LootContextParams.ORIGIN, mob.position());
                    List<ItemStack> drops = state.getDrops(builder).stream().toList();
                    for (var drop : drops) {
                        validItems.add(drop.getItem());
                    }
                    validItems.add(blockHolder.value().asItem());
                } else if (blockHolder.value().asItem() != Items.AIR) {
                    // May get rid of this, may not depending on if I want seeds.
                    validItems.add(blockHolder.value().asItem());
                }
            }
        } catch (Throwable e) {
            Constants.LOG.error("Error constructing valid items! {}", e.getMessage());
        }
    }
}
