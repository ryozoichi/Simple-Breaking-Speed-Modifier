package net.sievert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Config class for Simple Breaking Speed Modifier.
 * Handles loading, validating, and describing the mod's configuration.
 */
public class Config {

    /** Path to the JSON config file on disk. */
    public static final Path CONFIG_PATH = Path.of("config/simple_breaking_speed_modifier.json");

    /** Gson instance with pretty-printing for JSON (de)serialization. */
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Logger for config-related messages. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBreakingSpeedModifier.MOD_ID);

    /**
     * Player mining speed modifier.
     * Default value: 1.0 (vanilla speed).
     */
    @SerializedName("Player Block Break Speed Base Modifier")
    public double playerBlockBreakSpeedBase = 1.0;

    /**
     * Enables debug logging when true.
     * Default value: false.
     */
    @SerializedName("Enable Debug Logging")
    public boolean enableDebugLogging = false;

    /** Singleton instance for use throughout the mod. */
    public static Config INSTANCE = new Config();

    /**
     * Loads config from disk. If file does not exist, writes the default config.
     * If any required fields are missing, logs an error and uses the default config.
     * Never overwrites existing user configs except on first run.
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                // Read the config file as JSON text
                String json = Files.readString(CONFIG_PATH);
                // Deserialize JSON to config object
                Config loaded = GSON.fromJson(json, Config.class);

                // Check for required fields
                List<String> missing = getMissingFields(json);
                if (!missing.isEmpty()) {
                    LOGGER.error("Config is missing required field(s): {}. Using default value. Edit or delete the file.", String.join(", ", missing));
                    INSTANCE = new Config();
                } else {
                    INSTANCE = loaded;
                    LOGGER.info(
                            "Loaded config: Player Block Break Speed Base Modifier = {} ({})",
                            INSTANCE.playerBlockBreakSpeedBase,
                            describeSpeed(INSTANCE.playerBlockBreakSpeedBase)
                    );
                }
            } catch (JsonSyntaxException e) {
                LOGGER.error("Config file is malformed JSON. Using default value. Edit or delete the file.");
            } catch (IOException e) {
                LOGGER.error("Failed to read config file (IO error). Using default value.");
            } catch (Exception e) {
                LOGGER.error("Failed to load config due to unexpected error. Using default value.");
            }
        } else {
            // Config file missing: create default file
            saveDefault();
            LOGGER.info("Created default config!");
        }
    }

    /**
     * Checks for required fields in the JSON and returns a list of missing fields.
     * Extend this method if you add more required fields.
     *
     * @param json The JSON string of the config file.
     * @return A list of missing field names.
     */
    private static List<String> getMissingFields(String json) {
        List<String> missing = new ArrayList<>();
        if (!json.contains("\"Player Block Break Speed Base Modifier\"")) {
            missing.add("\"Player Block Break Speed Base Modifier\"");
        }
        if (!json.contains("\"Enable Debug Logging\"")) {
            missing.add("\"Enable Debug Logging\"");
        }
        return missing;
    }

    /**
     * Writes the current config to disk ONLY if the file is missing.
     * Never overwrites existing user configs.
     */
    private static void saveDefault() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LOGGER.error("Failed to create default config.");
        }
    }

    /**
     * Describes how much faster or slower the mining speed is compared to default (1.0).
     * For example:
     *   1.25 -> "25% faster than default"
     *   0.75 -> "25% slower than default"
     *   1.0  -> "default value"
     *
     * @param value The configured mining speed multiplier.
     * @return A human-readable description.
     */
    private static String describeSpeed(double value) {
        if (value == 1.0) {
            return "default value";
        }
        double percent = Math.abs((value - 1.0) * 100.0);
        if (value > 1.0) {
            return String.format("%.0f%% faster than default", percent);
        } else {
            return String.format("%.0f%% slower than default", percent);
        }
    }
}
