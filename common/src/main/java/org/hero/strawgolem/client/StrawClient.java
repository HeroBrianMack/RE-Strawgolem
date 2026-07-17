package org.hero.strawgolem.client;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.registry.EntityRegistry;

import java.util.function.BiConsumer;

public class StrawClient {
    public static void registerRenderers(BiConsumer<EntityType<? extends Entity>, EntityRendererProvider> entityRenderers,
                                         BiConsumer<BlockEntityType<? extends BlockEntity>, BlockEntityRendererProvider> blockEntityRenderers) {
        Constants.LOG.debug("Registering Renderers");
        entityRenderers.accept(EntityRegistry.STRAWGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.BREEDERGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.STOCKGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.MINERGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.BEEKEEPERGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.FISHERGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.LUMBERJACKGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.SMELTERGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.EXCAVATORGOLEM.get(), GolemRenderer::new);
        entityRenderers.accept(EntityRegistry.COOKGOLEM.get(), GolemRenderer::new);
    }
}
