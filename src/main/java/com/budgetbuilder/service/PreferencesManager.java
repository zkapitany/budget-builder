package com.budgetbuilder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PreferencesManager {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesManager.class);
    private static final String PREFERENCES_FILE = "budget-builder.properties";
    private static final String DEFAULT_DOWNLOAD_DIR_KEY = "default.download.directory";
    
    private Properties properties;

    public PreferencesManager() {
        properties = new Properties();
        loadPreferences();
    }

    /**
     * Preferenciák betöltése a fájlból
     */
    private void loadPreferences() {
        File prefFile = new File(PREFERENCES_FILE);
        if (prefFile.exists()) {
            try (FileInputStream fis = new FileInputStream(prefFile)) {
                properties.load(fis);
                logger.info("Preferenciák betöltve: {}", PREFERENCES_FILE);
            } catch (IOException e) {
                logger.error("Hiba a preferenciák betöltésekor", e);
            }
        } else {
            // Alapértelmezett érték beállítása
            String defaultDir = System.getProperty("user.home") + File.separator + "Downloads";
            properties.setProperty(DEFAULT_DOWNLOAD_DIR_KEY, defaultDir);
            savePreferences();
            logger.info("Alapértelmezett preferenciák létrehozva");
        }
    }

    /**
     * Preferenciák mentése a fájlba
     */
    private void savePreferences() {
        try (FileOutputStream fos = new FileOutputStream(PREFERENCES_FILE)) {
            properties.store(fos, "Budget Builder Preferences");
            logger.info("Preferenciák mentve: {}", PREFERENCES_FILE);
        } catch (IOException e) {
            logger.error("Hiba a preferenciák mentésekor", e);
        }
    }

    /**
     * Download könyvtár lekérése
     */
    public String getDownloadDirectory() {
        String dir = properties.getProperty(DEFAULT_DOWNLOAD_DIR_KEY);
        if (dir == null || dir.isEmpty()) {
            dir = System.getProperty("user.home") + File.separator + "Downloads";
        }
        return dir;
    }

    /**
     * Download könyvtár beállítása
     */
    public void setDownloadDirectory(String directory) {
        if (directory != null && !directory.isEmpty()) {
            if (Files.exists(Paths.get(directory))) {
                properties.setProperty(DEFAULT_DOWNLOAD_DIR_KEY, directory);
                savePreferences();
                logger.info("Download könyvtár beállítva: {}", directory);
            } else {
                logger.warn("A megadott könyvtár nem létezik: {}", directory);
            }
        }
    }

    /**
     * Összes preferencia lekérése
     */
    public Properties getAllPreferences() {
        return properties;
    }
}