package org.hero.strawgolem.nongolem.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.hero.strawgolem.golem.StrawGolem;
import org.hero.strawgolem.registry.SoundRegistry;

public class ConsumeGolemGoal extends MeleeAttackGoal {
    public ConsumeGolemGoal(PathfinderMob pMob, boolean pFollowingTargetEvenIfNotSeen) {
        super(pMob, 0.8, pFollowingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && mob.getTarget() instanceof StrawGolem;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity pTarget) {
        // Only adding the null check here as things tend to ignore the nonnull either on accident or purpose.
        if (this.canPerformAttack(pTarget) && pTarget instanceof StrawGolem) {
            this.resetAttackCooldown();
            // Not of fan of this sfx, but for now it'll do.
            mob.playSound(SoundEvents.GENERIC_EAT, 2, 0.5f);
            // May adjust attack range, currently a bit strong
            pTarget.hurt(pTarget.level().damageSources().mobAttack(mob), 0.5F);
            // Straw Golem is most certainly scaredc after being attacked.
            pTarget.playSound(SoundRegistry.GOLEM_SCARED.get());
            // Currently allows persistent attacks, considering loss of aggro after one bite.
        }
    }
}
