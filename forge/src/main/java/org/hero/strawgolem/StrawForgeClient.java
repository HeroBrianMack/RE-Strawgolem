package org.hero.strawgolem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hero.strawgolem.client.StrawClient;
import org.hero.strawgolem.registry.ParticleRegistry;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class StrawForgeClient {
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