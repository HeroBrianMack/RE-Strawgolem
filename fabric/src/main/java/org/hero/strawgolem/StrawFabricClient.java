package org.hero.strawgolem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.hero.strawgolem.client.StrawClient;

public final class StrawFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StrawClient.registerRenderers(EntityRendererRegistry::register, BlockEntityRenderers::register);
    }
}