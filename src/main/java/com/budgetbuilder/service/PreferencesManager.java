package com.budgetbuilder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Properties;

public class PreferencesManager {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesManager.class);

    private static final String FILE_NAME = "budget-builder.properties";
    private static final String DEFAULT_DOWNLOAD_DIR_KEY = "default.download.directory";

    private final Properties properties = new Properties();
    private final Path preferencesPath;

    public PreferencesManager() {
        this.preferencesPath = resolvePreferencesPathNextToApp();
        loadPreferences();
    }

    /**
     * Írható preferences a JAR/EXE mappájában.
     */
    private Path resolvePreferencesPathNextToApp() {
        try {
            URL location = PreferencesManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            Path basePath = Paths.get(location.toURI());
            Path dir = Files.isDirectory(basePath) ? basePath : basePath.getParent();

            if (dir == null) {
                dir = Paths.get(".").toAbsolutePath().normalize();
            }

            return dir.resolve(FILE_NAME);
        } catch (URISyntaxException e) {
            Path fallback = Paths.get(".").toAbsolutePath().normalize().resolve(FILE_NAME);
            logger.warn("Nem sikerült meghatározni az app mappát, fallback: {}", fallback.toAbsolutePath());
            return fallback;
        }
    }

    private void loadPreferences() {
        // 1) Ha már van fájl a JAR/EXE mellett, azt olvassuk
        if (Files.exists(preferencesPath)) {
            try (InputStream in = Files.newInputStream(preferencesPath)) {
                properties.load(in);
                logger.info("Preferenciák betöltve fájlból: {}", preferencesPath.toAbsolutePath());
                applyFallbacks();
                return;
            } catch (IOException e) {
                logger.error("Hiba a preferenciák betöltésekor (fájl): {}", preferencesPath.toAbsolutePath(), e);
            }
        }

        // 2) Különben: default betöltése a JAR resources-ból
        boolean loadedFromResource = false;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(FILE_NAME)) {
            if (in != null) {
                properties.load(in);
                loadedFromResource = true;
                logger.info("Preferenciák betöltve resource-ból: {}", FILE_NAME);
            } else {
                logger.warn("Nem található resource: {}. Default beállítás kódból.", FILE_NAME);
                setDefaultsInMemory();
            }
        } catch (IOException e) {
            logger.error("Hiba a preferenciák betöltésekor (resource)", e);
            setDefaultsInMemory();
        }

        applyFallbacks();

        // 3) És mentjük a JAR/EXE mellé, hogy szerkeszthető legyen
        if (loadedFromResource) {
            logger.info("Default preferences kimásolása ide: {}", preferencesPath.toAbsolutePath());
        }
        savePreferences();
    }

    private void applyFallbacks() {
        String dir = properties.getProperty(DEFAULT_DOWNLOAD_DIR_KEY);
        if (dir == null || dir.isBlank()) {
            properties.setProperty(DEFAULT_DOWNLOAD_DIR_KEY, defaultDownloadsDir());
        }
    }

    private void setDefaultsInMemory() {
        properties.setProperty(DEFAULT_DOWNLOAD_DIR_KEY, defaultDownloadsDir());
    }

    private String defaultDownloadsDir() {
        return System.getProperty("user.home") + File.separator + "Downloads";
    }

    private void savePreferences() {
        try (OutputStream out = Files.newOutputStream(
                preferencesPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            properties.store(out, "Budget Builder Preferences");
            logger.info("Preferenciák mentve: {}", preferencesPath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Hiba a preferenciák mentésekor: {}", preferencesPath.toAbsolutePath(), e);
        }
    }

    public String getDownloadDirectory() {
        String dir = properties.getProperty(DEFAULT_DOWNLOAD_DIR_KEY);
        if (dir == null || dir.isBlank()) {
            dir = defaultDownloadsDir();
        }
        return dir;
    }

    public void setDownloadDirectory(String directory) {
        if (directory == null || directory.isBlank()) return;

        Path p = Paths.get(directory);
        if (Files.exists(p) && Files.isDirectory(p)) {
            properties.setProperty(DEFAULT_DOWNLOAD_DIR_KEY, directory);
            savePreferences();
            logger.info("Download könyvtár beállítva: {}", directory);
        } else {
            logger.warn("A megadott könyvtár nem létezik vagy nem könyvtár: {}", directory);
        }
    }

    public Properties getAllPreferences() {
        return properties;
    }

    public Path getPreferencesPath() {
        return preferencesPath;
    }
}