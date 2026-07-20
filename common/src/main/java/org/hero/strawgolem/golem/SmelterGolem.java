package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.golem.goals.SmelterCollectGoal;
import org.hero.strawgolem.golem.goals.SmelterStashGoal;
import org.hero.strawgolem.golem.goals.SmelterSupplyGoal;
import org.hero.strawgolem.platform.Services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A smelter golem that tends every furnace, blast furnace, and smoker in range:
 * feeds them smeltable input and fuel from its bound chest and carries the
 * finished output back.
 */
public class SmelterGolem extends StrawGolem {

    public SmelterGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    public boolean hasDepositChest() {
        return getPriorityPos().getX() != Integer.MAX_VALUE;
    }

    /** All furnace-family block entities in working range, nearest first. */
    public List<BlockPos> findFurnaces() {
        BlockPos center = blockPosition();
        int r = Golem.searchRange;
        int rv = Golem.searchRangeVertical;
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -rv, -r), center.offset(r, rv, r))) {
            if (level().getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity) {
                found.add(pos.immutable());
            }
        }
        found.sort(Comparator.comparingDouble(p -> p.distToCenterSqr(getX(), getY(), getZ())));
        return found;
    }

    /** The recipe type a given furnace block entity smelts with. */
    public RecipeType<? extends net.minecraft.world.item.crafting.AbstractCookingRecipe> recipeTypeFor(AbstractFurnaceBlockEntity furnace) {
        if (furnace instanceof BlastFurnaceBlockEntity) {
            return RecipeType.BLASTING;
        }
        if (furnace instanceof SmokerBlockEntity) {
            return RecipeType.SMOKING;
        }
        return RecipeType.SMELTING;
    }

    /** Whether this stack has a cooking recipe for the given furnace's type. */
    public boolean smeltableIn(AbstractFurnaceBlockEntity furnace, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return level().getRecipeManager()
                .getRecipeFor(recipeTypeFor(furnace), new SingleRecipeInput(stack), level())
                .isPresent();
    }

    /** Withdraws the first matching stack from the bound chest. */
    public ItemStack takeFromChest(Predicate<ItemStack> predicate, int max) {
        Level level = level();
        BlockPos chest = getPriorityPos();
        if (chest.getX() == Integer.MAX_VALUE) {
            return ItemStack.EMPTY;
        }
        if (level.getBlockEntity(chest) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (predicate.test(slot)) {
                    return container.removeItem(i, Math.min(max, slot.getCount()));
                }
            }
            return ItemStack.EMPTY;
        }
        return Services.PLATFORM.extractMatching(level, chest, predicate, max, false);
    }

    /** Whether the bound chest holds a matching stack. */
    public boolean chestHas(Predicate<ItemStack> predicate) {
        Level level = level();
        BlockPos chest = getPriorityPos();
        if (chest.getX() == Integer.MAX_VALUE) {
            return false;
        }
        if (level.getBlockEntity(chest) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (predicate.test(container.getItem(i))) {
                    return true;
                }
            }
            return false;
        }
        return !Services.PLATFORM.extractMatching(level, chest, predicate, 1, true).isEmpty();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(1, new SmelterCollectGoal(this));
        goalSelector.addGoal(1, new SmelterSupplyGoal(this));
        goalSelector.addGoal(2, new SmelterStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
