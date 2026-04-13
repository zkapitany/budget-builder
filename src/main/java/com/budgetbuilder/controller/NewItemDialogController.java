package com.budgetbuilder.controller;

import com.budgetbuilder.model.BudgetItem;
import com.budgetbuilder.service.ExcelService;
import com.budgetbuilder.service.BudgetManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewItemDialogController {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialogController.class);

    @FXML private ComboBox<BudgetItem> itemTypeCombo;
    @FXML private ComboBox<BudgetItem> subItemCombo;
    @FXML private Button addBtn;
    @FXML private Button cancelBtn;

    private BudgetManager budgetManager;
    private MainController mainController;
    private ExcelService excelService;
    private List<BudgetItem> allBudgetItems;

    @FXML
    public void initialize() {
        logger.info("NewItemDialogController inicializálva");
        
        try {
            excelService = new ExcelService();
            setupComboBoxes();
            
            if (itemTypeCombo != null) {
                itemTypeCombo.setOnAction(e -> updateSubItems());
            }
            if (addBtn != null) {
                addBtn.setOnAction(e -> addItem());
            }
            if (cancelBtn != null) {
                cancelBtn.setOnAction(e -> closeDialog());
            }
            
            logger.info("Event handlerek regisztrálva");
        } catch (Exception e) {
            logger.error("Hiba az inicializálás során", e);
            showError("Inicializálási hiba: " + e.getMessage());
        }
    }

    public void setBudgetManager(BudgetManager budgetManager) {
        try {
            this.budgetManager = budgetManager;
            if (budgetManager != null) {
                loadBudgetItems();
            }
        } catch (Exception e) {
            logger.error("Hiba a BudgetManager beállításakor", e);
            showError("Hiba: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupComboBoxes() {
        if (itemTypeCombo == null) {
            logger.warn("itemTypeCombo null!");
            return;
        }

        itemTypeCombo.setConverter(new StringConverter<BudgetItem>() {
            @Override
            public String toString(BudgetItem item) {
                if (item == null) return "";
                return item.getNewItems() != null ? item.getNewItems() : "";
            }

            @Override
            public BudgetItem fromString(String string) {
                return null;
            }
        });

        if (subItemCombo != null) {
            subItemCombo.setConverter(new StringConverter<BudgetItem>() {
                @Override
                public String toString(BudgetItem item) {
                    if (item == null) return "";
                    String itemText = item.getItem() != null ? item.getItem() : "";
                    String itemId = item.getItemId() != null ? item.getItemId() : "";
                    return itemId.isEmpty() ? itemText : itemId + " - " + itemText;
                }

                @Override
                public BudgetItem fromString(String string) {
                    return null;
                }
            });
        }
        
        logger.info("ComboBox-ok beállítva");
    }

    private void loadBudgetItems() {
        try {
            if (budgetManager == null || budgetManager.getTemplatePath() == null) {
                showError("Költségvetés manager vagy template fájl nincs beállítva!");
                return;
            }

            allBudgetItems = excelService.readBudgetItems(budgetManager.getTemplatePath());
            
            if (allBudgetItems == null || allBudgetItems.isEmpty()) {
                logger.warn("Nem találhatók tételek a template fájlban");
                showError("Nem találhatók tételek a template fájlban!");
                return;
            }

            logger.info("===== TÉTELEK BETÖLTÉSE KÉSZ =====");
            logger.info("Összes betöltött tétel: {}", allBudgetItems.size());

            List<BudgetItem> uniqueItems = new ArrayList<>();
            List<String> seenIds = new ArrayList<>();
            
            for (BudgetItem item : allBudgetItems) {
                if (item.getNewItems() != null && !item.getNewItems().isEmpty() && 
                    item.getNewItemsId() != null && !item.getNewItemsId().isEmpty() &&
                    !seenIds.contains(item.getNewItemsId())) {
                    uniqueItems.add(item);
                    seenIds.add(item.getNewItemsId());
                    logger.info("Egyedi tétel: NewItemsID='{}', NewItems='{}'", 
                        item.getNewItemsId(), item.getNewItems());
                }
            }

            if (itemTypeCombo != null) {
                itemTypeCombo.setItems(FXCollections.observableArrayList(uniqueItems));
                logger.info("{} egyedi tétel betöltve az első legördülő menübe", uniqueItems.size());
            }
        } catch (IOException e) {
            logger.error("Hiba a tételek betöltésekor", e);
            showError("Hiba a tételek betöltésekor: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Váratlan hiba a loadBudgetItems során", e);
            showError("Váratlan hiba: " + e.getMessage());
        }
    }

    private void updateSubItems() {
        try {
            if (itemTypeCombo == null || allBudgetItems == null) {
                logger.warn("itemTypeCombo vagy allBudgetItems null!");
                return;
            }

            BudgetItem selectedItem = itemTypeCombo.getSelectionModel().getSelectedItem();
            
            if (selectedItem == null) {
                logger.info("Nincs kiválasztott tétel");
                if (subItemCombo != null) {
                    subItemCombo.setItems(FXCollections.observableArrayList());
                }
                return;
            }

            String selectedNewItemsId = selectedItem.getNewItemsId();
            logger.info("Alkategóriák frissítése: {}", selectedNewItemsId);

            List<BudgetItem> subItems = new ArrayList<>();
            
            for (BudgetItem item : allBudgetItems) {
                String itemId = item.getItemId();
                
                if (itemId != null && itemId.startsWith(selectedNewItemsId)) {
                    subItems.add(item);
                    logger.info("  Match: ItemID='{}' - '{}'", itemId, item.getItem());
                }
            }

            logger.info("Szűrés vége: {} alkategória szűrve", subItems.size());

            if (subItemCombo != null) {
                subItemCombo.setItems(FXCollections.observableArrayList(subItems));
                if (subItems.isEmpty()) {
                    logger.warn("Figyelem: Nincs egyező alkategória!");
                }
            }
        } catch (Exception e) {
            logger.error("Hiba az alkategóriák frissítésekor", e);
            showError("Hiba az alkategóriák frissítésekor: " + e.getMessage());
        }
    }

    private void addItem() {
        try {
            BudgetItem selectedSubItem = subItemCombo.getSelectionModel().getSelectedItem();
            
            if (selectedSubItem == null) {
                showError("Kérlek válassz ki egy alkategóriát!");
                return;
            }

            if (budgetManager == null) {
                showError("Költségvetés manager nincs beállítva!");
                return;
            }

            String itemId = selectedSubItem.getItemId();
            logger.info("Tétel hozzáadása: {}", itemId);
            
            budgetManager.addItemToBudgetMemory(itemId);
            
            if (mainController != null) {
                mainController.refreshTables();
            }
            
            showSuccess("Tétel hozzáadva!\n\n" + selectedSubItem.getItem());
            closeDialog();
            
        } catch (IOException e) {
            logger.error("Hiba a tétel hozzáadásakor (IO)", e);
            showError("Hiba a tétel hozzáadásakor: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Váratlan hiba a tétel hozzáadásakor", e);
            showError("Váratlan hiba: " + e.getMessage());
        }
    }

    private void closeDialog() {
        try {
            if (cancelBtn != null && cancelBtn.getScene() != null) {
                Stage stage = (Stage) cancelBtn.getScene().getWindow();
                stage.close();
                logger.info("Dialog bezárva");
            }
        } catch (Exception e) {
            logger.error("Hiba a dialog bezárásakor", e);
        }
    }

    private void showError(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            logger.error("Hiba az error dialog megjelenítésekor", e);
        }
    }

    private void showSuccess(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Siker");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            logger.error("Hiba a success dialog megjelenítésekor", e);
        }
    }
}