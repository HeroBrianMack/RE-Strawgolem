package org.hero.strawgolem.config;

import org.hero.strawgolem.Constants;

import java.util.HashMap;
import java.util.Map;

public class Config {
    // Load config 'config.properties', if it isn't present create one
    // using the lambda specified as the provider.
    private SimpleConfig CONFIG;
    private String file = "";
    private Map<String, Object> defaults;

    public Config() {
        defaults = new HashMap<>();
        section("Strawgolem Config");
        file += "\n";
        section("Golem Health");
        add("Max Health", 6f, "The max health of a golem.");
        add("Barrel Max Health", 100, "The max health of a barrel.");
        section("Golem Movement");
        add("Walk Speed", 0.5,
                "The walk speed of a golem.");
        add("Run Speed", 0.8,
                "The run speed of a golem.");
        add("Wander Range", 24, "How far a golem can wander");
        add("Panic When Hurt", true,
                "Whether a golem should panic when hurt.");
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
        CONFIG = SimpleConfig.of("strawgolem").provider(this::provider)
                .request();
        System.out.println(CONFIG.isBroken());
    }

    // if the custom provider is not specified SimpleConfig will create an empty file instead
    private String provider() {
        // Custom config provider, returns the default config content
        return file;
    }

    private void add(String key, Object value) {
        file += key + "=" + value + "\n";
        defaults.put(key, value);
    }

    private void add(String key, Object value, String description) {
        description(description);
        add(key, value);
    }
    /**
     * Adds a comment or section to the config
     *
     * @param section The section
     */
    private void section(String section) {
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
