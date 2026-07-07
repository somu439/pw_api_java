package one.two.three.support;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {

    private static final Properties props = loadProperties();

    public static final String BASE_URL = resolve("BASE_URL", "https://google.com");
    public static final String JWT_TOKEN = resolve("JWT_TOKEN", "");

    public static final Map<String, String> HEADERS = buildHeaders();

    private static Properties loadProperties() {
        String env = System.getProperty("env", "dev");
        String path = "env/" + env + ".properties";
        Properties p = new Properties();
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                p.load(is);
            } else {
                System.err.println("[Config] Property file not found on classpath: " + path + " — using defaults");
            }
        } catch (Exception e) {
            System.err.println("[Config] Failed to load " + path + ": " + e.getMessage());
        }
        return p;
    }

    private static String resolve(String key, String defaultValue) {
        String envVar = System.getenv(key);
        if (envVar != null && !envVar.isEmpty()) return envVar;
        String prop = props.getProperty(key);
        if (prop != null && !prop.isEmpty()) return prop;
        return defaultValue;
    }

    private static Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        if (!JWT_TOKEN.isEmpty()) {
            headers.put("Authorization", "Bearer " + JWT_TOKEN);
        }
        return headers;
    }
}
