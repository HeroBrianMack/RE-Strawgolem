package org.hero.strawgolem.golem.features;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.hero.strawgolem.Constants;
import org.hero.strawgolem.golem.StrawGolem;

public class GolemHungerFeature implements IGolemTickFeature {
    private int waitTime = 20;
    private int counter = 0;
    private StrawGolem golem;

    /**
     * Constructor for the Golem Hunger Feature
     * @param golem The Straw Golem the hunger is bound to.
     */
    public GolemHungerFeature(StrawGolem golem) {
        this.golem = golem;
    }

    // For now just increment every second, will consider reduction later.

    /**
     * This tick method will increment a counter, and after twenty counter increments
     * the Straw Golem's hunger will increment by one.
     */
    public void tick() {
        if (golem.getHunger() >= Constants.Golem.maxHunger) {
            updateGolemSpeed();
            return;
        } else if (golem.getHunger() < 0) {
            // Should never trigger, but better to be cautious
            golem.setHunger(0);
        }
        counter++;
        if (counter == waitTime) {
            golem.setHunger(golem.getHunger() + 1);
            counter = 0;
        }
        updateGolemSpeed();
    }

    /**
     * This method updates the Straw Golem's speed based on its hunger.
     */
    public void updateGolemSpeed() {
        // The casting probably could be simplified.
        // Using 3.0 to scale the speed, note that as hunger -> maxHunger the division should approach 1
        // Using 0.000001 as an epsilon, just to prevent any problems with imprecision.
        float epsilon = 0.000001f;
        float speedRatio = (float) Math.floor(epsilon + -3.0
                * (double) golem.getHunger() / Constants.Golem.maxHunger);
        // Flipping the order, so that the closer hunger is the maxHunger, the closer to 0.0f the speed becomes.
        speedRatio += 3.0f + epsilon;
        var attr = golem.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            // Normalizing speedRatio, since I don't want Golems moving triple speed.
            attr.setBaseValue(Constants.Golem.defaultMovement * Math.min(1.0f, speedRatio / 3.0f));
        } else {
            // Should never trigger, but best to be safe.
            Constants.LOG.error("Golem missing Attribute: {}!", "Movement Speed");
        }
    }

    public void refresh() {
        counter = 0;
        updateGolemSpeed();
    }
}
