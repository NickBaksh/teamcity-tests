package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties");
        }
    }

    public static String getProperty(String key) {
        return INSTANCE.properties.getProperty(key);
    }

    public static String getTestEnvironment() {
        return getProperty("test.environment");
    }

    public static String getProperty(String key, String defaultValue) {
        String value = INSTANCE.properties.getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static String getApiVersion() {
        String version = getProperty("apiVersion");
        return parseApiVersion(version);
    }

    private static String parseApiVersion(String version) {
        if (version == null || version.isEmpty()) {
            throw new RuntimeException("api.version is not configured in config.properties!");
        }

        version = version.trim();

        if (version.contains("/")) {
            String[] parts = version.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (!parts[i].isEmpty()) {
                    version = parts[i];
                    break;
                }
            }
        }

        if (version.startsWith("api") && version.length() > 3) {
            version = version.substring(3);
        }

        if (!version.startsWith("v") && version.matches("\\d.*")) {
            version = "v" + version;
        }

        if (version.isEmpty()) {
            throw new RuntimeException(
                    "❌ Failed to parse api.version: '" +
                            getProperty("apiVersion") +
                            "' is invalid format"
            );
        }

        return version;
    }
}
