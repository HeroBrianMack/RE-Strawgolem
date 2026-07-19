package org.hero.strawgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.client.particle.SimplerParticleType;
import org.hero.strawgolem.golem.StrawGolem;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import static org.hero.strawgolem.registry.DataTicketsRegistry.*;

import static org.hero.strawgolem.registry.ParticleRegistry.snow;
import static org.hero.strawgolem.registry.ParticleRegistry.snowfall;

public class GolemHarvestAnimationController extends AnimationController<StrawGolem> {

    private static final RawAnimation[] harvest = {
            //test
            RawAnimation.begin().thenPlay("harvest_item"),
            RawAnimation.begin().thenPlay("harvest_block")
    };

    private static final AnimationStateHandler<StrawGolem> PREDICATE = event -> {
        StrawGolem golem = event.animatable();
        AnimationController<StrawGolem> controller = event.controller();
        // if the golem is picking a block up
        int status = golem.pickupStatus();
        if (status != 0) {
            status--;
            // temporary no config options...
            if (controller.getAnimationState().equals(State.STOPPED)) {
                controller.forceAnimationReset();
            }
            if (golem.hasBarrel() && status < 1) status++;
            // This section has been overcomplicated, but it does work.
            status = Math.max(0, Math.min(harvest.length - 1, status));

            return event.setAndContinue(harvest[status]);

        }
        event.controller().forceAnimationReset();
        return PlayState.STOP;
    };

    public GolemHarvestAnimationController() {
        super("harvest_handler", Constants.Animation.TRANSITION_TIME, PREDICATE);
        // This will likely need changed, but for now it's fine...
        setCustomInstructionKeyframeHandler(event -> {
            if (event.keyframeData().getInstructions().equals("completeHarvest")) {}
        });
        // Disabling this for now.
        setParticleKeyframeHandler(event -> {
            if (event.getRenderState().getGeckolibData(FESTIVE) && event.keyframeData().getEffect().equals("strawgolem:snow")) {
                spawnParticleAtLocator(event.getRenderState(), "locator", (SimplerParticleType) snow.get(), (SimplerParticleType) snowfall.get());
            }
        });
    }

    public void spawnParticleAtLocator(GeoRenderState renderState, String locatorName, SimplerParticleType snow, SimplerParticleType snowfall) {
        GeoBone bon = currentModel.getAnimationProcessor().getBone(locatorName);
        if (bon == null) return;
        // Convert to world coordinates
        Vec3 pos = renderState.getGeckolibData(POSITION).add(new Vec3(-bon.getModelPosition().x / 16.0,
                bon.getModelPosition().y / 16.0,
                bon.getModelPosition().z / 16.0)
                .yRot((float) Math.toRadians(-renderState.getGeckolibData(YROT) + 180)));
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        double speed = 1.0;
        Vec3 lookAngle = renderState.getGeckolibData(ANGLE);
        // Just to make the particles have the correct movement
        double velX = lookAngle.x * speed;
        double velZ = lookAngle.z * speed;
        var random = RandomSource.create();
        for (int i = 0; i < 3; i++) {
            // Just a note: 0.4 is spawn diameter
            double offsetX = (random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (random.nextDouble() - 0.5) * 0.4;
            Minecraft.getInstance().particleEngine.createParticle(snowfall, x + offsetX, y, z + offsetZ, velX * 2, 0, velZ * 2);
        }
        for (int i = 0; i < 100; i++) {
            // Just a note: 0.4 is spawn diameter
            double offsetX = (random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (random.nextDouble() - 0.5) * 0.4;
            Minecraft.getInstance().particleEngine.createParticle(snow, x + offsetX, y, z + offsetZ, velX, 0, velZ);

        }
    }
}
