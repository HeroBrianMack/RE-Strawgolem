package org.hero.strawgolem.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

// The generic type of ParticleProvider must match the type of the particle type this provider is for.
public class SnowFallParticleProvider implements ParticleProvider<SimpleParticleType> {
    // A set of particle sprites.
    private final SpriteSet spriteSet;

    // The registration function passes a SpriteSet, so we accept that and store it for further use.
    public SnowFallParticleProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }
    // This is where the magic happens. We return a new particle each time this method is called!
    // The type of the first parameter matches the generic type passed to the super interface.
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z, double xv, double yv, double zv) {
        // We don't use the type and speed, and pass in everything else. You may of course use them if needed.
        return new SnowFallParticle(level, x, y, z, xv, yv, zv, spriteSet);
    }
}