package org.hero.strawgolem;

import org.hero.strawgolem.config.Config;
import org.hero.strawgolem.platform.services.IPlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Constants {
    public static final String MODID = "strawgolem";
    public static final String MOD_NAME = "Straw Golem";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final IPlatformHelper COMMON_PLATFORM = ServiceLoader.load(IPlatformHelper.class).findFirst().orElseThrow();
    public static final Config CONFIG = new Config();
    public static class Golem {
        // Health
        public static final float maxHealth = CONFIG.getFloat("Max Health");
        public static final int barrelHealth = CONFIG.getInt("Barrel Max Health");
        // Movement
        public static final double defaultMovement = 0.23;
        public static final double defaultWalkSpeed = CONFIG.getDouble("Walk Speed");
        public static final double defaultRunSpeed = CONFIG.getDouble("Run Speed");
        public static final int wanderRange = CONFIG.getInt("Wander Range");;
        public static final boolean panic = CONFIG.getBool("Panic When Hurt");
        // Harvesting
        public static final int searchRange = CONFIG.getInt("Harvest Range");
        public static final int searchRangeVertical = 3;
        public static final double depositDistance = 1.5;
        public static boolean blockHarvest = CONFIG.getBool("Block Harvesting");
        public static boolean whitelistHarvest = CONFIG.getBool("Use Whitelist");
        public static String whitelist = CONFIG.getString("Crop Whitelist");
    }

    public static class Animation {
        public static final int TRANSITION_TIME = 4;
    }

}
