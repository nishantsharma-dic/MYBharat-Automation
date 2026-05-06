package com.mybharat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ConfigReader - Loads environment-specific properties.
 * 
 * Reads from: src/test/resources/config-{env}.properties
 * Default env: beta
 * 
 * Override at runtime: mvn test -Denv=prod
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private final Properties properties = new Properties();
    private final String env;

    public ConfigReader() {
        this.env = System.getProperty("env", "beta");
        String fileName = "config-" + env + ".properties";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                properties.load(input);
                log.info("Loaded config: {}", fileName);
            } else {
                log.error("Config file not found: {}", fileName);
                throw new RuntimeException("Config file not found: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + fileName, e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getEnv() {
        return env;
    }

    public String getUrl() {
        return properties.getProperty("url");
    }

    public String getDummyEmailUrl() {
        return properties.getProperty("dummyEmail");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }
}
