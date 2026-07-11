package org.hero.strawgolem.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StemBlock;
import org.hero.strawgolem.mixinInterfaces.StemFruit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(StemBlock.class)
public class MixinStemBlock implements StemFruit {
    @Shadow @Final private ResourceKey<Block> fruit;


    @Override
    public Block strawgolemRewrite$getFruit() {
        final var fruitOptional = BuiltInRegistries.BLOCK.get(fruit);
        // Just safety checks for nulls.
        return fruitOptional.isPresent() && fruitOptional.get().isBound() ? fruitOptional.get().value() : Blocks.AIR;
    }
}
