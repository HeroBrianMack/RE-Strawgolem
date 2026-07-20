package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.golem.goals.LumberChopGoal;
import org.hero.strawgolem.golem.goals.LumberFetchGoal;
import org.hero.strawgolem.golem.goals.LumberStashGoal;
import org.hero.strawgolem.platform.Services;

import java.util.function.Predicate;

/**
 * A lumberjack golem that fells trees (connected log clusters wearing leaves -
 * player builds are safe), deposits the logs into its bound chest, and replants
 * a sapling where the trunk stood.
 */
public class LumberjackGolem extends StrawGolem {

    public LumberjackGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    public boolean hasDepositChest() {
        return getPriorityPos().getX() != Integer.MAX_VALUE;
    }

    /** Withdraws the first matching stack from the bound chest ("pockets" access). */
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

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(1, new LumberFetchGoal(this));
        goalSelector.addGoal(1, new LumberChopGoal(this));
        goalSelector.addGoal(2, new LumberStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
