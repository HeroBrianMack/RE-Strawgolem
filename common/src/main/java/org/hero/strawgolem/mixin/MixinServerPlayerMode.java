package org.hero.strawgolem.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.mixinInterfaces.GolemOrderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerMode {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void strawgolem_onShiftRightClickContainer(
            ServerPlayer pPlayer, Level pLevel,
            ItemStack pStack, InteractionHand pHand,
            BlockHitResult pHitResult, CallbackInfoReturnable<InteractionResult> cir)
    {
        // Check if player is shifting
        if (!pPlayer.isShiftKeyDown() || !pPlayer.getMainHandItem().isEmpty()) {
            return;
        }

        BlockPos blockPos = pHitResult.getBlockPos();
        BlockEntity blockEntity = pLevel.getBlockEntity(blockPos);

        // Confirm block is container
        if (blockEntity instanceof Container) {
            if (((Object) pPlayer) instanceof GolemOrderer orderer) {
                StrawGolem golem = orderer.strawgolemRewrite$getGolem();
                if (golem != null && golem.isAlive()) {
                    golem.setPriorityPos(blockPos);
                    pPlayer.displayClientMessage(Component.translatable(
                            "strawgolem.ordering.complete"), true);

                    cir.setReturnValue(InteractionResult.SUCCESS);
                    cir.cancel();
                } else if (golem != null && !golem.isAlive()) {
                    pPlayer.displayClientMessage(Component.translatable(
                            "strawgolem.ordering.failure"), true);
                }
                orderer.strawgolemRewrite$setGolem(null);
            }
        }
    }
}
