package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.hero.strawgolem.client.GolemArmAnimationController;
import org.hero.strawgolem.client.GolemHarvestAnimationController;
import org.hero.strawgolem.client.GolemLegAnimationController;
import org.hero.strawgolem.golem.api.ContainerHelper;
import org.hero.strawgolem.golem.api.ReachHelper;
import org.hero.strawgolem.golem.api.BiPredicate;
import org.hero.strawgolem.golem.api.VisionHelper;
import org.hero.strawgolem.golem.goals.GolemDepositGoal;
import org.hero.strawgolem.golem.goals.GolemHarvestGoal;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.registry.SoundRegistry;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Queue;

import static org.hero.strawgolem.Constants.CONFIG;

public class StrawGolem extends AbstractGolem implements GeoAnimatable {
    public StrawGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    public final Deliverer deliverer = new Deliverer();
    public final Harvester harvester = new Harvester();

    public static final double defaultMovement = 0.23;
    public static final double defaultWalkSpeed = 0.5;
    public static final float baseHealth = 6;
    private static final EntityDataAccessor<Boolean> HAT = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CARRY_STATUS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PICKUP_STATUS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARREL = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new GolemWanderGoal(this));
        goalSelector.addGoal(1, new GolemDepositGoal(this));
        goalSelector.addGoal(1, new GolemHarvestGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(CARRY_STATUS, 0);
        pBuilder.define(PICKUP_STATUS, 0);
        pBuilder.define(HAT, false);
        pBuilder.define(BARREL, 0);
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new GolemArmAnimationController(this));
        registrar.add(new GolemLegAnimationController(this));
        registrar.add(new GolemHarvestAnimationController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return instanceCache;
    }

    @Override
    public double getTick(Object o) {
        return RenderUtil.getCurrentTick();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, defaultMovement)
                .add(Attributes.MAX_HEALTH, baseHealth);
    }

    @Override
    public void tick() {
        Item item = getMainHandItem().getItem();
        if (item instanceof BlockItem) setCarryStatus(2);
        else if (getMainHandItem() != ItemStack.EMPTY) setCarryStatus(1);
        else setCarryStatus(0);
        super.tick();
    }
// seems to have an item dupe glitch...
    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
