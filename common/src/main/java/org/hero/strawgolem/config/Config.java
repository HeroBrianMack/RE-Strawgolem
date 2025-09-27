package org.hero.strawgolem.config;

public class Config {
    // Load config 'config.properties', if it isn't present create one
    // using the lambda specified as the provider.
    SimpleConfig CONFIG = SimpleConfig.of( "strawgolem" ).provider( this::provider ).request();
    String file = "";
    public Config() {
        section("My Config!\n");
        section("Golem Movement");
        add("Golem Walkspeed", 0.5);
        add("Golem Runspeed", 0.8);
    }
    // Custom config provider, returns the default config content
    // if the custom provider is not specified SimpleConfig will create an empty file instead
    private String provider( String filename ) {
        return file;
    }

    private void add(String key, Object value) {
        file += key + "=" + value + "\n";
    }
    private void add(String key, Object value, String description) {
        section(description);
        add(key, value);
    }
//    private void add(String key, int value) {
//        file += key + "=" + value + "\n";
//    }

    /**
     * Adds a comment or section to the config
     * @param comment The comment
     */
    private void section(String comment) {
        file += "# " + comment + "\n";
    }


    // And that's it! Now you can request values from the config:
    public final String SOME_STRING = CONFIG.getOrDefault( "key.of.the.value1", "default value" );
    public final int SOME_INTEGER = CONFIG.getOrDefault( "key.of.the.value2", 42 );
    public final boolean SOME_BOOL = CONFIG.getOrDefault( "key.of.the.value3", false );
}
