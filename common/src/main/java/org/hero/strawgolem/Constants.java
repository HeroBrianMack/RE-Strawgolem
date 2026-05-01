package org.hero.strawgolem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.hero.strawgolem.config.Config;
import org.hero.strawgolem.platform.services.IPlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

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
        public static final int maxHunger = CONFIG.getInt("Hunger Time");
        public static final Set<Item> foodItem = constructItemList(CONFIG.getString("Food Item"));
        public static final int maxLife = CONFIG.getInt("Lifespan Time");
        public static final Set<Item> repairItem = constructItemList(CONFIG.getString("Repair Item"));
        public static final boolean shiver = CONFIG.getBool("Shiver");
        public static final boolean dynamicDecay = CONFIG.getBool("Environmental Decay");

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
        public static Set<Block> whitelist = constructBlockList(CONFIG.getString("Crop Whitelist"));

        private static Set<Block> constructBlockList(String list) {
             Set<Block> set = new HashSet<>();
             String delim = list.contains(",") ? "," : " ";
             for (String str : list.split(delim)) {
                 ResourceLocation location = ResourceLocation.tryParse(str);
                 if (location == null) {
                     LOG.error("Resource location for {} not found!", str);
                 } else {
                     try {
                         if (BuiltInRegistries.ITEM.get(location) instanceof BlockItem block) {
                             set.add(block.getBlock());
                         } else if (BuiltInRegistries.BLOCK.get(location) != Blocks.AIR){
                             set.add(BuiltInRegistries.BLOCK.get(location));
                         }
                     } catch (Exception e) {
                         LOG.error("Unable to find the block for the resource location!");
                     }
                 }
             }
             return set;
        }

        private static Set<Item> constructItemList(String list) {
            Set<Item> set = new HashSet<>();
            String delim = list.contains(",") ? "," : " ";
            for (String str : list.split(delim)) {
                ResourceLocation location = ResourceLocation.tryParse(str);
                if (location == null) {
                    LOG.error("Resource location for {} not found!", str);
                } else {
                    try {
                        set.add(BuiltInRegistries.ITEM.get(location));
                    } catch (Exception e) {
                        LOG.error("Unable to find the item for the resource location!");
                    }
                }
            }
            return set;
        }
    }

    public static class Animation {
        public static final int TRANSITION_TIME = 4;
    }

}
