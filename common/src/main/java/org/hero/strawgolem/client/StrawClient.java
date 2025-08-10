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
        Constants.LOG.debug("LOAD REGISTERS!!!");
        entityRenderers.accept(EntityRegistry.STRAWGOLEM.get(), GolemRenderer::new);
    }
}
