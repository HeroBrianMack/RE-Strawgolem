package org.hero.strawgolem.config;

import org.hero.strawgolem.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Config {
    // Load config 'config.properties', if it isn't present create one
    // using the lambda specified as the provider.
    private SimpleConfig CONFIG;
    private String file = "";
    private Map<String, Object> defaults;
    private ArrayList<Runnable> CONFIG_REBUILD = new ArrayList<>();
    // There could be a way to use TreeMap here and make things more efficient, but this section only runs once.
    private HashMap<Integer, Map<String, Object>> versionOverrides = new HashMap<>();
    private boolean newConfigVersion = false;

    public Config() {
        consConfig();
        setVersionOverrides();
        // Whether there is an update to the config version (mandatory config overrides essentially).
        newConfigVersion = versionOverrides.keySet().stream().anyMatch(v -> v > versionParse());
        // If there is a missing config or a config update, rebuild the config.
        if (!CONFIG.getKeys().containsAll(defaults.keySet()) || newConfigVersion) {
            rebuildConfig();
        }

        // No need to have values in the rebuilder/overrider anymore, so just make it null.
        CONFIG_REBUILD = null;
        versionOverrides = null;
        if (CONFIG.isBroken()) {
            Constants.LOG.error("Configuration file is broken!");
        }
    }

    private void setVersionOverrides() {
        // Honestly could simplify this logically to not even bother keeping old overrides,
        // but just in case users jump up from old versions, I will keep this as such.
        // Note that this map is Immutable, so do not try to modify the map!
        // Version 1 Overrides:
        versionOverrides.put(1,
                Map.of("Config Version Number", "1"));
        // Version X Overrides:
//        versionOverrides.put(X,
//                Map.of("Config Version Number", "X"));
    }

    private void consConfig() {
        defaults = new HashMap<>();
        section("Strawgolem Config");
        CONFIG_REBUILD.add(() -> file += "\n");
        file += "\n";
        // The sections of the config.
        golemHealthSection();
        golemMovementSection();
        golemHarvestingSection();
        miscSection();
        metaSection();
        CONFIG = SimpleConfig.of("strawgolem").provider(this::provider)
                .request();
    }

    private void rebuildConfig() {
        file = "";
        CONFIG_REBUILD.forEach(Runnable::run);
        CONFIG = SimpleConfig.of("strawgolem").provider(this::provider)
                .request(true);
    }

    /**
     * Method that handles the Golem Health Section of the Config.
     */
    private void golemHealthSection() {
        section("Golem Health");
        add("Max Health", 6f, "The max health of a golem.");
        add("Barrel Max Health", 100, "The max health of a barrel.");
        add("Hunger", false, "Whether a golem should have hunger.");
        add("Hunger Time", 4800, "The time in seconds " +
                "it takes for a Straw Golem to become fully hungry.");
        add("Food Item", "minecraft:apple", "The item(s) a golem is fed with.");
        add("Lifespan", false, "Whether a golem should have a lifespan.");
        add("Lifespan Time", 4800, "The time in seconds " +
                "that a Straw Golem will have as a natural lifespan in a default environment.");
        add("Lifespan Variation", true, "Whether golems should have variation " +
                "in their lifespans.");
        add("Environmental Decay", false, "Whether a golem should age faster or slower" +
                " in response to environmental factors, examples being:\n# A golem in rain or water will age faster, " +
                "whereas a golem in cold biomes will age slower.");
        add("Repair Item", "minecraft:wheat", "The item(s) a golem is repaired with.");
        add("Shiver", true, "Whether a golem should shiver in response to environmental factors " +
                "such as rain and cold biomes.");
    }

    /**
     * Method that handles the Golem Movement Section of the Config.
     */
    private void golemMovementSection() {
        section("Golem Movement");
        add("Walk Speed", 0.5,
                "The walk speed of a golem.");
        add("Run Speed", 0.8,
                "The run speed of a golem.");
        add("Wander Range", 24, "How far a golem can wander");
        add("Panic", true,
                "Whether a golem should panic when threatened.");
        add("Flee Range", 15.0f, "How far a golem can flee");
    }

    /**
     * Method that handles the Golem Harvesting Section of the Config.
     */
    private void golemHarvestingSection() {
        section("Golem Harvesting");
        add("Harvest Range", 24,
                "Range for a golem to detect crops and chests.");
        add("Block Harvesting", true,
                "Whether a golem should harvest crop blocks"
                        + " like pumpkins and melons.");
        add("Use Whitelist", false,
                "Whether a golem should only harvest crops in the whitelist.");
        add("Crop Whitelist", " ",
                "What crops should be harvested,"
                        + " please use valid resource locations.");
    }

    /**
     * Method that handles the MetaData Section of the Config.
     */
    private void miscSection() {
        section("Miscellaneous");
        add("Hungry Animals", true, "Whether animals should try to attack Straw Golems");
        add("Angry Pillagers", true, "Whether pillagers should try to attack Straw Golems");
        add("Winter Skin", true, "Whether the Straw Golem winter skin will be shown. The skin" +
                " is still only attainable during the Winter.");
        add("Hemisphere", "North", "For determining season-based timings. Acceptable values are:" +
                " North, South, None.\n# Note: None will disable seasonal events.");
    }

    /**
     * Method that handles the Metadata Section of the Config.
     */
    private void metaSection() {
        section("Metadata");
        add("Config Version Number", "1",
                "Please do not modify this value casually, or risk config values being overwritten " +
                        "or made invalid!");
    }

    private int versionParse() {
        // Get current version
        String version = CONFIG.get("Config Version Number");
        if (version == null) {
            // Returning 0 as a way of saying this is an unversioned config
            return 0;
        } else {
            try {
                return Integer.parseInt(version);
            } catch (Exception e) {
                Constants.LOG.error("Error parsing config version number : {}", version);
            }
        }
        // Currently force resets if version num broken. No different from 0 effectively.
        // May use as a checker to disable resets?
        return -1;
    }

    private int updateVersion(String key) {
        // Grab current version.
        var currentVersion = versionParse();
        // Variable just to track the newest version that overrides the key.
        int newestVersion = -1;
        // Get all override versions.
        for (var k : versionOverrides.keySet()) {
            // Now check if the overrides for the version have the key (the config variable).
            if (versionOverrides.get(k).containsKey(key)) {
                // Check that the version is greater than the current version then against
                // newestVersion to locate max for the key.
                if (k > currentVersion && k > newestVersion) {
                    newestVersion = k;
                }
            }
        }
        return newestVersion;
    }

    // if the custom provider is not specified SimpleConfig will create an empty file instead
    private String provider() {
        // Custom config provider, returns the default config content
        return file;
    }

    // Special variation just for rebuilding config.
    private void add(String key, String description) {
        description(description);
        // If the config version has updated.
        if (newConfigVersion) {
            // Get the newest update version for this key
            var update = updateVersion(key);
            if (update != -1) {
                add(key, versionOverrides.get(update).get(key));
                return;
            }
        }
        // If there is a new config, but it isn't for this key continue rebuilding as usual.
        add(key, getObject(key));
    }

    private void cheapAdd(String key, Object value) {
        file += key + "=" + value + "\n";
    }

    private void add(String key, Object value) {
        file += key + "=" + value + "\n";
        defaults.put(key, value);
    }

    private void add(String key, Object value, String description) {
        CONFIG_REBUILD.add(() -> add(key, description));
        description(description);
        add(key, value);
    }
    /**
     * Adds a comment or section to the config
     *
     * @param section The section
     */
    private void section(String section) {
        CONFIG_REBUILD.add(() -> safeSection(section));
        file += "# [" + section + "]\n";
    }

    private void safeSection(String section) {
        file += "# [" + section + "]\n";
    }

    /**
     * Adds a comment to the config
     *
     * @param comment The comment
     */
    private void description(String comment) {
        file += "# " + comment + "\n";
    }

    // These methods are unlikely to be used
    int getOrDefault(String key, Integer defaultValue) {
        return CONFIG.getOrDefault(key, defaultValue);
    }

    boolean getOrDefault(String key, Boolean defaultValue) {
        return CONFIG.getOrDefault(key, defaultValue);
    }

    String getOrDefault(String key, String defaultValue) {
        return CONFIG.getOrDefault(key, defaultValue);
    }

    double getOrDefault(String key, Double defaultValue) {
        return CONFIG.getOrDefault(key, defaultValue);
    }

    public Object getObject(String key) {
        Object defaultVal = defaults.get(key);
        if (CONFIG.get(key) == null) {
            return defaultVal;
        }
        return CONFIG.get(key);
    }

    public double getDouble(String key) {
        try {
            if (getObject(key) == null) {
                return 1.0;
            }
            return Double.parseDouble((String) getObject(key));
        } catch (Throwable e) {
            Constants.LOG.error(e.getMessage());
            try {
                return Double.parseDouble((String) defaults.get(key));
            } catch (Throwable q) {
                Constants.LOG.error(q.getMessage());

            }
        }
        return 1.0;
    }

    public float getFloat(String key) {
        try {
            if (getObject(key) == null) {
                return 1.0f;
            }
            return Float.parseFloat((String) getObject(key));
        } catch (Throwable e) {
            Constants.LOG.error(e.getMessage());
            try {
                return Float.parseFloat((String) defaults.get(key));
            } catch (Throwable q) {
                Constants.LOG.error(q.getMessage());

            }
        }
        return 1.0f;
    }

    public int getInt(String key) {
        try {
            if (getObject(key) == null) {
                return 1;
            }
            return Integer.parseInt((String) getObject(key));
        } catch (Throwable e) {
            Constants.LOG.error(key + "  " + e.getMessage());
            try {
                return Integer.parseInt((String) defaults.get(key));
            } catch (Throwable q) {
                Constants.LOG.error(key + " " + q.getMessage());
            }
        }
        return 1;
    }

    public boolean getBool(String key) {
        try {
            if (getObject(key) == null) {
                return false;
            }
            return Boolean.parseBoolean((String) getObject(key));
        } catch (Throwable e) {
            Constants.LOG.error(key +  " " + e.getMessage());
            try {
                return Boolean.parseBoolean((String) defaults.get(key));
            } catch (Throwable q) {
                Constants.LOG.error(key+ " " +q.getMessage());
            }
        }
        return false;
    }

    public String getString(String key) {
        try {
            if (getObject(key) == null) {
                return "";
            }
            return (String) getObject(key);
        } catch (Throwable e) { // This catch should never be possible!
            Constants.LOG.error(e.getMessage());
            try {
                return (String) defaults.get(key);
            } catch (Throwable q) {
                Constants.LOG.error(q.getMessage());

            }
        }
        return "";
    }
}
