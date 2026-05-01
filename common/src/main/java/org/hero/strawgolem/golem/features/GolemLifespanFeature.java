package org.hero.strawgolem.golem.features;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

public class GolemLifespanFeature implements IGolemTickFeature {
    private int waitTime = 20;
    private int counter = 0;
    private StrawGolem golem;

    /**
     * Constructor for the Golem Life Span Feature
     * @param golem The Straw Golem the life span is bound to.
     */
    public GolemLifespanFeature(StrawGolem golem) {
        this.golem = golem;
    }

    /**
     * This tick method will increment a counter, and after twenty counter increments
     * the Straw Golem's life span will increment by one.
     */
    public void tick() {
        if (golem.getLifeSpan() >= Constants.Golem.maxLife) {
            // Kill golem if its lived past its maximum lifespan
            golem.kill();
            return;
        } else if (golem.getLifeSpan() < 0) {
            // Should never trigger, but better to be cautious
            // May have it simply kill the golem if it goes negative, but for now just reset.
            golem.setLifeSpan(0);
        }
        counter++;
        if (counter == waitTime) {
            golem.setLifeSpan(golem.getLifeSpan() + 1);
            counter = 0;
        }
        updateGolemHealth();
    }

    /**
     * This method updates the Straw Golem's health based on its lifespan.
     */
    public void updateGolemHealth() {
        // The casting probably could be simplified.
        // Using 3.0 to scale the speed, note that as lifespan -> maxLife the division should approach 1
        // Using 0.000001 as an epsilon, just to prevent any problems with imprecision.
        float epsilon = 0.000001f;
        float healthRatio = (float) Math.floor(epsilon + -3.0
                * (double) golem.getLifeSpan() / Constants.Golem.maxLife);
        // Flipping the order, so that the closer life span is the max lifespan, the closer to 0.0f the health becomes.
        healthRatio += 3.0f + epsilon;
        var attr = golem.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            // Normalizing speedRatio, since I don't want Golems moving triple speed.
            attr.setBaseValue(Constants.Golem.maxHealth * Math.min(1.0f, healthRatio / 3.0f));
        } else {
            // Should never trigger, but best to be safe.
            Constants.LOG.error("Golem missing Attribute: {}!", "Max Health");
        }
    }
}
