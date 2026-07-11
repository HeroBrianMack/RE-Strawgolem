package org.hero.strawgolem.golem.api;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;

public class ItemHelper {

    /**
     * Returns whether the BlockItem is a crop seed (Stem Block/Crop Block).
     * @param block The BlockItem to test.
     * @return If the BlockItem is a crop seed true, else false.
     */
    public static boolean isSeed(BlockItem block) {

        return block.getBlock() instanceof CropBlock ||     // Is the block a Crop Block or...
                (block.getBlock() instanceof StemBlock) ||  // Is the block a stem block or...
                block.getBlock().defaultBlockState()        // Does the block have an age property?
                        .getProperties().stream().anyMatch(prop ->
                        prop.getName().equalsIgnoreCase("AGE"));
    }
}
