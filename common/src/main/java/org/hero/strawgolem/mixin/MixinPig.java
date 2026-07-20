package org.hero.strawgolem.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.nongolem.goals.ConsumeGolemGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pig.class)
public class MixinPig extends Mob {

    protected MixinPig(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci) {
        if (Constants.Golem.animalAggro) {
            this.goalSelector.addGoal(5, new ConsumeGolemGoal((Pig)(Object)this, true));
            this.targetSelector.addGoal(5, // Check if <> breaks anything, it shouldn't in theory
                    new NearestAttackableTargetGoal<>((Pig)(Object)this, StrawGolem.class, 10, true, false, (e) -> e instanceof StrawGolem golem && golem.isEdibleGolem()));
        }
    }
}
