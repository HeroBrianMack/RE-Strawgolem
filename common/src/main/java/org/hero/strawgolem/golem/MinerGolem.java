package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.Container;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.golem.goals.MinerFetchGoal;
import org.hero.strawgolem.golem.goals.MinerMineGoal;
import org.hero.strawgolem.golem.goals.MinerStashGoal;
import org.hero.strawgolem.item.GolemRetrainerItem;
import org.hero.strawgolem.platform.Services;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A miner golem that breaks only the block types a player has shown it
 * (sneak-click with a block item to toggle). Fetches a pickaxe from its bound
 * chest, mines targets in range, and deposits the drops back into the chest.
 */
public class MinerGolem extends StrawGolem {

    /** Blocks this miner is allowed to break; empty = no mining at all. */
    private final Set<Block> mineFilter = new LinkedHashSet<>();

    public MinerGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    public boolean hasMineFilter() {
        return !mineFilter.isEmpty();
    }

    public boolean filterMatches(Block block) {
        return mineFilter.contains(block);
    }

    public boolean hasDepositChest() {
        return getPriorityPos().getX() != Integer.MAX_VALUE;
    }

    /** The tool family this digger works with; the excavator overrides to shovels. */
    public boolean isMiningTool(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.world.item.PickaxeItem;
    }

    /** Lang key prefix for filter feedback; the excavator overrides for digger wording. */
    protected String filterLangPrefix() {
        return "strawgolem.minefilter";
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !held.isEmpty() && !(held.getItem() instanceof GolemRetrainerItem)) {
            if (held.getItem() instanceof BlockItem blockItem) {
                if (!level().isClientSide) {
                    Block block = blockItem.getBlock();
                    if (mineFilter.remove(block)) {
                        player.displayClientMessage(Component.translatable(
                                mineFilter.isEmpty() ? filterLangPrefix() + ".cleared" : filterLangPrefix() + ".removed",
                                held.getHoverName()), true);
                    } else {
                        mineFilter.add(block);
                        player.displayClientMessage(Component.translatable(
                                filterLangPrefix() + ".added", held.getHoverName()), true);
                    }
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        ListTag filters = new ListTag();
        for (Block block : mineFilter) {
            filters.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(block).toString()));
        }
        pCompound.put("MinerFilter", filters);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        mineFilter.clear();
        ListTag filters = pCompound.getList("MinerFilter", Tag.TAG_STRING);
        for (int i = 0; i < filters.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(filters.getString(i));
            if (id != null) {
                BuiltInRegistries.BLOCK.getOptional(id).ifPresent(mineFilter::add);
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(1, new MinerFetchGoal(this));
        goalSelector.addGoal(1, new MinerMineGoal(this));
        goalSelector.addGoal(2, new MinerStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
