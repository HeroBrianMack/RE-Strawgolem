package org.hero.strawgolem.client;

import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class GolemLegAnimationController extends AnimationController<StrawGolem> {

    private static final RawAnimation[] legs = {
            RawAnimation.begin().thenPlay("legs_idle"),
            RawAnimation.begin().thenPlay("legs_walk"),
            RawAnimation.begin().thenPlay("legs_run")
    };

    private static final AnimationStateHandler<StrawGolem> PREDICATE = event -> {
        StrawGolem golem = event.getAnimatable();
        // if the golem is picking a block up
        if (golem.pickupStatus() != 0) return PlayState.STOP;
        AnimationController<StrawGolem> controller = event.getController();
        if (controller.getAnimationState().equals(State.STOPPED)) {
            controller.forceAnimationReset();
        }
        controller.setAnimation(legs[golem.movementStatus()]);
        return PlayState.CONTINUE;
    };

    public GolemLegAnimationController(StrawGolem animatable) {
        super(animatable, "legs_handler", Constants.Animation.TRANSITION_TIME, PREDICATE);
    }
}
