package org.hero.strawgolem.platform.services;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    Path getConfigPath();
    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, Supplier<BlockEntityType<T>> blockEntityType);
    <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block);
    <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entity);
    <T extends ArmorMaterial> Holder<T> registerArmorMaterial(String id, Supplier<T> armorMaterial);
    <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item);
    <T extends SoundEvent> Supplier<T> registerSound(String id, Supplier<T> sound);
    <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(String id, Supplier<T> tab);
    <T extends ParticleOptions> Supplier<ParticleType<T>> registerParticle(String id, Supplier<ParticleType<T>> particle);
    <E extends Mob> Supplier<SpawnEggItem> makeSpawnEggFor(Supplier<EntityType<E>> entityType, int primaryEggColour, int secondaryEggColour, Item.Properties itemProperties);
    CreativeModeTab.Builder newCreativeTabBuilder();

    /**
     * Checks whether the block at pos exposes a platform-specific item inventory
     * (e.g. a NeoForge IItemHandler capability) even if it is not a vanilla Container.
     * Lets golems deliver to modded storage such as Sophisticated Storage.
     */
    default boolean isItemReceiver(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos) {
        return false;
    }

    /**
     * Inserts into a platform-specific item inventory at pos. Returns the remainder.
     */
    default net.minecraft.world.item.ItemStack insertItem(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack stack) {
        return stack;
    }

    /**
     * Simulates inserting into a platform-specific item inventory at pos.
     * Returns the remainder that would not fit. Default: nothing fits.
     */
    default net.minecraft.world.item.ItemStack insertItemSimulate(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack stack) {
        return stack;
    }

    /**
     * Extracts up to maxCount items matching the predicate from a platform-specific
     * item inventory at pos. Simulate=true only peeks. Returns EMPTY when nothing matches.
     */
    default net.minecraft.world.item.ItemStack extractMatching(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, java.util.function.Predicate<net.minecraft.world.item.ItemStack> predicate, int maxCount, boolean simulate) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
