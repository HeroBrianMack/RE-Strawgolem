package org.hero.strawgolem.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;

/**
 * A dwarf-hearted digger: the miner's brain with a shovel in its hand. Show it
 * gravel, sand, dirt or clay (sneak-click with the block) and it excavates.
 */
public class ExcavatorGolem extends MinerGolem {

    public ExcavatorGolem(EntityType<? extends StrawGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean isMiningTool(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }

    @Override
    protected String filterLangPrefix() {
        return "strawgolem.digfilter";
    }
}
