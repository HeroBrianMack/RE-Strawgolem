package org.hero.strawgolem.golem.goals;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import org.hero.strawgolem.Constants.Golem;
import org.hero.strawgolem.golem.BreederGolem;

import java.util.EnumSet;
import java.util.List;

/**
 * Feeds pairs of compatible adult animals with the food the golem is holding,
 * setting them in love so they breed. A target is valid when at least one other
 * animal of the same species can complete the pair (either also feedable, or
 * already in love).
 */
public class BreederBreedGoal extends Goal {
    private static final double FEED_DIST_SQ = 4.0;
    private static final int FEED_COOLDOWN_TICKS = 30;

    private final BreederGolem golem;
    private Animal target;
    private int cooldown;

    public BreederBreedGoal(BreederGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** Adult animals that are ready to breed and would eat the given food. */
    public static List<Animal> feedableAnimals(BreederGolem golem, ItemStack food) {
        return golem.level().getEntitiesOfClass(Animal.class,
                golem.getBoundingBox().inflate(Golem.searchRange),
                a -> a.isAlive() && a.getAge() == 0 && !a.isInLove() && a.canFallInLove() && a.isFood(food));
    }

    /**
     * Nearest animal worth feeding with this food: it must be feedable and a pair
     * must be completable (a second feedable of the same species, or one already
     * in love). Returns null when this food has no takers right now.
     */
    public static Animal findTarget(BreederGolem golem, ItemStack food) {
        if (food.isEmpty()) {
            return null;
        }
        List<Animal> feedable = feedableAnimals(golem, food);
        Animal best = null;
        double bestDist = Double.MAX_VALUE;
        for (Animal candidate : feedable) {
            EntityType<?> type = candidate.getType();
            // Per-species population cap: every individual of the species counts,
            // including babies and animals on breeding cooldown.
            int population = golem.level().getEntitiesOfClass(Animal.class,
                    golem.getBoundingBox().inflate(Golem.searchRange),
                    a -> a.getType() == type).size();
            if (population >= Golem.breederPopulationCap) {
                continue;
            }
            long sameTypeFeedable = feedable.stream().filter(a -> a.getType() == type).count();
            boolean pairable = sameTypeFeedable >= 2 || !golem.level().getEntitiesOfClass(Animal.class,
                    golem.getBoundingBox().inflate(Golem.searchRange),
                    a -> a.getType() == type && a.isInLove()).isEmpty();
            if (!pairable) {
                continue;
            }
            double dist = golem.distanceToSqr(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        target = findTarget(golem, golem.getMainHandItem());
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        ItemStack held = golem.getMainHandItem();
        return target != null && target.isAlive() && !target.isInLove()
                && target.canFallInLove() && !held.isEmpty() && target.isFood(held);
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(target, Golem.defaultWalkSpeed);
    }

    @Override
    public void stop() {
        target = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        golem.getLookControl().setLookAt(target);
        if (golem.distanceToSqr(target) > FEED_DIST_SQ) {
            golem.getNavigation().moveTo(target, Golem.defaultWalkSpeed);
            return;
        }
        // Close enough: feed the animal.
        ItemStack food = golem.getMainHandItem();
        food.shrink(1);
        golem.setItemSlot(EquipmentSlot.MAINHAND, food);
        target.setInLove(null);
        golem.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL, 0.6F, 0.9F + golem.level().random.nextFloat() * 0.2F);
        cooldown = FEED_COOLDOWN_TICKS;
        target = null;
    }
}
