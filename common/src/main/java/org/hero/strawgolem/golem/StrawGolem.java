package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Pillager;
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
import org.hero.strawgolem.golem.features.GolemHungerFeature;
import org.hero.strawgolem.golem.features.GolemLifespanFeature;
import org.hero.strawgolem.golem.features.IGolemTickFeature;
import org.hero.strawgolem.golem.goals.*;
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
import java.util.List;

import static org.hero.strawgolem.Constants.*;

public class StrawGolem extends AbstractGolem implements GeoAnimatable {
    // Constructor for Straw Golem just uses the super class (needs to be examined for changes in future versions).
    public StrawGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // GeckoLib variable, unnoteworthy.
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);

    // The deliverer for the Straw Golem.
    public final Deliverer deliverer = new Deliverer();
    // The features of the Straw Golem.
    private final GolemHungerFeature hunger = new GolemHungerFeature(this);
    private final GolemLifespanFeature lifeSpan = new GolemLifespanFeature(this);
    private final List<IGolemTickFeature> features = List.of(hunger, lifeSpan);

    // The constants of the Straw Golem.
    public static final double defaultMovement = Golem.defaultMovement;
    public static final double defaultWalkSpeed = Golem.defaultWalkSpeed;
    public static final float baseHealth = Golem.maxHealth;
    // Synched data accessors for the Straw Golem.
    private static final EntityDataAccessor<Boolean> HAT = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FESTIVE = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PANIC = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CARRY_STATUS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PICKUP_STATUS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARREL = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFE_SPAN = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> PRIORITY_POS = SynchedEntityData.defineId(StrawGolem.class, EntityDataSerializers.BLOCK_POS);

    // Variable for forcing Straw Golem animation resets.
    private boolean forceAnimationReset = false;
    // Variable for determining whether Straw Golem is creating snow particles.
    public boolean createSnow = false;

    @Override
    protected void registerGoals() {
        // Adds goals for the Straw Golem.
        goalSelector.addGoal(0, new FloatGoal(this));
        // Multiplying by 1.2 as a sort of "adrenaline" factor.
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        // Adds the Avoidance goals.
        generateAvoids();
        goalSelector.addGoal(2, new GolemWanderGoal(this));
        goalSelector.addGoal(1, new GolemDepositGoal(this));
        goalSelector.addGoal(1, new GolemHarvestGoal(this));
        goalSelector.addGoal(1, new GolemGrabGoal(this));
    }

    /**
     * Generates and adds to the goalSelector the Avoidance goals
     * of the entities that the Straw Golem wants to avoid.
     */
    protected void generateAvoids() {
        final double WALK = defaultWalkSpeed;
        final double RUN = Golem.defaultRunSpeed;
        // Just a variable to store priority conveniently.
        final int PRIORITY = 1;
        // Adding the avoid entity goals, could make this a for-each loop, but it seems unneeded.
        goalSelector.addGoal(PRIORITY, new GolemAvoidEntityGoal<>(this, Pillager.class,
                (e) -> e.getTarget() instanceof StrawGolem, Golem.fleeRange, WALK, RUN, EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        goalSelector.addGoal(PRIORITY, new GolemAvoidEntityGoal<>(this, Sheep.class,
                (e) -> e.getTarget() instanceof StrawGolem, Golem.fleeRange, WALK, RUN, EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        goalSelector.addGoal(PRIORITY, new GolemAvoidEntityGoal<>(this, Cow.class,
                (e) -> e.getTarget() instanceof StrawGolem, Golem.fleeRange, WALK, RUN, EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        goalSelector.addGoal(PRIORITY, new GolemAvoidEntityGoal<>(this, Pig.class,
                (e) -> e.getTarget() instanceof StrawGolem, Golem.fleeRange, WALK, RUN, EntitySelector.NO_CREATIVE_OR_SPECTATOR));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        // Defines any required persistent and synched data.
        pBuilder.define(CARRY_STATUS, 0);
        pBuilder.define(PICKUP_STATUS, 0);
        pBuilder.define(HAT, false);
        pBuilder.define(FESTIVE, false);
        pBuilder.define(PANIC, false);
        pBuilder.define(BARREL, 0);
        pBuilder.define(HUNGER, 0);
        pBuilder.define(LIFE_SPAN, 0);
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

    /**
     * Creates the Straw Golem's Attributes.
     * @return The Straw Golem's Attributes.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, defaultMovement)
                .add(Attributes.MAX_HEALTH, baseHealth);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel pLevel, DamageSource pDamageSource) {
        super.dropAllDeathLoot(pLevel, pDamageSource);
        if (hasHat()) {
            this.spawnAtLocation(ItemRegistry.STRAW_HAT.get());
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemstack.isEmpty()) {
            this.spawnAtLocation(itemstack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void tick() {
        // Tick each feature
        // May need to push all of these onto serverSide
        if (!level().isClientSide && isAlive())  {
            if (random.nextFloat() < 0.02f) {
                playSound(SoundRegistry.GOLEM_AMBIENT.get());
            }
            features.forEach(IGolemTickFeature::tick);
            if (Golem.panic) {
                setPanic(isRunningScaredGoal());
                if (getPanic()) {
                    dropEquipment();
                }
            }
        }
        // Get the Straw Golem's held item.
        Item item = getMainHandItem().getItem();

        // Refresh the Straw Golem's carry status based on held item.

        // If the Straw Golem is holding a Block.
        if (item instanceof BlockItem && !(item instanceof ItemNameBlockItem)) setCarryStatus(2);
        // If the Straw Golem is holding a regular item.
        else if (!getMainHandItem().isEmpty()) setCarryStatus(1);
        // If the Straw Golem is holding nothing.
        else setCarryStatus(0);
        super.tick();
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        // If the interaction is on the client side, don't bother (note: the super call is equivalent).
        if (level().isClientSide) return InteractionResult.PASS;
        // Straw Golem's enjoy interaction by a player regardless of the item.
        this.playSound(SoundRegistry.GOLEM_HAPPY.get());
        // Get the item the player is holding.
        ItemStack item = pPlayer.getMainHandItem();
        // Currently only doing main hand processing for reduction of bugs/unintended interactions.
        if (pHand == InteractionHand.MAIN_HAND && !item.isEmpty()) {
            // If the item is a barrel and the Straw Golem is not wearing a fresh barrel.
            if (item.is(Items.BARREL) && barrelHP() != Golem.barrelHealth) {
                // Replace the barrel with the player's held one.
                entityData.set(BARREL, Golem.barrelHealth);
                // Remove a barrel from the player's hand.
                item.shrink(1);
            } else if (Golem.repairItem.contains(item.getItem()) && healthStatus() != 0) {
                // If the player is holding a Straw Golem Repair Item, and the Straw Golem is damaged.

                // Decreasing life span by life a third of max life span to a minimum of 0.
                setLifeSpan(Math.max(0, getLifeSpan() - Golem.maxLife / 3));
                // Updating the Straw Golem's max health
                lifeSpan.refresh();
                // If max health is less than the increase in health, set Straw Golem to max health.
                if (getMaxHealth() - getHealth() < 3.0f) {
                    setHealth(getMaxHealth());
                } else {
                    // Else increase the Straw Golem's health by 3.
                    setHealth(getHealth() + 3.0f);
                }
                // Straw Golems are interested in repairs so play the sound.
                this.playSound(SoundRegistry.GOLEM_INTERESTED.get());
                // Decrement the repair item in the player's hand.
                item.shrink(1);
            } else if (Golem.foodItem.contains(item.getItem()) && getHunger() > Golem.maxHunger / 5) {
                // If the player is holding a Straw Golem Food Item, and the Straw Golem is hungry.

                // Decreasing hunger by a third of max hunger to a minimum of 0.
                setHunger(Math.max(0, getHunger() - Golem.maxHunger / 3));
                // Updating the Straw Golem's hunger.
                hunger.refresh();
                // Straw Golems are interested in food so play the sound.
                this.playSound(SoundRegistry.GOLEM_INTERESTED.get());
                // Decrement the food item in the player's hand.
                item.shrink(1);
            } else if (item.is(ItemRegistry.STRAW_HAT.get()) && !hasHat()) {
                // If the item is a straw hat and the Straw Golem is hatless.

                // Equip the hat.
                entityData.set(HAT, true);
                // Straw Golems are interested in straw hats, so play the sound.
                this.playSound(SoundRegistry.GOLEM_INTERESTED.get());
                // Decrement the hat in the player's hand.
                item.shrink(1);
            } else if (item.is(Items.BRUSH)) {
                // Straw Golems simply enjoy being brushed
                this.playSound(SoundRegistry.GOLEM_HAPPY.get());
                // Clears the winter model.
                entityData.set(FESTIVE, false);
                // Could drain durability here, but doesn't seem notable enough to do so.
            } else {
                // Unrecognized item: don't eat the click. Letting the interaction fall
                // through gives the held item's own entity-interaction a turn (mob
                // capture tools, the golem retrainer, etc.).
                return InteractionResult.PASS;
            }
            // Mark the result as consumption.
            return InteractionResult.CONSUME;
        } else if (pHand == InteractionHand.MAIN_HAND
                    && pPlayer.getMainHandItem().isEmpty() && pPlayer.isCrouching()
                    && pPlayer instanceof GolemOrderer orderer) {
            // If player has nothing in mainhand and is crouching.

            // If a player has no assigned Straw Golem or the assigned is another Straw Golem, select this one.
            if (orderer.strawgolemRewrite$getGolem() == null || !orderer.strawgolemRewrite$getGolem().equals(this)) {
                // Assign the Straw Golem to the player.
                orderer.strawgolemRewrite$setGolem(this);
                // Display an assignment message.
                pPlayer.displayClientMessage(Component.translatable("strawgolem.ordering.start"), true);
            } else {
                // Else unassign the Straw Golem from the player.
                orderer.strawgolemRewrite$setGolem(null);
                // Display an unassignment message.
                pPlayer.displayClientMessage(Component.translatable("strawgolem.ordering.stop"), true);
            }
        }
        return super.mobInteract(pPlayer, pHand);
    }

    // may mess with knockback when barreled, or change this to the hurt method...
    @Override
    protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        try {
            // If a snowball and the season is Winter.
            if (pDamageSource.getDirectEntity() instanceof Snowball && isWinter() && !this.level().isClientSide) {
                this.playSound(SoundRegistry.GOLEM_STRAINED.get());
                entityData.set(FESTIVE, true);
                return;
            }
        } catch(Throwable e) {
            LOG.error(e.getMessage());
        }
        if (barrelHP() - pDamageAmount > 0) { // barrel blocks the damage.
            entityData.set(BARREL, (int) (barrelHP() - pDamageAmount));
            playSound(SoundEvents.SHIELD_BLOCK);
            return;
        } else if (hasBarrel()) { // barrel breaks from the damage.
            // Reduce the damage by the remaining barrel health
            pDamageAmount -= barrelHP();
            entityData.set(BARREL, 0);
            playSound(SoundEvents.SHIELD_BREAK);
        }
        this.playSound(SoundRegistry.GOLEM_HURT.get());
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }


    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
        // Play Straw Golem death sound upon its death.
        playSound(SoundRegistry.GOLEM_DEATH.get());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Checking if golem speed needs fixed
        // Hat!
        this.entityData.set(HAT, tag.getBoolean("hat"));
        this.entityData.set(FESTIVE, tag.getBoolean("festive"));
        // I don't think it's necessary to keep golem panicking?
//        this.entityData.set(PANIC, tag.getBoolean("panic"));
        this.entityData.set(CARRY_STATUS, tag.getInt("carry"));
        // Barrel!
        this.entityData.set(BARREL, tag.getInt("barrelHP"));
        this.entityData.set(HUNGER, tag.getInt("hunger"));
        this.entityData.set(LIFE_SPAN, tag.getInt("lifespan"));
        this.entityData.set(PRIORITY_POS, BlockPos.of(tag.getLong("priorityPos")));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        // Loading persistent golem data.
        tag.putBoolean("hat", this.hasHat());
        tag.putBoolean("festive", this.entityData.get(FESTIVE));
        tag.putInt("carry", carryStatus());
        tag.putInt("barrelHP", barrelHP());
        tag.putInt("hunger", getHunger());
        tag.putInt("lifespan", getLifeSpan());
        tag.putLong("priorityPos", this.entityData.get(PRIORITY_POS).asLong());
        super.addAdditionalSaveData(tag);
    }

    /**
     * This method returns an integer depending on the golem's health.
     * @return A status code based on golem health,
     * 0 means essentially full health,
     * 1 means injured,
     * 2 means severely injured.
     */
    public int healthStatus() {
        // basic code to check how dead a golem is.
        // Will return 0 even with minor damage to address lifespan changing health.
        return getHealth() / baseHealth > 0.8 ? 0 : baseHealth * 0.333333 < getHealth() ? 1 : 2;
    }

    /**
     * This method returns an integer depending on the Straw Golem's movement status.
     * @return A status code based on golem movement,
     * 0 means zero movement,
     * 1 means walking,
     * 2 means running.
     */
    public int movementStatus() {
        double movement = getDeltaMovement().horizontalDistance() * level().tickRateManager().tickrate();
        return movement == 0 ? 0 : movement < defaultWalkSpeed * 0.8 ? 1 : 2;
    }

    /**
     * This method returns an integer depending on the Straw Golem's carrying status.
     * @return A status code based on item carrying,
     * 0 means no item,
     * 1 means a regular item,
     * 2 means a block.
     */
    public int carryStatus() {
        return entityData.get(CARRY_STATUS);
    }

    /**
     * Sets the Straw Golem's carry status.
     * @param status The new Straw Golem carry status,
     * 0 means no item,
     * 1 means regular item,
     * 2 means a block.
     */
    public void setCarryStatus(int status) {
        entityData.set(CARRY_STATUS, status);
    }

    /**
     * Determines if the Straw Golem is festive (winter-form).
     * @return Whether the Straw Golem is festive.
     */
    public boolean isFestive() {
        return Golem.winterSkin && entityData.get(FESTIVE);
    }

    /**
     * Determines based on hemisphere if the current season is Winter.
     * @return Whether the current season is Winter.
     */
    private boolean isWinter() {
        Month month = LocalDate.now().getMonth();
        return (Golem.hemisphere.equals("North") && (month == Month.DECEMBER || month == Month.JANUARY))
        || (Golem.hemisphere.equals("South") && (month == Month.JULY || month == Month.AUGUST));
    }

    /**
     * This method returns an integer depending on the golem's pick up status.
     * @return A status code based on if and what the golem is picking up,
     * 0 means not picking up,
     * 1 means picking up an item,
     * 2 means picking up a block.
     */
    public int pickupStatus() {
        return entityData.get(PICKUP_STATUS);
    }

    /**
     * 0 means not picking up, 1 means picking up an item, 2 means picking up a block.
     */
    /**
     * Sets the Straw Golem's pickup status.
     * @param status The new pickup status,
     * 0 means not picking up,
     * 1 means picking up an item,
     * 2 means picking up a block.
     */
    public void setPickupStatus(int status) {
        entityData.set(PICKUP_STATUS, status);
    }

    /**
     * Sets the Straw Golem's pickup status based on the item it is picking up.
     * @param item The item the Straw Golem is picking up.
     */
    public void setPickupStatus(ItemStack item) {
        // If the item is not nothing, continue setting pickup status.
        if (!item.isEmpty()) {
            // If the item is a type of block, set pickup status to 2.
            if (item.getItem() instanceof BlockItem && !(item.getItem() instanceof ItemNameBlockItem)) {
                setPickupStatus(2);
            }
            else {  // Else set pickup status to 1 to indicate a regular item.
                setPickupStatus(1);
            }
        } else {
            // If item is nothing, clear pickup status by setting it to 0.
            setPickupStatus(0);
        }
    }

    /**
     * Checks if the Straw Golem should hold an item above its head.
     * @return Whether the Straw Golem should hold an item above its head.
     */
    public boolean holdItemAbove() {
        // Either the Straw Golem is holding a block,
        // or it is holding an item while wearing a barrel.
        return carryStatus() == 2 || (carryStatus() == 1 && hasBarrel());
    }

    /**
     * Checks if the Straw Golem has a hat.
     * @return Whether the Straw Golem has a hat.
     */
    public boolean hasHat() {
        return entityData.get(HAT);
    }

    /**
     * Gets the health of the Straw Golem's barrel.
     * @return The health of the Straw Golem's barrel.
     */
    public int barrelHP() {
        return entityData.get(BARREL);
    }

    /**
     * Checks if the Straw Golem has a barrel.
     * @return Whether the Straw Golem has a barrel.
     */
    public boolean hasBarrel() {
        // If Barrel Health is not 0, it must have a barrel.
        return barrelHP() != 0;
    }

    /**
     * Sets the Block Position favored by the Straw Golem for delivering items.
     * @param pos The Block Position favored by the Straw Golem for delivering items.
     */
    public void setPriorityPos(BlockPos pos) {
        entityData.set(PRIORITY_POS, pos);
    }

    /**
     * Gets the Block Position favored by the Straw Golem for delivering items.
     * @return The Block Position favored by the Straw Golem for delivering items.
     */
    /**
     * Whether livestock will try to eat this golem. Working golems that must stand
     * among animals (e.g. the breeder) override this to false.
     */
    public boolean isEdibleGolem() {
        return true;
    }

    /**
     * Deposits a stack into the bound priority chest (working golems' "pockets");
     * anything that does not fit is dropped at the golem's feet.
     */
    public void depositToChest(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        BlockPos dest = getPriorityPos();
        ItemStack remainder = stack;
        if (!level().isClientSide && dest.getX() != Integer.MAX_VALUE) {
            Container container = net.minecraft.world.level.block.entity.HopperBlockEntity.getContainerAt(level(), dest);
            if (container != null) {
                remainder = deliverer.insertIntoContainer(container, stack);
            } else {
                remainder = org.hero.strawgolem.platform.Services.PLATFORM.insertItem(level(), dest, stack);
            }
        }
        if (!remainder.isEmpty()) {
            spawnAtLocation(remainder);
        }
    }

    /** Puts the little straw hat on (or takes it off). Used by profession hats. */
    public void setHat(boolean hasHat) {
        entityData.set(HAT, hasHat);
    }

    public BlockPos getPriorityPos() {
        return entityData.get(PRIORITY_POS);
    }

    /**
     * This determines if the Straw Golem is in rain.
     * @return Whether the Straw Golem is in rain.
     */
    private boolean isInRain() {
        BlockPos blockpos = this.blockPosition();
        return this.level().isRainingAt(blockpos) || this.level().isRainingAt(BlockPos.containing(blockpos.getX(), this.getBoundingBox().maxY, blockpos.getZ()));
    }

    /**
     * This determines if the Straw Golem should be considered cold.
     * @return Whether the Straw Golem is cold.
     */
    private boolean isCold() {
        return !this.level().getBiome(this.blockPosition()).value().warmEnoughToRain(this.blockPosition());
    }

    // May make barrel ignore cold shivering?
    /**
     * This determines if the Straw Golem should shiver.
     * Current determinators: Water/Bubble, Powder Snow, Rain without hat, and Cold Biome.
     * @return Whether the Straw Golem should shiver.
     */
    public boolean shouldShiver() {
        return isInWaterOrBubble() || isInPowderSnow || (!hasHat() && isInRain()) || isCold();
    }

    /**
     * This determines if a Straw Golem needs its animation force reset.
     * @return Whether the Straw Golem needs its animation to be force reset.
     */
    public boolean shouldForceAnimationReset() {
        // Check if the Golem needs its animation to be force reset.
        if (forceAnimationReset) {
            // Now that we know it needs it to be force reset, set it back to false.
            forceAnimationReset = false;
            // Return true to indicate the required animation force reset.
            return true;
        } else {
            // If no force reset needed, return false,
            return false;
        }
    }

    /**
     * This method Straw Golem to need to have its animation force reset.
     */
    public void forceAnimationReset() {
        this.forceAnimationReset = true;
    }

    /**
     * Sets the golem's hunger level.
     * @param hunger The number to set as the golem's hunger.
     */
    public void setHunger(int hunger) {
        entityData.set(HUNGER, hunger);
    }

    /**
     * Gets the golem's hunger level.
     */
    public int getHunger() {
        return entityData.get(HUNGER);
    }

    /**
     * Sets the golem's life span.
     * @param life The number to set as the golem's life.
     */
    public void setLifeSpan(int life) {
        entityData.set(LIFE_SPAN, life);
    }

    /**
     * Gets the golem's life span.
     */
    public int getLifeSpan() {
        return entityData.get(LIFE_SPAN);
    }

    /**
     * Determines how harsh a golem's environment is.
     * This will be used for GolemLifeSpan calculations.
     * @return The harshness value of the environment.
     */
    public float getEnvironmentHarshness() {
        float harsh = 1.0f;
        // If in the water, decay should become more rapid.
        if (isInWaterOrBubble()) {
            harsh++;
        }
        // If in the cold, decay should slow.
        if (isInPowderSnow || isCold()) {
            harsh -= 0.5f;
        }
        // If in the rain without a hat, decay should become more rapid.
        if (isInRain() && !hasHat()) {
            harsh++;
        }
        return harsh;
    }

    /**
     * This method checks if the running goal would make the golem be scared.
     * @return Whether the running goal is a fear causing one.
     */
    private boolean isRunningScaredGoal() {
        for (var goal : this.goalSelector.getAvailableGoals()) {
            if (goal.getGoal() instanceof PanicGoal || goal.getGoal() instanceof GolemAvoidEntityGoal<?>) {
                if (goal.isRunning()) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Gets the Straw Golem's panic status.
     * @return The Straw Golem's panic status.
     */
    public boolean getPanic() {
        return Golem.panic && entityData.get(PANIC);
    }

    /**
     * Sets the Straw Golem's panic status
     * @param panic The new panic status.
     */
    public void setPanic(boolean panic) {
        entityData.set(PANIC, panic);
    }

    // ToDo: Move this out of StrawGolem, it can simply be in GolemDepositGoal.
    public class Deliverer {
        BlockPos storagePos;
        BiPredicate<BlockPos> predicate = (gol, pos) ->
                VisionHelper.canSee(gol, pos) && ContainerHelper.isContainer(gol, pos) && ReachHelper.canPath(gol, pos);

        public boolean shouldChangeDeliverable(BlockPos pos) {
            // This is a XOR, basically:
            // if exactly one of these are true, return true else, return false.
            return pos.equals(getPriorityPos()) ^ predicate.filter(StrawGolem.this, getPriorityPos());
        }

        public BlockPos getDeliverable() {
            StrawGolem golem = StrawGolem.this;
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
            if (item.isEmpty()) {
                return;
            }
            // Resolve a vanilla Container; HopperBlockEntity.getContainerAt merges double chests.
            Container container = null;
            if (level instanceof net.minecraft.world.level.Level lvl) {
                container = net.minecraft.world.level.block.entity.HopperBlockEntity.getContainerAt(lvl, pos);
            } else if (level.getBlockEntity(pos) instanceof Container c) {
                container = c;
            }
            if (container != null) {
                StrawGolem.this.setItemSlot(EquipmentSlot.MAINHAND, insertIntoContainer(container, item));
                return;
            }
            // Platform inventories (e.g. NeoForge item handler capability - modded storage).
            ItemStack remainder = org.hero.strawgolem.platform.Services.PLATFORM.insertItem(level, pos, item);
            if (remainder.getCount() != item.getCount()) {
                StrawGolem.this.setItemSlot(EquipmentSlot.MAINHAND, remainder);
            } else {
                // Should in theory never trigger
                LOG.error("Delivery location is not a container! {} {}", item.isEmpty(), ContainerHelper.isContainer(level, pos));
            }
        }

        public ItemStack insertIntoContainer(Container container, ItemStack stack) {
            stack = stack.copy();
            for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
                ItemStack cItem = container.getItem(i);
                if (!cItem.isEmpty() && ItemStack.isSameItemSameComponents(cItem, stack)) {
                    int limit = Math.min(container.getMaxStackSize(), cItem.getMaxStackSize());
                    int move = Math.min(stack.getCount(), limit - cItem.getCount());
                    if (move > 0) {
                        cItem.grow(move);
                        stack.shrink(move);
                        container.setItem(i, cItem);
                    }
                }
            }
            for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
                if (container.getItem(i).isEmpty()) {
                    int limit = Math.min(container.getMaxStackSize(), stack.getMaxStackSize());
                    container.setItem(i, stack.split(limit));
                }
            }
            return stack;
        }

    }

}
