package org.hero.strawgolem.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.hero.strawgolem.golem.BreederGolem;
import org.hero.strawgolem.golem.StockGolem;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.registry.EntityRegistry;

/**
 * Retrains a golem to its next profession (Harvester -> Breeder -> Stocker ->
 * Harvester) by replacing it with a fresh entity of the next type. Memories
 * (bound chests, carry filters) are wiped; position and health carry over.
 */
public class GolemRetrainerItem extends Item {

    public GolemRetrainerItem(Properties properties) {
        super(properties);
    }

    /**
     * Replaces a golem with a fresh one of the given profession at the same spot
     * and health. Memories (bindings, filters, menus) are wiped by design.
     */
    public static StrawGolem convert(StrawGolem golem, EntityType<? extends StrawGolem> next, net.minecraft.world.level.Level level) {
        StrawGolem fresh = next.create(level);
        if (fresh == null) {
            return null;
        }
        fresh.moveTo(golem.getX(), golem.getY(), golem.getZ(), golem.getYRot(), golem.getXRot());
        fresh.setHealth(Math.min(golem.getHealth(), fresh.getMaxHealth()));
        golem.discard();
        level.addFreshEntity(fresh);
        return fresh;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.translatable("item.strawgolem.golem_retrainer.tooltip")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.strawgolem.golem_retrainer.tooltip2")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.ITALIC));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof StrawGolem golem)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Cycle: Harvester -> Breeder -> Stocker -> Miner -> Excavator -> Beekeeper -> Fisher
        // -> Lumberjack -> Smelter -> Cook -> Harvester (sneak reverses).
        boolean reverse = player.isShiftKeyDown();
        EntityType<? extends StrawGolem> next;
        if (golem instanceof org.hero.strawgolem.golem.CookGolem) {
            next = reverse ? EntityRegistry.SMELTERGOLEM.get() : EntityRegistry.STRAWGOLEM.get();
        } else if (golem instanceof org.hero.strawgolem.golem.SmelterGolem) {
            next = reverse ? EntityRegistry.LUMBERJACKGOLEM.get() : EntityRegistry.COOKGOLEM.get();
        } else if (golem instanceof org.hero.strawgolem.golem.LumberjackGolem) {
            next = reverse ? EntityRegistry.FISHERGOLEM.get() : EntityRegistry.SMELTERGOLEM.get();
        } else if (golem instanceof org.hero.strawgolem.golem.FisherGolem) {
            next = reverse ? EntityRegistry.BEEKEEPERGOLEM.get() : EntityRegistry.LUMBERJACKGOLEM.get();
        } else if (golem instanceof org.hero.strawgolem.golem.BeekeeperGolem) {
            next = reverse ? EntityRegistry.EXCAVATORGOLEM.get() : EntityRegistry.FISHERGOLEM.get();
        } else if (golem instanceof org.hero.strawgolem.golem.ExcavatorGolem) {
            next = reverse ? EntityRegistry.MINERGOLEM.get() : EntityRegistry.BEEKEEPERGOLEM.get();
        } else if (golem instanceof org.hero.strawgolem.golem.MinerGolem) {
            next = reverse ? EntityRegistry.STOCKGOLEM.get() : EntityRegistry.EXCAVATORGOLEM.get();
        } else if (golem instanceof StockGolem) {
            next = reverse ? EntityRegistry.BREEDERGOLEM.get() : EntityRegistry.MINERGOLEM.get();
        } else if (golem instanceof BreederGolem) {
            next = reverse ? EntityRegistry.STRAWGOLEM.get() : EntityRegistry.STOCKGOLEM.get();
        } else {
            next = reverse ? EntityRegistry.COOKGOLEM.get() : EntityRegistry.BREEDERGOLEM.get();
        }

        StrawGolem fresh = convert(golem, next, player.level());
        if (fresh == null) {
            return InteractionResult.PASS;
        }

        player.level().playSound(null, fresh.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.NEUTRAL, 0.5F, 1.4F);
        player.displayClientMessage(Component.translatable("strawgolem.retrain", fresh.getName()), true);
        return InteractionResult.SUCCESS;
    }
}
