package org.hero.strawgolem.client;

import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class GolemHarvestAnimationController extends AnimationController<StrawGolem> {

    private static final RawAnimation[] harvest = {
            //test
            RawAnimation.begin().thenPlay("harvest_item"),
            RawAnimation.begin().thenPlay("harvest_block")
    };

    private static final AnimationStateHandler<StrawGolem> PREDICATE = event -> {
        StrawGolem golem = event.getAnimatable();
        AnimationController<StrawGolem> controller = event.getController();
        // if the golem is picking a block up
        int status = golem.pickupStatus();
        if (status != 0) {
            status--;
            // temporary no config options...
            if (controller.getAnimationState().equals(State.STOPPED)) {
                controller.forceAnimationReset();
            }
            if (golem.hasBarrel()) status++;
            return event.setAndContinue(harvest[status]);

        }
        event.getController().forceAnimationReset();
        return PlayState.STOP;
    };

    public GolemHarvestAnimationController(StrawGolem animatable) {
        super(animatable, "harvest_handler", Constants.Animation.TRANSITION_TIME, PREDICATE);
        // This will likely need changed, but for now it's fine...
        setCustomInstructionKeyframeHandler(event -> {
            if (event.getKeyframeData().getInstructions().equals("completeHarvest")) {}
        });
    }
}
