package org.hero.strawgolem.golem.api;

import net.minecraft.core.BlockPos;
import org.hero.strawgolem.golem.StrawGolem;

@FunctionalInterface
// could do <Q, T> or something (Honestly it's just specific BiPredicate...)
public interface BiPredicate {
    /**
     * Runs this operation.
     */
    boolean filter(StrawGolem golem, BlockPos pos);
}