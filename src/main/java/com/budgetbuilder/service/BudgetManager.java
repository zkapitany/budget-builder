package com.budgetbuilder.service;

import com.budgetbuilder.model.Material;
import com.budgetbuilder.model.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BudgetManager {
    private static final Logger logger = LoggerFactory.getLogger(BudgetManager.class);
    
    private final ExcelService excelService;
    private List<Material> materials;
    private List<WorkItem> workItems;
    private String budgetFilePath;
    private String templatePath;

    /**
     * BudgetManager konstruktor
     * @param templatePath A template Excel fájl elérési útvonala
     * @throws IOException Ha hiba van a fájl kezelésekor
     */
    public BudgetManager(String templatePath) throws IOException {
        this.excelService = new ExcelService();
        this.templatePath = templatePath;
        this.budgetFilePath = null;
        this.materials = new ArrayList<>();
        this.workItems = new ArrayList<>();
        
        // Template fájl létezésének ellenőrzése
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            throw new IOException("Template fájl nem található: " + templatePath);
        }
        
        logger.info("BudgetManager inicializálva. Template: {}", templatePath);
    }

    /**
     * ItemId alapján anyagokat szűr a template fájlból
     * OPTIMALIZÁLT: Csak a szükséges sorok olvasása
     * @param itemId Az elem ID-ja (pl. "1.1", "1.2")
     * @return Az adott ItemID-vel kezdődő anyagok listája
     */
    public List<Material> getMaterialsByItemId(String itemId) throws IOException {
        logger.info("Anyagok lekérése ItemID alapján: '{}'", itemId);
        return excelService.readMaterialsByItemId(templatePath, itemId);
    }

    /**
     * ItemId alapján munkatételeket szűr a template fájlból
     * OPTIMALIZÁLT: Csak a szükséges sorok olvasása
     * @param itemId Az elem ID-ja (pl. "1.1", "1.2")
     * @return Az adott ItemID-vel kezdődő munkatételek listája
     */
    public List<WorkItem> getWorkItemsByItemId(String itemId) throws IOException {
        logger.info("Munkatételek lekérése ItemID alapján: '{}'", itemId);
        return excelService.readWorkItemsByItemId(templatePath, itemId);
    }

    /**
	* Tétel hozzáadása a memóriában lévő költségvetéshez
	* (nem menti le a fájlba azonnal)
	*/
	public void addItemToBudgetMemory(String itemId) throws IOException {
		logger.info("Tétel hozzáadása memóriához: {}", itemId);
    
		// ItemID után egy pontot adunk hozzá a szűréshez
		String itemIdPrefix = itemId + ".";
		logger.info("Szűrési prefix: '{}'", itemIdPrefix);
    
		// Anyagok lekérése és hozzáadása
		List<Material> itemMaterials = getMaterialsByItemId(itemIdPrefix);
		logger.info("{} anyag szűrve", itemMaterials.size());
		materials.addAll(itemMaterials);
    
		// Munkatételek lekérése és hozzáadása
		List<WorkItem> itemWorkItems = getWorkItemsByItemId(itemIdPrefix);
		logger.info("{} munkatétel szűrve", itemWorkItems.size());
		workItems.addAll(itemWorkItems);
    
		logger.info("Tétel hozzáadva memóriához: {}", itemId);
	}

    /**
	* Tétel hozzáadása a költségvetéshez és fájlba mentése
	*/
	public void addItemToBudget(String itemId, String budgetPath) throws IOException {
		if (budgetPath == null || budgetPath.isEmpty()) {
        throw new IOException("Költségvetés fájl elérési útvonala nem megadva!");
		}

		this.budgetFilePath = budgetPath;
    
		logger.info("Tétel hozzáadása a költségvetéshez: {}", itemId);
		logger.info("Költségvetés fájl: {}", budgetPath);
    
		// ItemID után egy pontot adunk hozzá a szűréshez
		String itemIdPrefix = itemId + ".";
    
		// Anyagok lekérése és hozzáadása
		List<Material> itemMaterials = getMaterialsByItemId(itemIdPrefix);
		logger.info("{} anyag szűrve", itemMaterials.size());
		materials.addAll(itemMaterials);
		excelService.addMaterialsToBudget(budgetPath, itemMaterials);
    
		// Munkatételek lekérése és hozzáadása
		List<WorkItem> itemWorkItems = getWorkItemsByItemId(itemIdPrefix);
		logger.info("{} munkatétel szűrve", itemWorkItems.size());
		workItems.addAll(itemWorkItems);
		excelService.addWorkItemsToBudget(budgetPath, itemWorkItems);
    
		logger.info("Tétel sikeresen hozzáadva költségvetéshez: {}", itemId);
	}

    /**
     * Azonos sorok konszolidálása (összesítése)
     * Összevonja az azonos EON cikkszámú vagy megnevezésű sorokat,
     * és összeadja a tervezett mennyiségeket
     */
    public void consolidateDuplicates() throws IOException {
        if (budgetFilePath == null) {
            logger.info("Nincs költségvetés fájl, csak memóriában konszolidálunk");
        }

        logger.info("Konszolidálás megkezdve");
        logger.info("Konszolidálás előtt: {} anyag, {} munkatétel", 
            materials.size(), workItems.size());
        
        materials = consolidateMaterialList(materials);
        workItems = consolidateWorkItemList(workItems);
        
        logger.info("Konszolidálás után: {} anyag, {} munkatétel", 
            materials.size(), workItems.size());
        
        // Ha van költségvetés fájl, akkor frissítse azt
        if (budgetFilePath != null && !budgetFilePath.isEmpty()) {
            File budgetFile = new File(budgetFilePath);
            if (budgetFile.exists()) {
                boolean deleted = budgetFile.delete();
                if (deleted) {
                    logger.info("Régi költségvetés fájl törölve");
                }
            }
            
            excelService.createBudgetFile(budgetFilePath, materials, workItems);
            logger.info("Költségvetés fájl frissítve konszolidált adatokkal");
        }
    }

    /**
     * Anyagok listájának konszolidálása
     * Összevonja az azonos EON Cikkszám + Megnevezés kombinációval rendelkező sorokat
     * @param materials Az anyagok listája
     * @return A konszolidált anyagok listája
     */
    private List<Material> consolidateMaterialList(List<Material> materials) {
        logger.info("Anyagok konszolidálása megkezdve");
        
        Map<String, Material> consolidated = new LinkedHashMap<>();
        
        for (Material material : materials) {
            // Kulcs: EON Cikkszám + Megnevezés
            String key = material.getEonCikkszam() + "|" + material.getMegnevezese();
            
            if (consolidated.containsKey(key)) {
                // Ha már létezik ez a kombináció, összadjuk a mennyiséget
                Material existing = consolidated.get(key);
                Double newQuantity = existing.getTervezettMennyiseg() + material.getTervezettMennyiseg();
                existing.setTervezettMennyiseg(newQuantity);
                logger.debug("Anyag mennyiség frissítve: {} = {}", key, newQuantity);
            } else {
                // Új anyag hozzáadása
                consolidated.put(key, new Material(
                    material.getMaterialId(),
                    material.getElmukCikkszam(),
                    material.getEonCikkszam(),
                    material.getMegnevezese(),
                    material.getTervezettMennyiseg(),
                    material.getMertkegyseg(),
                    material.getMegjegyzes()
                ));
                logger.debug("Új anyag hozzáadva: {}", key);
            }
        }
        
        logger.info("Anyagok konszolidálva: {} -> {}", materials.size(), consolidated.size());
        return new ArrayList<>(consolidated.values());
    }

    /**
     * Munkatételek listájának konszolidálása
     * Összevonja az azonos EON Cikkszám + Elszámolási tétel kombinációval rendelkező sorokat
     * @param workItems A munkatételek listája
     * @return A konszolidált munkatételek listája
     */
    private List<WorkItem> consolidateWorkItemList(List<WorkItem> workItems) {
        logger.info("Munkatételek konszolidálása megkezdve");
        
        Map<String, WorkItem> consolidated = new LinkedHashMap<>();
        
        for (WorkItem workItem : workItems) {
            // Kulcs: EON Cikkszám + Elszámolási tétel megnevezése
            String key = workItem.getEonCikkszam() + "|" + workItem.getElszamolaTetelmegnevezese();
            
            if (consolidated.containsKey(key)) {
                // Ha már létezik ez a kombináció, összadjuk a mennyiséget
                WorkItem existing = consolidated.get(key);
                Double newQuantity = existing.getTervezettMennyiseg() + workItem.getTervezettMennyiseg();
                existing.setTervezettMennyiseg(newQuantity);
                logger.debug("Munkatétel mennyiség frissítve: {} = {}", key, newQuantity);
            } else {
                // Új munkatétel hozzáadása
                consolidated.put(key, new WorkItem(
                    workItem.getWorkId(),
                    workItem.getEonCikkszam(),
                    workItem.getBszjAzonosito(),
                    workItem.getElszamolaTetelmegnevezese(),
                    workItem.getTervezettMennyiseg(),
                    workItem.getMertkegyseg(),
                    workItem.getMegjegyzes()
                ));
                logger.debug("Új munkatétel hozzáadva: {}", key);
            }
        }
        
        logger.info("Munkatételek konszolidálva: {} -> {}", workItems.size(), consolidated.size());
        return new ArrayList<>(consolidated.values());
    }

    /**
     * Anyagok rendezése EON Cikkszám alapján
     */
    public void sortMaterialsByEonCikkszam() {
        materials.sort(Comparator.comparing(Material::getEonCikkszam));
        logger.info("Anyagok rendezve EON Cikkszám alapján");
    }

    /**
     * Munkatételek rendezése EON Cikkszám alapján
     */
    public void sortWorkItemsByEonCikkszam() {
        workItems.sort(Comparator.comparing(WorkItem::getEonCikkszam));
        logger.info("Munkatételek rendezve EON Cikkszám alapján");
    }

    /**
     * Anyagok rendezése Megnevezés alapján
     */
    public void sortMaterialsByMegnevezese() {
        materials.sort(Comparator.comparing(Material::getMegnevezese));
        logger.info("Anyagok rendezve Megnevezés alapján");
    }

    /**
     * Munkatételek rendezése Elszámolási tétel alapján
     */
    public void sortWorkItemsByElszamolaTetelmegnevezese() {
        workItems.sort(Comparator.comparing(WorkItem::getElszamolaTetelmegnevezese));
        logger.info("Munkatételek rendezve Elszámolási tétel alapján");
    }

    /**
     * Anyagok rendezése mennyiség alapján (csökkenő)
     */
    public void sortMaterialsByQuantityDesc() {
        materials.sort((m1, m2) -> m2.getTervezettMennyiseg().compareTo(m1.getTervezettMennyiseg()));
        logger.info("Anyagok rendezve mennyiség alapján (csökkenő)");
    }

    /**
     * Munkatételek rendezése mennyiség alapján (csökkenő)
     */
    public void sortWorkItemsByQuantityDesc() {
        workItems.sort((w1, w2) -> w2.getTervezettMennyiseg().compareTo(w1.getTervezettMennyiseg()));
        logger.info("Munkatételek rendezve mennyiség alapján (csökkenő)");
    }

    /**
     * Anyag törlése a listából MaterialId alapján
     */
    public void removeMaterial(String materialId) {
        int beforeSize = materials.size();
        materials.removeIf(m -> m.getMaterialId().equals(materialId));
        int afterSize = materials.size();
        logger.info("Anyag törölve: {} (előtte: {}, után: {})", materialId, beforeSize, afterSize);
    }

    /**
     * Munkatétel törlése a listából WorkId alapján
     */
    public void removeWorkItem(String workId) {
        int beforeSize = workItems.size();
        workItems.removeIf(w -> w.getWorkId().equals(workId));
        int afterSize = workItems.size();
        logger.info("Munkatétel törölve: {} (előtte: {}, után: {})", workId, beforeSize, afterSize);
    }

    /**
     * Összes anyag törlése
     */
    public void clearAllMaterials() {
        int size = materials.size();
        materials.clear();
        logger.info("Összes anyag törölve ({} tétel)", size);
    }

    /**
     * Összes munkatétel törlése
     */
    public void clearAllWorkItems() {
        int size = workItems.size();
        workItems.clear();
        logger.info("Összes munkatétel törölve ({} tétel)", size);
    }

    /**
     * Statisztika: Anyagok összesen
     */
    public int getTotalMaterialsCount() {
        return materials.size();
    }

    /**
     * Statisztika: Munkatételek összesen
     */
    public int getTotalWorkItemsCount() {
        return workItems.size();
    }

    /**
     * Statisztika: Összes anyag mennyisége
     */
    public Double getTotalMaterialsQuantity() {
        return materials.stream()
            .mapToDouble(Material::getTervezettMennyiseg)
            .sum();
    }

    /**
     * Statisztika: Összes munkatétel mennyisége
     */
    public Double getTotalWorkItemsQuantity() {
        return workItems.stream()
            .mapToDouble(WorkItem::getTervezettMennyiseg)
            .sum();
    }

    /**
     * Anyag keresése EON Cikkszám alapján
     */
    public List<Material> searchMaterialsByEonCikkszam(String eonCikkszam) {
        logger.info("Anyag keresése EON Cikkszám alapján: {}", eonCikkszam);
        return materials.stream()
            .filter(m -> m.getEonCikkszam().contains(eonCikkszam))
            .toList();
    }

    /**
     * Munkatétel keresése EON Cikkszám alapján
     */
    public List<WorkItem> searchWorkItemsByEonCikkszam(String eonCikkszam) {
        logger.info("Munkatétel keresése EON Cikkszám alapján: {}", eonCikkszam);
        return workItems.stream()
            .filter(w -> w.getEonCikkszam().contains(eonCikkszam))
            .toList();
    }

    /**
     * Anyag keresése Megnevezés alapján
     */
    public List<Material> searchMaterialsByMegnevezese(String megnevezese) {
        logger.info("Anyag keresése Megnevezés alapján: {}", megnevezese);
        return materials.stream()
            .filter(m -> m.getMegnevezese().toLowerCase().contains(megnevezese.toLowerCase()))
            .toList();
    }

    /**
     * Munkatétel keresése Elszámolási tétel alapján
     */
    public List<WorkItem> searchWorkItemsByElszamolaTetelmegnevezese(String elszamolaTetelmegnevezese) {
        logger.info("Munkatétel keresése Elszámolási tétel alapján: {}", elszamolaTetelmegnevezese);
        return workItems.stream()
            .filter(w -> w.getElszamolaTetelmegnevezese().toLowerCase().contains(elszamolaTetelmegnevezese.toLowerCase()))
            .toList();
    }

    /**
     * Összes anyag lekérése (másolat)
     */
    public List<Material> getMaterials() {
        return new ArrayList<>(materials);
    }

    /**
     * Összes munkatétel lekérése (másolat)
     */
    public List<WorkItem> getWorkItems() {
        return new ArrayList<>(workItems);
    }

    /**
     * Költségvetés fájl elérési útvonalának lekérése
     */
    public String getBudgetFilePath() {
        return budgetFilePath;
    }

    /**
     * Template fájl elérési útvonalának lekérése
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * ExcelService lekérése (ha szükséges)
     */
    public ExcelService getExcelService() {
        return excelService;
    }

    /**
     * Az aktuális anyagok és munkatételek állapotának kiírása (debug)
     */
    public void printStatus() {
        logger.info("======== KÖLTSÉGVETÉS ÁLLAPOTA ========");
        logger.info("Template fájl: {}", templatePath);
        logger.info("Költségvetés fájl: {}", budgetFilePath);
        logger.info("Anyagok száma: {}", materials.size());
        logger.info("Munkatételek száma: {}", workItems.size());
        logger.info("Összes anyag mennyisége: {}", getTotalMaterialsQuantity());
        logger.info("Összes munkatétel mennyisége: {}", getTotalWorkItemsQuantity());
        logger.info("========================================");
    }
}