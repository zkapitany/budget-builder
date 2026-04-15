package com.budgetbuilder.controller;

import com.budgetbuilder.model.Material;
import com.budgetbuilder.model.WorkItem;
import com.budgetbuilder.service.BudgetManager;
import com.budgetbuilder.service.ExcelService;
import com.budgetbuilder.service.PreferencesManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private BorderPane mainPane;
    @FXML private TableView<Material> materialTable;
    @FXML private TableView<WorkItem> workItemTable;
    @FXML private Button newItemBtn;
    @FXML private Button exportBtn;
    @FXML private Button consolidateBtn;
    @FXML private Button selectTemplateBtn;
    @FXML private Button settingsBtn;
    @FXML private Label templatePathLabel;

    private BudgetManager budgetManager;
    private ExcelService excelService;
    private PreferencesManager preferencesManager;
    private String templatePath;

    @FXML
    public void initialize() {
        excelService = new ExcelService();
        preferencesManager = new PreferencesManager();
        setupTableColumns();
        setupButtons();

        loadTemplateFromPreferences();

        logger.info("MainController inicializálva");
    }

    private void setupTableColumns() {
        // ========== ANYAGOK TÁBLÁZAT ==========
        TableColumn<Material, String> matIdCol = new TableColumn<>("Anyag_ID");
        matIdCol.setCellValueFactory(new PropertyValueFactory<>("materialId"));
        matIdCol.setPrefWidth(80);

        TableColumn<Material, String> elmukCol = new TableColumn<>("ELMŰ Cikkszám");
        elmukCol.setCellValueFactory(new PropertyValueFactory<>("elmukCikkszam"));
        elmukCol.setPrefWidth(120);

        TableColumn<Material, String> eonCol = new TableColumn<>("EON Cikkszám");
        eonCol.setCellValueFactory(new PropertyValueFactory<>("eonCikkszam"));
        eonCol.setPrefWidth(120);

        TableColumn<Material, String> megnevCol = new TableColumn<>("Megnevezese");
        megnevCol.setCellValueFactory(new PropertyValueFactory<>("megnevezese"));
        megnevCol.setPrefWidth(200);

        TableColumn<Material, Double> mennyCol = new TableColumn<>("Tervezett Mennyiseg");
        mennyCol.setCellValueFactory(new PropertyValueFactory<>("tervezettMennyiseg"));
        mennyCol.setPrefWidth(100);

        TableColumn<Material, String> metrCol = new TableColumn<>("M.e.");
        metrCol.setCellValueFactory(new PropertyValueFactory<>("mertkegyseg"));
        metrCol.setPrefWidth(80);

        TableColumn<Material, String> megjeCol = new TableColumn<>("Megjegyzese");
        megjeCol.setCellValueFactory(new PropertyValueFactory<>("megjegyzese"));
        megjeCol.setPrefWidth(100);

        materialTable.getColumns().addAll(matIdCol, elmukCol, eonCol, megnevCol, mennyCol, metrCol, megjeCol);

        // ========== MUNKATÉTELEK TÁBLÁZAT ==========
        TableColumn<WorkItem, String> workIdCol = new TableColumn<>("Munka_ID");
        workIdCol.setCellValueFactory(new PropertyValueFactory<>("workId"));
        workIdCol.setPrefWidth(80);

        TableColumn<WorkItem, String> workEonCol = new TableColumn<>("EON Cikkszám");
        workEonCol.setCellValueFactory(new PropertyValueFactory<>("eonCikkszam"));
        workEonCol.setPrefWidth(120);

        TableColumn<WorkItem, String> bszjCol = new TableColumn<>("BSZJ Azonosito");
        bszjCol.setCellValueFactory(new PropertyValueFactory<>("bszjAzonosito"));
        bszjCol.setPrefWidth(100);

        TableColumn<WorkItem, String> elszamolaCol = new TableColumn<>("Elszamolasi Tetel");
        elszamolaCol.setCellValueFactory(new PropertyValueFactory<>("elszamolaTetelmegnevezese"));
        elszamolaCol.setPrefWidth(250);

        TableColumn<WorkItem, Double> workMennyCol = new TableColumn<>("Tervezett Mennyiseg");
        workMennyCol.setCellValueFactory(new PropertyValueFactory<>("tervezettMennyiseg"));
        workMennyCol.setPrefWidth(100);

        TableColumn<WorkItem, String> workMeCol = new TableColumn<>("ME");
        workMeCol.setCellValueFactory(new PropertyValueFactory<>("mertkegyseg"));
        workMeCol.setPrefWidth(80);

        TableColumn<WorkItem, String> workMegjeCol = new TableColumn<>("Megjegyzese");
        workMegjeCol.setCellValueFactory(new PropertyValueFactory<>("megjegyzese"));
        workMegjeCol.setPrefWidth(100);

        workItemTable.getColumns().addAll(workIdCol, workEonCol, bszjCol, elszamolaCol, workMennyCol, workMeCol, workMegjeCol);
    }

    private void setupButtons() {
        selectTemplateBtn.setOnAction(e -> selectTemplateFile());
        newItemBtn.setOnAction(e -> showNewItemDialog());
        exportBtn.setOnAction(e -> exportBudget());
        consolidateBtn.setOnAction(e -> consolidateData());
        settingsBtn.setOnAction(e -> showSettingsDialog());
    }

    @FXML
    private void selectTemplateFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Template Excel Fájl Kiválasztása");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Fájlok (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("Összes Fájl (*.*)", "*.*")
        );

        File initialDir = resolveValidDirectory(preferencesManager.getTemplateDirectory(), new File(System.getProperty("user.home")));
        if (initialDir != null) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File selectedFile = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (selectedFile != null) {
            templatePath = selectedFile.getAbsolutePath();
            templatePathLabel.setText("Template: " + selectedFile.getName());
            File parent = selectedFile.getParentFile();
            if (parent != null) {
                preferencesManager.setTemplateDirectory(parent.getAbsolutePath());
            }
            initializeBudget();
            logger.info("Template fájl kiválasztva: {}", templatePath);
        }
    }

    private void initializeBudget() {
        try {
            if (templatePath != null && !templatePath.isEmpty()) {
                budgetManager = new BudgetManager(templatePath);
                refreshTables();
                logger.info("Költségvetés inicializálva");
            }
        } catch (Exception e) {
            logger.error("Hiba a költségvetés inicializálása során", e);
            showError("Hiba: " + e.getMessage());
        }
    }

    private void showNewItemDialog() {
        if (budgetManager == null) {
            showError("Kérlek előbb válassz ki egy template fájlt!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new-item-dialog.fxml"));

            Stage dialogStage = new Stage();
            Scene scene = new Scene(loader.load());
            dialogStage.setTitle("Új Tétel Hozzáadása");
            dialogStage.setScene(scene);

            NewItemDialogController controller = loader.getController();
            controller.setBudgetManager(budgetManager);
            controller.setMainController(this);

            dialogStage.showAndWait();
            refreshTables();
        } catch (IOException e) {
            logger.error("Hiba a dialog megnyitásakor", e);
            showError("Hiba a dialog megnyitásakor: " + e.getMessage());
        }
    }

    private void exportBudget() {
        if (budgetManager == null) {
            showError("Nincs költségvetés az exportáláshoz!");
            return;
        }

        if (budgetManager.getTotalMaterialsCount() == 0 && budgetManager.getTotalWorkItemsCount() == 0) {
            showError("A költségvetés üres! Adj hozzá legalább egy tételt!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Költségvetés Mentése");
        fileChooser.setInitialFileName("költségvetés.xlsx");

        File preferredDir = resolveValidDirectory(preferencesManager.getExportDirectory(), new File(System.getProperty("user.home")));
        if (preferredDir != null) {
            fileChooser.setInitialDirectory(preferredDir);
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Fájlok (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());
        if (file != null) {
            try {
                excelService.createBudgetFile(file.getAbsolutePath(),
                        budgetManager.getMaterials(),
                        budgetManager.getWorkItems());
                showNotification("Költségvetés sikeresen mentve:\n" + file.getAbsolutePath());
                logger.info("Költségvetés mentve: {}", file.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Hiba a költségvetés mentésekor", e);
                showError("Hiba a költségvetés mentésekor: " + e.getMessage());
            }
        }
    }

    private void consolidateData() {
        if (budgetManager == null) {
            showError("Nincs költségvetés a rendezéshez!");
            return;
        }

        if (budgetManager.getTotalMaterialsCount() == 0 && budgetManager.getTotalWorkItemsCount() == 0) {
            showError("A költségvetés üres! Adj hozzá legalább egy tételt!");
            return;
        }

        try {
            budgetManager.consolidateDuplicates();
            refreshTables();
            showNotification("Adatok sikeresen konszolidálva és rendezve!");
            logger.info("Adatok konszolidálva");
        } catch (Exception e) {
            logger.error("Hiba a konszolidálás során", e);
            showError("Hiba a konszolidálás során: " + e.getMessage());
        }
    }

    private void showSettingsDialog() {
        try {
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Beállítások");

            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");

            HBox downloadDirBox = new HBox(10);
            Label downloadDirLabel = new Label("Export/Download könyvtár:");
            downloadDirLabel.setPrefWidth(150);

            TextField downloadDirField = new TextField(preferencesManager.getExportDirectory());
            downloadDirField.setEditable(false);
            downloadDirField.setPrefWidth(300);

            Button chooseExportDirBtn = new Button("📁 Könyvtár kiválasztása");
            chooseExportDirBtn.setOnAction(e -> {
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle("Export/Download könyvtár kiválasztása");

                File current = resolveValidDirectory(downloadDirField.getText(), new File(System.getProperty("user.home")));
                if (current != null) {
                    dirChooser.setInitialDirectory(current);
                }

                File selectedDir = dirChooser.showDialog(settingsStage);
                if (selectedDir != null) {
                    downloadDirField.setText(selectedDir.getAbsolutePath());
                }
            });

            downloadDirBox.getChildren().addAll(downloadDirLabel, downloadDirField, chooseExportDirBtn);

            HBox templateDirBox = new HBox(10);
            Label templateDirLabel = new Label("Template könyvtár:");
            templateDirLabel.setPrefWidth(150);

            TextField templateDirField = new TextField(preferencesManager.getTemplateDirectory());
            templateDirField.setEditable(false);
            templateDirField.setPrefWidth(300);

            Button chooseTemplateDirBtn = new Button("📁 Könyvtár kiválasztása");
            chooseTemplateDirBtn.setOnAction(e -> {
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle("Template könyvtár kiválasztása");

                File current = resolveValidDirectory(templateDirField.getText(), new File(System.getProperty("user.home")));
                if (current != null) {
                    dirChooser.setInitialDirectory(current);
                }

                File selectedDir = dirChooser.showDialog(settingsStage);
                if (selectedDir != null) {
                    templateDirField.setText(selectedDir.getAbsolutePath());
                }
            });

            templateDirBox.getChildren().addAll(templateDirLabel, templateDirField, chooseTemplateDirBtn);

            HBox buttonBox = new HBox(10);
            buttonBox.setStyle("-fx-alignment: center-right;");
            Button saveBtn = new Button("Mentés");
            saveBtn.setPrefWidth(100);
            saveBtn.setOnAction(e -> {
                preferencesManager.setExportDirectory(downloadDirField.getText());
                preferencesManager.setTemplateDirectory(templateDirField.getText());
                loadTemplateFromPreferences();
                showNotification("Beállítások mentve a Preferencies.txt fájlba.");
                settingsStage.close();
            });

            Button okBtn = new Button("OK");
            okBtn.setPrefWidth(100);
            okBtn.setOnAction(e -> settingsStage.close());
            buttonBox.getChildren().addAll(saveBtn, okBtn);

            content.getChildren().addAll(
                    new Label("Beállítások"),
                    new Separator(),
                    downloadDirBox,
                    new Separator(),
                    templateDirBox,
                    new Separator(),
                    buttonBox
            );

            Scene scene = new Scene(content, 650, 320);
            settingsStage.setScene(scene);
            settingsStage.showAndWait();

        } catch (Exception e) {
            logger.error("Hiba a beállítások dialógus megnyitásakor", e);
            showError("Hiba: " + e.getMessage());
        }
    }

    public void refreshTables() {
        if (budgetManager != null) {
            materialTable.setItems(FXCollections.observableArrayList(budgetManager.getMaterials()));
            workItemTable.setItems(FXCollections.observableArrayList(budgetManager.getWorkItems()));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hiba");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logger.error("Hiba: {}", message);
    }

    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Siker");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logger.info("Üzenet: {}", message);
    }

    private void loadTemplateFromPreferences() {
        Path templateDir = Paths.get(preferencesManager.getTemplateDirectory());
        Path templateFile = templateDir.resolve("template.xlsx");
        File defaultTemplate = templateFile.toFile();

        if (defaultTemplate.exists() && defaultTemplate.isFile()) {
            templatePath = defaultTemplate.getAbsolutePath();
            templatePathLabel.setText("Template: " + defaultTemplate.getName());
            initializeBudget();
            logger.info("Template fájl automatikusan betöltve: {}", templatePath);
        } else {
            templatePath = null;
            templatePathLabel.setText("Template fájl nincs kiválasztva");
            logger.warn("Template fájl nem található: {}", defaultTemplate.getAbsolutePath());
        }
    }

    private File resolveValidDirectory(String path, File fallbackDir) {
        if (path != null && !path.isBlank()) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
            logger.warn("A beállított könyvtár nem létezik: {}. Fallback használva.", path);
        }

        if (fallbackDir != null && fallbackDir.exists() && fallbackDir.isDirectory()) {
            return fallbackDir;
        }

        return null;
    }
}
