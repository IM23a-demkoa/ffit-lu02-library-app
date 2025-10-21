package ch.bzz;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties properties;

    /**
     * Lädt die Konfiguration aus config.properties (einmalig)
     * @return Properties-Objekt mit allen Konfigurationswerten
     */
    public static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Fehler beim Laden von config.properties", e);
            }
        }
        return properties;
    }

    /**
     * Optional: Hole einzelne Property direkt
     */
    public static String get(String key) {
        return getProperties().getProperty(key);
    }
}
