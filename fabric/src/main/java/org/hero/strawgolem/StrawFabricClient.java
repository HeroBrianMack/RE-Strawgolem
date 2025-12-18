package org.hero.strawgolem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.hero.strawgolem.client.StrawClient;
import org.hero.strawgolem.registry.ParticleRegistry;

public final class StrawFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleRegistry.registerParticleProv((type, factory) -> {
            ParticleFactoryRegistry.getInstance().register(
                    type,
                    factory::apply
            );
        });

        StrawClient.registerRenderers(EntityRendererRegistry::register, BlockEntityRenderers::register);
    }
}