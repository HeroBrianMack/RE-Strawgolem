package org.hero.strawgolem;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.hero.strawgolem.client.StrawClient;
import org.hero.strawgolem.registry.ParticleRegistry;

@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class StrawNeoClient {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        StrawClient.registerRenderers(event::registerEntityRenderer, event::registerBlockEntityRenderer);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        ParticleRegistry.registerParticleProv((type, factory) -> {
            event.registerSpriteSet(type, factory::apply);
        });
    }
}
