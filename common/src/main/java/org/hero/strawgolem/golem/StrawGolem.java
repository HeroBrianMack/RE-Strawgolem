package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.*;
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
import org.hero.strawgolem.golem.goals.GolemGrabGoal;
import org.hero.strawgolem.golem.goals.GolemHarvestGoal;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.mixinInterfaces.GolemOrderer;
import org.hero.strawgolem.registry.ItemRegistry;
import org.hero.strawgolem.registry.SoundRegistry;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.time.LocalDate;
import java.time.Month;
import java.util.Queue;

import static org.hero.strawgolem.Constants.*;

public class StrawGolem extends AbstractGolem implements GeoAnimatable {
    public StrawGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    public final Deliverer deliverer = new Deliverer();
    public final Harvester harvester = new Harvester();

    public static final double defaultMovement = Golem.defaultMovement;
    public static final double defaultWalkSpeed = Golem.defaultWalkSpeed;
    public static final float baseHealth = Golem.maxHealth;
    private static final EntityDataAccessor<Boolean> HAT = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FESTIVE = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CARRY_STATUS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PICKUP_STATUS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARREL = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> PRIORITY_POS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BLOCK_POS);

    private boolean forceAnimationReset = false;
    public boolean createSnow = false;

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new GolemWanderGoal(this));
        goalSelector.addGoal(1, new GolemDepositGoal(this));
        goalSelector.addGoal(1, new GolemHarvestGoal(this));
        goalSelector.addGoal(1, new GolemGrabGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(CARRY_STATUS, 0);
        pBuilder.define(PICKUP_STATUS, 0);
        pBuilder.define(HAT, false);
        pBuilder.define(FESTIVE, false);
        pBuilder.define(BARREL, 0);
        pBuilder.define(PRIORITY_POS, new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
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
        if (item instanceof BlockItem && !(item instanceof ItemNameBlockItem)) setCarryStatus(2);
        else if (!getMainHandItem().isEmpty()) setCarryStatus(1);
        else setCarryStatus(0);
        super.tick();
    }
// seems to have an item dupe glitch...
    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (level().isClientSide) return InteractionResult.PASS;
        this.playSound(SoundRegistry.GOLEM_HAPPY.get());
        ItemStack item = pPlayer.getMainHandItem();
        if (pHand == InteractionHand.MAIN_HAND && item != ItemStack.EMPTY) {
            if (item.is(Items.BARREL) && barrelHP() != Golem.barrelHealth) {
                entityData.set(BARREL, Golem.barrelHealth);
                item.shrink(1);
            } else if (item.is(Items.WHEAT) && healthStatus() != 0) {
                if (getMaxHealth() - getHealth() < 3.0f) {
                    setHealth(getMaxHealth());
                } else {
                    setHealth(getHealth() + 3.0f);
                }
                this.playSound(SoundRegistry.GOLEM_INTERESTED.get());
                item.shrink(1);
            } else if (item.is(ItemRegistry.STRAW_HAT.get()) && !hasHat()) {
                entityData.set(HAT, true);
                this.playSound(SoundRegistry.GOLEM_INTERESTED.get());
            } else if (item.is(Items.BRUSH)) {
                this.playSound(SoundRegistry.GOLEM_HAPPY.get());
                entityData.set(FESTIVE, false);

            }
            return InteractionResult.CONSUME;
        } else if (pHand == InteractionHand.MAIN_HAND
                    && pPlayer.getMainHandItem().isEmpty() && pPlayer.isCrouching()
                    && pPlayer instanceof GolemOrderer orderer) {
            if (orderer.strawgolemRewrite$getGolem() == null || !orderer.strawgolemRewrite$getGolem().equals(this)) {
                orderer.strawgolemRewrite$setGolem(this);
                pPlayer.displayClientMessage(Component.translatable("strawgolem.ordering.start"), true);
            } else {
                orderer.strawgolemRewrite$setGolem(null);
                pPlayer.displayClientMessage(Component.translatable("strawgolem.ordering.stop"), true);
            }
        }
        return super.mobInteract(pPlayer, pHand);
    }

    // may mess with knockback when barreled, or change this to the hurt method...
    @Override
    protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        try {
            if (pDamageSource.getDirectEntity() instanceof Snowball && isHoliday() && !this.level().isClientSide) {
                this.playSound(SoundRegistry.GOLEM_STRAINED.get());
                entityData.set(FESTIVE, true);
                return;
            }
        } catch(Throwable e) {
            LOG.error(e.getMessage());
        }
        if (barrelHP() - pDamageAmount >= 0) { // barrel blocks
            entityData.set(BARREL, (int) (barrelHP() - pDamageAmount));
            playSound(SoundEvents.SHIELD_BLOCK);
            return;
        } else if (hasBarrel()) { // barrel breaks
            // Reduce the damage by the remaining barrel health
            pDamageAmount -= barrelHP();
            entityData.set(BARREL, 0);
            playSound(SoundEvents.SHIELD_BREAK);
        }
        // TODO: Add Panic
        this.playSound(SoundRegistry.GOLEM_HURT.get());
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Checking if golem speed needs fixed
        // Hat!
        this.entityData.set(HAT, tag.getBoolean("hat"));
        this.entityData.set(FESTIVE, tag.getBoolean("festive"));
        this.entityData.set(CARRY_STATUS, tag.getInt("carry"));
        // Barrel!
        this.entityData.set(BARREL, tag.getInt("barrelHP"));
        this.entityData.set(PRIORITY_POS, BlockPos.of(tag.getLong("priorityPos")));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("hat", this.hasHat());
        tag.putBoolean("festive", this.entityData.get(FESTIVE));
        tag.putInt("carry", carryStatus());
        tag.putInt("barrelHP", barrelHP());
        tag.putLong("priorityPos", this.entityData.get(PRIORITY_POS).asLong());
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
        return getMaxHealth() - 0.0001f <= getHealth() ? 0 : getMaxHealth() * 0.333333 < getHealth() ? 1 : 2;
    }

    /**
     * This method returns an integer depending on the golem's movement status.
     * @return A status code based on golem movement, 0 means zero movement, 1 means walking, 2 means running.
     */
    public int movementStatus() {
        double movement = getDeltaMovement().horizontalDistance() * level().tickRateManager().tickrate();
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

    public boolean isFestive() {
        return entityData.get(FESTIVE);
    }

    private boolean isHoliday() {
        Month month = LocalDate.now().getMonth();
        // Define the range: Dec 20th to Dec 27th
        return month == Month.DECEMBER || month == Month.JANUARY;
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
            if (item.getItem() instanceof BlockItem && !(item.getItem() instanceof ItemNameBlockItem)) {
                setPickupStatus(2);
            }
            else {
                setPickupStatus(1);
            }
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

    public void setPriorityPos(BlockPos pos) {
        entityData.set(PRIORITY_POS, pos);
    }

    public BlockPos getPriorityPos() {
        return entityData.get(PRIORITY_POS);
    }

    public boolean shouldForceAnimationReset() {
        if (forceAnimationReset) {
            forceAnimationReset = false;
            return true;
        } else {
            return false;
        }
    }

    public void forceAnimationReset() {
        this.forceAnimationReset = true;
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
            BiPredicate<BlockPos> predicate = (gol, pos) -> VisionHelper.canSee(gol, pos) && ContainerHelper.isContainer(gol, pos) && ReachHelper.canPath(gol, pos);
            // Checking the player-bound position first.
            if (getPriorityPos().getX() != Integer.MAX_VALUE && predicate.filter(golem, getPriorityPos())) {
                return getPriorityPos();
            }
            if (storagePos != null && predicate.filter(golem, storagePos)) {
                return storagePos;
            }
            BlockPos pos = VisionHelper.findNearestBlock(golem, predicate);
            // Keep StoragePos as the saved one (may change this for only save the player-specified ones...)
            storagePos = storagePos == null || !predicate.filter(golem, storagePos) ? pos : storagePos;
            return pos;
        }

        public void deliver(LevelReader level, BlockPos pos) {
            ItemStack item = StrawGolem.this.getMainHandItem();
            if (!item.isEmpty() && ContainerHelper.isContainer(level, pos)) {
                if (level.getBlockEntity(pos) instanceof Container container) {
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack cItem = container.getItem(i);
                        if (cItem.is(item.getItem()) && container.getMaxStackSize(cItem) > cItem.getCount()) {
                            cItem.grow(item.getCount());
                            container.setItem(i, cItem);
                            StrawGolem.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            return;
                        }
                    }
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack cItem = container.getItem(i);
                        if (cItem.isEmpty() && container.getMaxStackSize() > cItem.getCount()) {
                            container.setItem(i, item);
                            StrawGolem.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            return;
                        }
                    }
                }
            } else {
                // Should in theory never trigger
                LOG.error("Delivery location is not a container! {} {}", item.isEmpty(), ContainerHelper.isContainer(level, pos));
            }
        }

    }

}
