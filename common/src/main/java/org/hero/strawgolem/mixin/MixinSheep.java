package org.hero.strawgolem.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.nongolem.goals.ConsumeGolemGoal;
import org.hero.strawgolem.nongolem.goals.ConsumeV2Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public class MixinSheep extends Mob {

    protected MixinSheep(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci) {
        if (Constants.Golem.animalAggro) {
            // Don't believe the cast is needed....
            this.goalSelector.addGoal(5, new ConsumeV2Goal((Sheep)(Object)this, true));
            this.targetSelector.addGoal(5, // Check if <> breaks anything, it shouldn't in theory
                    new NearestAttackableTargetGoal<>((Sheep)(Object)this, StrawGolem.class, true));
        }
    }

}
