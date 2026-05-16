package org.hero.strawgolem.nongolem.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class ConsumeV2Goal extends MeleeAttackGoal {
    public ConsumeV2Goal(PathfinderMob pMob, boolean pFollowingTargetEvenIfNotSeen) {
        super(pMob, 0.8, pFollowingTargetEvenIfNotSeen);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity pTarget) {
        if (this.canPerformAttack(pTarget)) {
            this.resetAttackCooldown();
            // Not of fan of this sfx, but for now it'll do.
            mob.playSound(SoundEvents.GENERIC_EAT, 2, 0.5f);
            // May adjust attack range, currently a bit strong
            pTarget.hurt(pTarget.level().damageSources().mobAttack(mob), 0.5F);
            // Currently allows persistent attacks, considering loss of aggro after one bite.
        }
    }
}
