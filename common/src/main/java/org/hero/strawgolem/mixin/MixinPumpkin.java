package org.hero.strawgolem.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import org.hero.strawgolem.golem.StrawGolem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

import static org.hero.strawgolem.registry.EntityRegistry.STRAWGOLEM;

/* If golem spawn has mod conflicts!!!
 Note: User: Bawnorton from fabric project
not really, you can set your priority in the @Mixin annotation, lower applies first, so if you set your priority to like 10,000 then you'll apply later than most people and be first
or set to -10,000 and be last
most of the time
*/
@Mixin(CarvedPumpkinBlock.class)
public abstract class MixinPumpkin {
    @Shadow @Final private static Predicate<BlockState> PUMPKINS_PREDICATE;

    @Shadow
    private static void spawnGolemInWorld(Level pLevel, BlockPattern.BlockPatternMatch pPatternMatch, Entity pGolem, BlockPos pPos) {
    }

    @Unique
    private BlockPattern strawgolem$strawGolemFull;
    @Unique
    private BlockPattern strawgolem$strawGolemBase;

    @ModifyReturnValue(method = "canSpawnGolem", at = @At("RETURN"))
    private boolean modifyCanSpawnGolem(boolean original, LevelReader pLevel, BlockPos pPos) {
        if (!original) {
            return strawgolem$getOrCreateStrawGolemBase().find(pLevel, pPos) != null;
        }
        return true; // original was already true
    }

    @Inject(method = "trySpawnGolem", at = @At("HEAD"))
    private void trySpawnGolem(Level pLevel, BlockPos pPos, CallbackInfo ci) {
        BlockPattern.BlockPatternMatch patternMatch = this.strawgolem$getOrCreateStrawGolemFull().find(pLevel, pPos);
        if (patternMatch != null) {
            StrawGolem strawGolem = STRAWGOLEM.get().create(pLevel);
            if (strawGolem != null) {
                spawnGolemInWorld(pLevel, patternMatch, strawGolem, patternMatch.getBlock(0, 1, 0).getPos());
            }
        }
    }




    @Unique
    private BlockPattern strawgolem$getOrCreateStrawGolemFull() {
        if (this.strawgolem$strawGolemFull == null) {
            this.strawgolem$strawGolemFull = BlockPatternBuilder.start().aisle(new String[]{"^", "#"}).where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.HAY_BLOCK))).build();
        }

        return this.strawgolem$strawGolemFull;
    }

    @Unique
    private BlockPattern strawgolem$getOrCreateStrawGolemBase() {
        if (this.strawgolem$strawGolemBase == null) {
            this.strawgolem$strawGolemBase = BlockPatternBuilder.start().aisle(new String[]{" ", "#"}).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.HAY_BLOCK))).build();
        }

        return this.strawgolem$strawGolemBase;
    }
}
