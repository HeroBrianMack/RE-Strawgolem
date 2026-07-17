package org.hero.strawgolem.golem.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.FisherGolem;

import java.util.EnumSet;

/**
 * Stands at the water's edge with a rod and periodically pulls a catch from
 * the vanilla fishing loot table (fish, junk, and the occasional treasure),
 * depositing it into the bound chest.
 */
public class FisherFishGoal extends Goal {
    private static final double FISH_DIST_SQ = 9.0;

    private final FisherGolem golem;
    private BlockPos waterPos;
    private int castTicks;
    private int waitTicks;

    public FisherFishGoal(FisherGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean holdingRod(FisherGolem golem) {
        return golem.getMainHandItem().getItem() instanceof FishingRodItem;
    }

    /** Nearest open water surface (water with air above) in the working volume. */
    public static BlockPos findWater(FisherGolem golem) {
        BlockPos center = golem.blockPosition();
        int r = Golem.searchRange;
        int rv = Golem.searchRangeVertical;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -rv, -r), center.offset(r, rv, r))) {
            if (!golem.level().getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)
                    || !golem.level().getBlockState(pos.above()).isAir()) {
                continue;
            }
            double dist = pos.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    @Override
    public boolean canUse() {
        if (!holdingRod(golem) || !golem.hasDepositChest()) {
            return false;
        }
        waterPos = findWater(golem);
        return waterPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return waterPos != null && holdingRod(golem)
                && golem.level().getFluidState(waterPos).is(net.minecraft.tags.FluidTags.WATER);
    }

    @Override
    public void start() {
        castTicks = 0;
        newWait();
        moveToWater();
    }

    @Override
    public void stop() {
        waterPos = null;
        castTicks = 0;
        golem.getNavigation().stop();
    }

    private void newWait() {
        waitTicks = 200 + golem.getRandom().nextInt(200);
    }

    private void moveToWater() {
        golem.getNavigation().moveTo(waterPos.getX() + 0.5, waterPos.getY(), waterPos.getZ() + 0.5, Golem.defaultWalkSpeed);
    }

    @Override
    public void tick() {
        if (waterPos == null) {
            return;
        }
        golem.getLookControl().setLookAt(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5);
        if (waterPos.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) > FISH_DIST_SQ || golem.isInWater()) {
            if (golem.getNavigation().isDone()) {
                moveToWater();
            }
            castTicks = 0;
            return;
        }
        golem.getNavigation().stop();
        if (++castTicks < waitTicks) {
            return;
        }
        if (golem.level() instanceof ServerLevel level) {
            ItemStack rod = golem.getMainHandItem();
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(waterPos))
                    .withParameter(LootContextParams.TOOL, rod)
                    .create(LootContextParamSets.FISHING);
            var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
            for (ItemStack catchStack : table.getRandomItems(params)) {
                golem.depositToChest(catchStack);
            }
            level.playSound(null, waterPos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.6F, 1.0F);
            rod.hurtAndBreak(1, golem, EquipmentSlot.MAINHAND);
        }
        castTicks = 0;
        newWait();
    }
}
