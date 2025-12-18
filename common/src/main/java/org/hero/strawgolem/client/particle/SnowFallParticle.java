package org.hero.strawgolem.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SnowFallParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    // First four parameters are self-explanatory. The SpriteSet parameter is provided by the
    // ParticleProvider, see below. You may also add additional parameters as needed, e.g. xSpeed/ySpeed/zSpeed.
    public SnowFallParticle(ClientLevel level, double x, double y, double z, double xv, double yv, double zv, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;
        this.gravity = 2.0f; // Our particle floats in midair now, because why not.
        this.quadSize = 0.2f;  // makes particle visible
        this.lifetime = 6;    // 0.3 sec
        float tmp = (float) (level.getRandom().nextFloat() * 2 * Math.PI);
        roll = tmp;
        oRoll = tmp;
        friction = 0;
        setParticleSpeed(xv/20, yv/20, zv/20);
        // We set the initial sprite here since ticking is not guaranteed to set the sprite
        // before the render method is called.
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        // Set the sprite for the current particle age, i.e. advance the animation.
        this.setSpriteFromAge(spriteSet);
        // Let super handle further movement. You may replace this with your own movement if needed.
        // You may also override move() if you only want to modify the built-in movement.
        this.oRoll = roll;
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}