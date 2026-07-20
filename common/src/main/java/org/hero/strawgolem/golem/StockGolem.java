package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.golem.goals.StockDeliverGoal;
import org.hero.strawgolem.golem.goals.StockWithdrawGoal;

/**
 * A courier golem that ferries items between two player-bound containers.
 * Ordering flow: select the golem (shift + empty hand), then the first container
 * clicked becomes the SOURCE (collect from) and the second becomes the
 * DESTINATION (deliver to, stored as the regular priority pos).
 */
public class StockGolem extends StrawGolem {
    public static final BlockPos UNSET = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private BlockPos sourcePos = UNSET;
    /** Transient ordering state: source was just set, next container click binds the destination. */
    private boolean awaitingDestination = false;
    /** Item whitelist; empty = carry anything. Toggled by sneak-clicking the golem with an item. */
    private final java.util.Set<net.minecraft.world.item.Item> carryFilter = new java.util.LinkedHashSet<>();

    public StockGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    public BlockPos getSourcePos() {
        return sourcePos;
    }

    public void setSourcePos(BlockPos pos) {
        this.sourcePos = pos == null ? UNSET : pos;
    }

    public boolean hasSourcePos() {
        return sourcePos.getX() != Integer.MAX_VALUE;
    }

    public boolean hasDestinationPos() {
        return getPriorityPos().getX() != Integer.MAX_VALUE;
    }

    public boolean isAwaitingDestination() {
        return awaitingDestination;
    }

    public void setAwaitingDestination(boolean awaiting) {
        this.awaitingDestination = awaiting;
    }

    /** True when the golem may carry this stack (empty filter = anything). */
    public boolean filterAllows(net.minecraft.world.item.ItemStack stack) {
        return carryFilter.isEmpty() || carryFilter.contains(stack.getItem());
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !held.isEmpty() && !(held.getItem() instanceof org.hero.strawgolem.item.GolemRetrainerItem)) {
            if (!level().isClientSide) {
                net.minecraft.world.item.Item item = held.getItem();
                if (carryFilter.remove(item)) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            carryFilter.isEmpty() ? "strawgolem.stockfilter.cleared" : "strawgolem.stockfilter.removed",
                            held.getHoverName()), true);
                } else {
                    carryFilter.add(item);
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            "strawgolem.stockfilter.added", held.getHoverName()), true);
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putLong("StockSourcePos", sourcePos.asLong());
        net.minecraft.nbt.ListTag filters = new net.minecraft.nbt.ListTag();
        for (net.minecraft.world.item.Item item : carryFilter) {
            filters.add(net.minecraft.nbt.StringTag.valueOf(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        pCompound.put("StockCarryFilter", filters);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("StockSourcePos")) {
            sourcePos = BlockPos.of(pCompound.getLong("StockSourcePos"));
        }
        carryFilter.clear();
        net.minecraft.nbt.ListTag filters = pCompound.getList("StockCarryFilter", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < filters.size(); i++) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(filters.getString(i));
            if (id != null) {
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id).ifPresent(carryFilter::add);
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(2, new GolemWanderGoal(this));
        goalSelector.addGoal(1, new StockWithdrawGoal(this));
        goalSelector.addGoal(1, new StockDeliverGoal(this));
    }
}
