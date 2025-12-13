package org.hero.strawgolem.golem.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import org.hero.strawgolem.golem.StrawGolem;

@FunctionalInterface
// could do <Q, T> or something (Honestly it's just specific BiPredicate...)
public interface BiPredicate<T> {
    /**
     * Runs this operation.
     */
    boolean filter(StrawGolem golem, T pos);

}