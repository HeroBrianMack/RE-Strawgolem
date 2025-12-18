package org.hero.strawgolem.registry;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.client.particle.SimplerParticleType;
import org.hero.strawgolem.client.particle.SnowFallParticleProvider;
import org.hero.strawgolem.client.particle.SnowParticleProvider;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParticleRegistry {
    public static Supplier<ParticleType<SimpleParticleType>> snow;
    public static Supplier<ParticleType<SimpleParticleType>> snowfall;

    public static <T extends ParticleOptions> Supplier<ParticleType<T>> registerParticle(String id, Supplier<ParticleType<T>> particle) {
        return Constants.COMMON_PLATFORM.registerParticle(id, particle);
    }

    public static void init() {
        snow = registerParticle("snow", () -> new SimplerParticleType(false));
        snowfall = registerParticle("snowfall", () -> new SimplerParticleType(false));
    }

    public static void registerParticleProv(BiConsumer<ParticleType<SimpleParticleType>,
                                            Function<SpriteSet, ParticleProvider<SimpleParticleType>>
                                            > registrar) {
        registrar.accept(snow.get(), SnowParticleProvider::new);
        registrar.accept(snowfall.get(), SnowFallParticleProvider::new);
    }
}
