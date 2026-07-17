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
import org.hero.strawgolem.golem.StrawGolem;

import java.util.function.Supplier;

/**
 * A trade hat: place it on any straw golem to assign that profession. The
 * golem keeps its position and health, wipes its memories, and proudly wears
 * the hat. This is the survival hiring path; spawn eggs are creative-only.
 */
public class ProfessionHatItem extends Item {

    private final Supplier<EntityType<? extends StrawGolem>> profession;

    public ProfessionHatItem(Properties properties, Supplier<EntityType<? extends StrawGolem>> profession) {
        super(properties);
        this.profession = profession;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof StrawGolem golem)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        EntityType<? extends StrawGolem> type = profession.get();
        if (golem.getType() == type) {
            player.displayClientMessage(Component.translatable("strawgolem.hat.already"), true);
            return InteractionResult.SUCCESS;
        }
        StrawGolem fresh = GolemRetrainerItem.convert(golem, type, player.level());
        if (fresh == null) {
            return InteractionResult.PASS;
        }
        fresh.setHat(true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.level().playSound(null, fresh.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.NEUTRAL, 0.5F, 1.6F);
        player.displayClientMessage(Component.translatable("strawgolem.hat.applied", fresh.getName()), true);
        return InteractionResult.SUCCESS;
    }
}
