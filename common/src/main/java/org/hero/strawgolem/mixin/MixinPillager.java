package org.hero.strawgolem.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.Level;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.nongolem.goals.ConsumeGolemGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pillager.class)
public class MixinPillager extends Mob {
    protected MixinPillager(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci) {
        if (Constants.Golem.raiderAggro) {
            this.targetSelector.addGoal(3, // ToDo: Check if <> breaks anything, it should be fine.
                    new NearestAttackableTargetGoal<>((Pillager)(Object)this, StrawGolem.class, true));
        }
    }

}
