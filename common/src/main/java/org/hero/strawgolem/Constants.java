package org.hero.strawgolem;

import org.hero.strawgolem.platform.services.IPlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Constants {

    public static final String MODID = "strawgolem";
    public static final String MOD_NAME = "Straw Golem";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final IPlatformHelper COMMON_PLATFORM = ServiceLoader.load(IPlatformHelper.class).findFirst().orElseThrow();
    public static class Golem {
        public static final double defaultMovement = 0.23;
        public static final double defaultWalkSpeed = 0.5;
        public static final double defaultRunSpeed = 0.8;
        public static final int searchRange = 16;
        public static final int searchRangeVertical = 3;
        public static final float baseHealth = 6;
        public static final double depositDistance = 1.5;
        public static final int barrelHealth = 100;
    }

    public static class Animation {
        public static final int TRANSITION_TIME = 4;
    }

}
