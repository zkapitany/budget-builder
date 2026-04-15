package com.budgetbuilder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Properties;

public class PreferencesManager {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesManager.class);

    private static final String FILE_NAME = "Preferencies.txt";
    private static final String DEFAULT_EXPORT_DIR_KEY = "default.export.directory";
    private static final String DEFAULT_TEMPLATE_DIR_KEY = "default.template.directory";

    private final Properties properties = new Properties();
    private final Path appDirectory;
    private final Path preferencesPath;

    public PreferencesManager() {
        this.appDirectory = resolveAppDirectory();
        this.preferencesPath = appDirectory.resolve(FILE_NAME);
        loadPreferences();
    }

    private Path resolveAppDirectory() {
        String launcherPath = System.getProperty("jpackage.app-path");
        if (launcherPath != null && !launcherPath.isBlank()) {
            try {
                Path launcher = Paths.get(launcherPath).toAbsolutePath().normalize();
                Path parent = launcher.getParent();
                if (parent != null) {
                    logger.info("App mappa (jpackage.app-path alapján): {}", parent);
                    return parent;
                }
            } catch (Exception e) {
                logger.warn("Hibás jpackage.app-path érték: {}", launcherPath, e);
            }
        }

        try {
            URL location = PreferencesManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            Path basePath = Paths.get(location.toURI());
            Path dir = Files.isDirectory(basePath) ? basePath : basePath.getParent();

            if (dir == null) {
                dir = Paths.get(".").toAbsolutePath().normalize();
            }

            logger.info("App mappa (code source alapján): {}", dir);
            return dir;
        } catch (URISyntaxException e) {
            Path fallback = Paths.get(".").toAbsolutePath().normalize();
            logger.warn("Nem sikerült meghatározni az app mappát, fallback: {}", fallback.toAbsolutePath());
            return fallback;
        }
    }

    private void loadPreferences() {
        if (Files.exists(preferencesPath)) {
            try (var in = Files.newInputStream(preferencesPath)) {
                properties.load(in);
                logger.info("Preferenciák betöltve fájlból: {}", preferencesPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Hiba a preferenciák betöltésekor (fájl): {}", preferencesPath.toAbsolutePath(), e);
            }
        }

        applyFallbacks();
        savePreferences();
    }

    private void applyFallbacks() {
        String exportDir = properties.getProperty(DEFAULT_EXPORT_DIR_KEY);
        if (exportDir == null || exportDir.isBlank()) {
            properties.setProperty(DEFAULT_EXPORT_DIR_KEY, defaultExportDir());
        } else {
            Path configured = Paths.get(exportDir);
            if (!Files.isDirectory(configured)) {
                logger.warn("A beállított export könyvtár nem létezik: {}. Fallback használva.", exportDir);
                properties.setProperty(DEFAULT_EXPORT_DIR_KEY, defaultExportDir());
            }
        }

        String templateDir = properties.getProperty(DEFAULT_TEMPLATE_DIR_KEY);
        if (templateDir == null || templateDir.isBlank()) {
            properties.setProperty(DEFAULT_TEMPLATE_DIR_KEY, ensureDefaultTemplateDir());
        } else {
            Path configured = Paths.get(templateDir);
            if (!Files.isDirectory(configured)) {
                logger.warn("A beállított template könyvtár nem létezik: {}. Fallback használva.", templateDir);
                properties.setProperty(DEFAULT_TEMPLATE_DIR_KEY, ensureDefaultTemplateDir());
            }
        }
    }

    private String defaultExportDir() {
        return System.getProperty("user.home");
    }

    private void savePreferences() {
        Path parent = preferencesPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                logger.error("Nem sikerült létrehozni a preferencia mappa útvonalát: {}", parent, e);
            }
        }

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

    public String getExportDirectory() {
        String dir = properties.getProperty(DEFAULT_EXPORT_DIR_KEY);
        if (dir == null || dir.isBlank()) {
            dir = defaultExportDir();
        }
        return dir;
    }

    public void setExportDirectory(String directory) {
        if (directory == null || directory.isBlank()) return;

        Path p = Paths.get(directory);
        if (Files.exists(p) && Files.isDirectory(p)) {
            properties.setProperty(DEFAULT_EXPORT_DIR_KEY, directory);
            savePreferences();
            logger.info("Export könyvtár beállítva: {}", directory);
        } else {
            logger.warn("A megadott könyvtár nem létezik vagy nem könyvtár: {}", directory);
        }
    }

    public String getTemplateDirectory() {
        String dir = properties.getProperty(DEFAULT_TEMPLATE_DIR_KEY);
        if (dir == null || dir.isBlank()) {
            dir = ensureDefaultTemplateDir();
            properties.setProperty(DEFAULT_TEMPLATE_DIR_KEY, dir);
            savePreferences();
        }
        return dir;
    }

    public void setTemplateDirectory(String directory) {
        if (directory == null || directory.isBlank()) return;

        Path p = Paths.get(directory);
        if (Files.exists(p) && Files.isDirectory(p)) {
            properties.setProperty(DEFAULT_TEMPLATE_DIR_KEY, directory);
            savePreferences();
            logger.info("Template könyvtár beállítva: {}", directory);
        } else {
            logger.warn("A megadott template könyvtár nem létezik vagy nem könyvtár: {}", directory);
        }
    }

    public String getDownloadDirectory() {
        return getExportDirectory();
    }

    public void setDownloadDirectory(String directory) {
        setExportDirectory(directory);
    }

    private String ensureDefaultTemplateDir() {
        Path defaultTemplateDir = appDirectory.resolve("template");
        try {
            Files.createDirectories(defaultTemplateDir);
        } catch (IOException e) {
            logger.error("Nem sikerült létrehozni a default template könyvtárat: {}", defaultTemplateDir, e);
        }
        return defaultTemplateDir.toString();
    }

    public Properties getAllPreferences() {
        return properties;
    }

    public Path getPreferencesPath() {
        return preferencesPath;
    }
}
