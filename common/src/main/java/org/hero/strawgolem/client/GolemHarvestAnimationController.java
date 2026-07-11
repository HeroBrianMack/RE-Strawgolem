package org.hero.strawgolem.client;

import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

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
//        setParticleKeyframeHandler(event -> {
//            event.
//            if (event.isFestive() && event.keyframeData().getEffect().equals("strawgolem:snow")) animatable.createSnow = true;
//        });


    }
}
