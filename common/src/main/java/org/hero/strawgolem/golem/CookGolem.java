package org.hero.strawgolem.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.goals.CookCraftGoal;
import org.hero.strawgolem.golem.goals.CookStashGoal;
import org.hero.strawgolem.golem.goals.GolemWanderGoal;
import org.hero.strawgolem.item.GolemRetrainerItem;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A cook golem: show it food items (sneak-click) to build its menu, bind it a
 * pantry chest, and give it a crafting table to work at. It crafts menu dishes
 * from pantry ingredients - covering vanilla and the many modded foods that use
 * plain crafting recipes.
 */
public class CookGolem extends StrawGolem {

    /** Dishes this cook tries to keep making; empty = off duty. */
    private final Set<Item> menu = new LinkedHashSet<>();

    public CookGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isEdibleGolem() {
        return false;
    }

    public boolean hasMenu() {
        return !menu.isEmpty();
    }

    public Set<Item> getMenu() {
        return menu;
    }

    public boolean hasDepositChest() {
        return getPriorityPos().getX() != Integer.MAX_VALUE;
    }

    /** Nearest crafting table in working range - the cook's kitchen. */
    public BlockPos findKitchen() {
        BlockPos center = blockPosition();
        int r = Golem.searchRange;
        int rv = Golem.searchRangeVertical;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -rv, -r), center.offset(r, rv, r))) {
            if (level().getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
                double dist = pos.distToCenterSqr(getX(), getY(), getZ());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !held.isEmpty() && !(held.getItem() instanceof GolemRetrainerItem)) {
            if (held.get(DataComponents.FOOD) != null) {
                if (!level().isClientSide) {
                    Item item = held.getItem();
                    if (menu.remove(item)) {
                        player.displayClientMessage(Component.translatable(
                                menu.isEmpty() ? "strawgolem.cookmenu.cleared" : "strawgolem.cookmenu.removed",
                                held.getHoverName()), true);
                    } else {
                        menu.add(item);
                        player.displayClientMessage(Component.translatable(
                                "strawgolem.cookmenu.added", held.getHoverName()), true);
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
        ListTag list = new ListTag();
        for (Item item : menu) {
            list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        pCompound.put("CookMenu", list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        menu.clear();
        ListTag list = pCompound.getList("CookMenu", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(menu::add);
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, Golem.defaultRunSpeed * 1.2));
        generateAvoids();
        goalSelector.addGoal(1, new CookCraftGoal(this));
        goalSelector.addGoal(2, new CookStashGoal(this));
        goalSelector.addGoal(2, new GolemWanderGoal(this));
    }
}
