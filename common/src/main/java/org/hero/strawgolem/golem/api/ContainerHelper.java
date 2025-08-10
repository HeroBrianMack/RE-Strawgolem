package org.hero.strawgolem.golem.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;

public class ContainerHelper {
    public static boolean isContainer(LevelReader levelReader, BlockPos pos) {
        return pos != null && levelReader.getBlockEntity(pos) instanceof Container;
    }

    public static boolean isContainer(Mob mob, BlockPos pos) {
        return isContainer(mob.level(), pos);
    }

}
