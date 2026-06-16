package com.mybharat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ConfigReader - Environment-specific configuration loader for the test framework.
 *
 * Purpose: Loads properties from config-{env}.properties files located in
 *          src/test/resources/. Provides typed accessors for common properties
 *          (URL, browser, password, dummy email URL).
 *
 * Configuration Files:
 *   - config-beta.properties → Beta environment (https://yuva-beta.mybharats.in)
 *   - config-prod.properties → Production environment (https://mybharat.gov.in)
 *
 * Key Methods:
 *   - getProperty(key)   — returns any property value by key
 *   - getEnv()           — returns current environment name (beta/prod)
 *   - getUrl()           — returns the base application URL
 *   - getDummyEmailUrl() — returns the Yopmail URL for OTP retrieval
 *   - getPassword()      — returns the default test password
 *
 * Usage:
 *   ConfigReader config = new ConfigReader(); // reads based on -Denv system property
 *   String url = config.getUrl();
 *
 * Override at runtime: mvn test -Denv=prod
 * Default environment: beta
 *
 * Dependencies: Java Properties, Log4j2
 * Developer: Nishant Sharma (QA Team)
 *
 * @see BaseTest
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