//        System.out.println("interact!");
        if (level().isClientSide) return InteractionResult.PASS;
        pPlayer.playSound(SoundRegistry.GOLEM_HAPPY.get());
        ItemStack item = pPlayer.getMainHandItem();
        if (pHand == InteractionHand.MAIN_HAND && item != ItemStack.EMPTY) {
            if (item.is(Items.BARREL) && barrelHP() != 100) {
                entityData.set(BARREL, 100);
                item.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(pPlayer, pHand);
    }

    // may mess with knockback when barreled, or change this to the hurt method...
    @Override
    protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (barrelHP() - pDamageAmount >= 0) { // barrel blocks
            entityData.set(BARREL, (int) (barrelHP() - pDamageAmount));
            playSound(SoundEvents.SHIELD_BLOCK);
            return;
        } else if (hasBarrel()) { // barrel breaks
            entityData.set(BARREL, 0);
            playSound(SoundEvents.SHIELD_BREAK);
        }
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Checking if golem speed needs fixed
//        if (tag.getBoolean("fixSpeed")) {
//            this.getHunger().getState().updateSpeed(this);
//        }
        // Hat!
        this.entityData.set(HAT, tag.getBoolean("hat"));
        this.entityData.set(CARRY_STATUS, tag.getInt("carry"));
//        // Barrel!
        this.entityData.set(BARREL, tag.getInt("barrelHP"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("hat", this.hasHat());
        tag.putInt("carry", carryStatus());
        tag.putInt("barrelHP", barrelHP());
//        tag.putInt("barrelHealth", this.entityData.get(BARREL_HEALTH));
//        tag.putBoolean("fixSpeed", fixSpeed);
        super.addAdditionalSaveData(tag);
    }
    // May adjust this, kind of a clunky description
    /*
     * 0 : The golem is at full or above full health.
     * 1 : The golem is below full health, but has more than a third of health left.
     * 2 : The golem has a third or less of its health left.
    */
    /**
     * This method returns an integer depending on the golem's health.
     * @return A status code based on golem health, 0 means full health, 1 means injured, 2 means severely injured.
     */
    public int healthStatus() {
        // basic code to check how dead a golem is
        return getMaxHealth() <= getHealth() ? 0 : getMaxHealth() / 3 < getHealth() ? 1 : 2;
    }

    /**
     * This method returns an integer depending on the golem's movement status.
     * @return A status code based on golem movement, 0 means zero movement, 1 means walking, 2 means running.
     */
    public int movementStatus() {
//        level().tickRateManager()
//        System.out.println("movement status: " + walkDist);
//        System.out.println((walkDist > 0.0) + " " + (walkDist == 0.0));
        double movement = getDeltaMovement().horizontalDistance() * level().tickRateManager().tickrate();
//        System.out.println("mov status:"  + movement);
        return movement == 0 ? 0 : movement < defaultWalkSpeed * 0.8 ? 1 : 2;
    }

    /**
     * This method returns an integer depending on the golem's carrying status.
     * @return A status code based on item carrying, 0 means no item, 1 means a regular item, 2 means a block.
     */
    public int carryStatus() {
        return entityData.get(CARRY_STATUS);
    }

    public void setCarryStatus(int status) {
        entityData.set(CARRY_STATUS, status);
    }

    /**
     * This method returns an integer depending on the golem's pick up status.
     * @return A status code based on if and what the golem is picking up, 0 means not picking up, 1 means picking up an item, 2 means picking up a block.
     */
    public int pickupStatus() {
        return entityData.get(PICKUP_STATUS);
    }

    /**
     * 0 means not picking up, 1 means picking up an item, 2 means picking up a block.
     */
    public void setPickupStatus(int status) {
        entityData.set(PICKUP_STATUS, status);
    }
// rewrote this part, no stupid things included
    public void setPickupStatus(ItemStack item) {
//        itemToPickUp = item;
//        if (immediate) pickup();
//        setItemSlot(EquipmentSlot.MAINHAND, item);

        if (!item.isEmpty()) {
            if (item.getItem() instanceof BlockItem) setPickupStatus(2);
            else setPickupStatus(1);
        } else {
            setPickupStatus(0);
        }
    }

    public boolean isScared() {
        return false;
    }

    public boolean holdItemAbove() {
        return carryStatus() == 2 || hasBarrel();
    }

    public boolean hasHat() {
        return entityData.get(HAT);
    }

    public int barrelHP() {
        return entityData.get(BARREL);
    }

    public boolean hasBarrel() {
        return barrelHP() != 0;
    }

    public class Harvester {
        Queue<BlockPos> harvestLocations;
        BlockPos nextLocation() {
            return harvestLocations == null ? null : harvestLocations.poll();
        }

    }
    public class Deliverer {
        BlockPos storagePos;
        public BlockPos getDeliverable() {
            StrawGolem golem = StrawGolem.this;
//            BiPredicate<StrawGolem, BlockPos> = new BiPredicate<>((U, T) -> VisionHelper.canSee(U, T));
                BiPredicate e = (gol, pos) -> VisionHelper.canSee(gol, pos) && ContainerHelper.isContainer(gol, pos) && ReachHelper.canPath(gol, pos);
//             Test e = new Test( filter(StrawGolem gol, BlockPos pos) -> VisionHelper.canSee(gol, pos));
            if (storagePos != null && e.filter(golem, storagePos)) return storagePos;
//            int range = Constants.Golem.searchRange;

//            BlockPos closest = null;
//            BlockPos query = StrawGolem.this.blockPosition();
//            for (int x = -range; x <= range; ++x) {
//                for (int y = -range / 2; y <= range / 2; ++y) {
//                    for (int z = -range; z <= range; ++z) {
//                        BlockPos pos = query.offset(x, y, z);
////                        System.out.println(ContainerHelper.isContainer(StrawGolem.this.level(), pos) + " " + VisionHelper.canSee(StrawGolem.this, pos));
//                        if (ContainerHelper.isContainer(golem.level(), pos)
//                                && VisionHelper.canSee(golem, pos) && ReachHelper.canPath(golem, pos)
//                                /*&& !invalidContainers.contains(pos)*/) {
//                            // Should find the closest deliverable...
//                            closest = closest == null || query.distManhattan(pos) < query.distManhattan(closest) ? pos : closest;
////                            containerSet.add(pos);
//                        }
//                    }
//                }
//            }
            BlockPos pos = VisionHelper.findNearestBlock(golem, e);
            // Keep StoragePos as the saved one (may change this for only save the player-specified ones...)
            storagePos = storagePos == null ? pos : storagePos;
            return pos;
        }

        public void deliver(LevelReader level, BlockPos pos) {
            ItemStack item = StrawGolem.this.getMainHandItem();
            if (item != ItemStack.EMPTY && ContainerHelper.isContainer(level, pos)) {
//                System.out.println("deliver?");
                Container container = (Container) level.getBlockEntity(pos);
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack cItem = container.getItem(i);
                    if (cItem.is(item.getItem()) && container.getMaxStackSize(cItem) > cItem.getCount()) {
                        cItem.grow(item.getCount());
                        container.setItem(i, cItem);
//                        System.out.println("DELIVER!");
                        StrawGolem.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        return;
                    }
                }
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack cItem = container.getItem(i);
                    if (cItem.isEmpty() && container.getMaxStackSize() > cItem.getCount()) {
                        container.setItem(i, item);
//                        System.out.println("DELIVER!");
                        StrawGolem.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        return;
                    }
                }
            }
        }

    }

}
