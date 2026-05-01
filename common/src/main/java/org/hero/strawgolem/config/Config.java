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

    public Config() {
        consConfig();
        if (!CONFIG.getKeys().containsAll(defaults.keySet())) {
            rebuildConfig();
        }
        // No need to have values in the rebuilder anymore, so just make it null.
        CONFIG_REBUILD = null;

        Constants.LOG.debug("{}", CONFIG.isBroken());
    }

    private void consConfig() {
        Constants.LOG.error("MZEY");
        defaults = new HashMap<>();
        section("Strawgolem Config");
        CONFIG_REBUILD.add(() -> file += "\n");
        file += "\n";
        golemHealthSection();
        golemMovementSection();
        golemHarvestingSection();
        CONFIG = SimpleConfig.of("strawgolem").provider(this::provider)
                .request();
    }

    /**
     * Method that handles the Golem Health Section of the Config.
     */
    private void golemHealthSection() {
        section("Golem Health");
        add("Max Health", 6f, "The max health of a golem.");
        add("Barrel Max Health", 100, "The max health of a barrel.");
        add("Golem Hunger Time", 1200, "The time in seconds " +
                "it takes for a Straw Golem to become fully hungry.");
        add("Golem Lifespan Time", 1200, "The time in seconds " +
                "that a Straw Golem will have as a natural lifespan.");
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
        add("Panic When Hurt", true,
                "Whether a golem should panic when hurt.");
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

    private void rebuildConfig() {
        Constants.LOG.error("MZES");
        file = "";
        CONFIG_REBUILD.forEach(Runnable::run);
        CONFIG = SimpleConfig.of("strawgolem").provider(this::provider)
                .request(true);
    }

    // if the custom provider is not specified SimpleConfig will create an empty file instead
    private String provider() {
        // Custom config provider, returns the default config content
        return file;
    }

    // Special variation just for rebuilding config.
    private void add(String key, String description) {
        description(description);
        if (Objects.equals(key, "Golem Hunger Time")) {
            System.out.println(getObject(key));
            System.out.println(defaults.keySet());
        }
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
            return Integer.parseInt((String) getObject(key));
        } catch (Throwable e) {
            Constants.LOG.error(e.getMessage());
            try {
                return Integer.parseInt((String) defaults.get(key));
            } catch (Throwable q) {
                Constants.LOG.error(q.getMessage());
            }
        }
        return 1;
    }

    public boolean getBool(String key) {
        try {
            return Boolean.parseBoolean((String) getObject(key));
        } catch (Throwable e) {
            Constants.LOG.error(e.getMessage());
            try {
                return Boolean.parseBoolean((String) defaults.get(key));
            } catch (Throwable q) {
                Constants.LOG.error(q.getMessage());
            }
        }
        return false;
    }

    public String getString(String key) {
        try {
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
