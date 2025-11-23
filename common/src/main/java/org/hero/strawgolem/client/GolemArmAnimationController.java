package org.hero.strawgolem.client;

import net.minecraft.client.Minecraft;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class GolemArmAnimationController extends AnimationController<StrawGolem> {

    private static final RawAnimation[] arms = {
            RawAnimation.begin().thenPlay("arms_idle"),
            RawAnimation.begin().thenPlay("arms_walk"),
            RawAnimation.begin().thenPlay("arms_run"),
            RawAnimation.begin().thenPlay("arms_hold_item"),
            RawAnimation.begin().thenPlay("arms_hold_block"),
            RawAnimation.begin().thenPlay("arms_scared")
    };

    private static final AnimationStateHandler<StrawGolem> PREDICATE = event -> {
        StrawGolem golem = event.getAnimatable();
        // if the golem is picking a block up
        if (golem.pickupStatus() != 0 || golem.shouldForceAnimationReset()) return PlayState.STOP;
        AnimationController<StrawGolem> controller = event.getController();
        if (controller.getAnimationState().equals(State.STOPPED)) {
            controller.forceAnimationReset();
        }
        if (golem.isScared()) {
            controller.setAnimation(arms[5]);
        } else if (golem.holdItemAbove()) {
            controller.setAnimation(arms[4]);
        } else if (golem.carryStatus() != 0) {
            controller.setAnimation(arms[3]);
        } else {
            controller.setAnimation(arms[golem.movementStatus()]);
        }
        return PlayState.CONTINUE;
    };

    public GolemArmAnimationController(StrawGolem animatable) {
        super(animatable, "arms_handler", Constants.Animation.TRANSITION_TIME, PREDICATE);
    }
}