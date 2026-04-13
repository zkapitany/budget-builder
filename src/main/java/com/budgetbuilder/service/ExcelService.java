package com.budgetbuilder.service;

import com.budgetbuilder.model.BudgetItem;
import com.budgetbuilder.model.Material;
import com.budgetbuilder.model.WorkItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    /**
     * Template Excel fajl olvasasa - Tetelek ful
     * OPTIMALIZALT: Csak a szukseges sorok olvasasa
     */
    public List<BudgetItem> readBudgetItems(String templatePath) throws IOException {
        List<BudgetItem> budgetItems = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(new File(templatePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // DEBUG: Az összes sheet név kiírása
            logger.info("Elérhető sheet-ek:");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i);
                logger.info("  Sheet {}: '{}'", i, sheetName);
            }
            
            Sheet sheet = workbook.getSheet("Tetelek");
            if (sheet == null) {
                logger.warn("'Tetelek' ful nem talalhato. Próbálunk más neveket...");
                String[] possibleNames = {"Tételek", "Teteleк", "Items", "Tétel", "tételek"};
                for (String name : possibleNames) {
                    sheet = workbook.getSheet(name);
                    if (sheet != null) {
                        logger.info("Tételek sheet megtalálva: '{}'", name);
                        break;
                    }
                }
            }
            
            if (sheet == null) {
                logger.warn("'Tetelek' ful nem talalhato");
                return budgetItems;
            }

            logger.info("Tételek sheet betöltve, sorok száma: {}", sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String newItemsId = getCellValueAsString(row.getCell(0));
                String newItems = getCellValueAsString(row.getCell(1));
                String itemId = getCellValueAsString(row.getCell(2));
                String item = getCellValueAsString(row.getCell(3));

                if (!itemId.isEmpty()) {
                    budgetItems.add(new BudgetItem(newItemsId, newItems, itemId, item));
                }
            }
            
            logger.info("{} tetel betoltve", budgetItems.size());
        }
        
        return budgetItems;
    }

    /**
     * Template Excel fajl olvasasa - Anyagok ful (szurt)
     * Csak az adott ItemID-vel kezdodo sorokat olvassa
     */
    public List<Material> readMaterialsByItemId(String templatePath, String itemIdPrefix) throws IOException {
        List<Material> materials = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(new File(templatePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // DEBUG: Az összes sheet név kiírása
            logger.info("Elérhető sheet-ek:");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i);
                logger.info("  Sheet {}: '{}'", i, sheetName);
            }
            
            Sheet sheet = workbook.getSheet("Anyagok");
            if (sheet == null) {
                logger.warn("'Anyagok' ful nem talalhato. Próbálunk más neveket...");
                String[] possibleNames = {"Anyagok", "Anyag", "Materials", "anyagok"};
                for (String name : possibleNames) {
                    sheet = workbook.getSheet(name);
                    if (sheet != null) {
                        logger.info("Anyagok sheet megtalálva: '{}'", name);
                        break;
                    }
                }
            }
            
            if (sheet == null) {
                logger.error("Anyagok sheet nem található!");
                return materials;
            }

            logger.info("Anyagok sheet betöltve, sorok száma: {}", sheet.getLastRowNum());
            logger.info("ItemID prefix szűrés: '{}'", itemIdPrefix);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String materialId = getCellValueAsString(row.getCell(0));
                
                logger.debug("Sor {}: MaterialID='{}' kezdődik-e '{}'-tal?", i, materialId, itemIdPrefix);
                
                if (materialId.startsWith(itemIdPrefix)) {
                    String elmukCikkszam = getCellValueAsString(row.getCell(1));
                    String eonCikkszam = getCellValueAsString(row.getCell(2));
                    String megnevezese = getCellValueAsString(row.getCell(3));
                    Double tervezettMennyiseg = getCellValueAsDouble(row.getCell(4));
                    String mertkegyseg = getCellValueAsString(row.getCell(5));
                    String megjegyzese = getCellValueAsString(row.getCell(6));

                    materials.add(new Material(materialId, elmukCikkszam, eonCikkszam,
                            megnevezese, tervezettMennyiseg, mertkegyseg, megjegyzese));
                    
                    logger.info("  ✓ Anyag hozzáadva: {} - {}", materialId, megnevezese);
                }
            }
            
            logger.info("Összesen {} anyag betöltve ItemID '{}' alapján", materials.size(), itemIdPrefix);
        }
        
        return materials;
    }

    /**
     * Template Excel fajl olvasasa - Munkatetelekel ful (szurt)
     * Csak az adott ItemID-vel kezdodo sorokat olvassa
     */
    public List<WorkItem> readWorkItemsByItemId(String templatePath, String itemIdPrefix) throws IOException {
        List<WorkItem> workItems = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(new File(templatePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // DEBUG: Az összes sheet név kiírása
            logger.info("Elérhető sheet-ek:");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i);
                logger.info("  Sheet {}: '{}'", i, sheetName);
            }
            
            Sheet sheet = workbook.getSheet("Munkatételek");
            if (sheet == null) {
                logger.warn("'Munkatételek' ful nem talalhato. Próbálunk más neveket...");
                String[] possibleNames = {"Munkatételek", "Munkatetelekel", "WorkItems", "munkatetelekel"};
                for (String name : possibleNames) {
                    sheet = workbook.getSheet(name);
                    if (sheet != null) {
                        logger.info("Munkatételek sheet megtalálva: '{}'", name);
                        break;
                    }
                }
            }
            
            if (sheet == null) {
                logger.error("Munkatételek sheet nem található!");
                return workItems;
            }

            logger.info("Munkatételek sheet betöltve, sorok száma: {}", sheet.getLastRowNum());
            logger.info("ItemID prefix szűrés: '{}'", itemIdPrefix);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String workId = getCellValueAsString(row.getCell(0));
                
                logger.debug("Sor {}: WorkID='{}' kezdődik-e '{}'-tal?", i, workId, itemIdPrefix);
                
                if (workId.startsWith(itemIdPrefix)) {
                    String eonCikkszam = getCellValueAsString(row.getCell(1));
                    String bszjAzonosito = getCellValueAsString(row.getCell(2));
                    String elszamolaTetelmegnevezese = getCellValueAsString(row.getCell(3));
                    Double tervezettMennyiseg = getCellValueAsDouble(row.getCell(4));
                    String mertkegyseg = getCellValueAsString(row.getCell(5));
                    String megjegyzese = getCellValueAsString(row.getCell(6));

                    workItems.add(new WorkItem(workId, eonCikkszam, bszjAzonosito,
                            elszamolaTetelmegnevezese, tervezettMennyiseg, mertkegyseg, megjegyzese));
                    
                    logger.info("  ✓ Munkatétel hozzáadva: {} - {}", workId, elszamolaTetelmegnevezese);
                }
            }
            
            logger.info("Összesen {} munkatétel betöltve ItemID '{}' alapján", workItems.size(), itemIdPrefix);
        }
        
        return workItems;
    }

    /**
     * Koltsegvetes Excel fajl letrehozasa - szep formatazassal
     */
    public void createBudgetFile(String outputPath, List<Material> materials, List<WorkItem> workItems) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet materialsSheet = workbook.createSheet("Anyaglista");
            createMaterialsSheet(materialsSheet, materials, workbook);

            Sheet workItemsSheet = workbook.createSheet("Munkatetelekel");
            createWorkItemsSheet(workItemsSheet, workItems, workbook);

            try (FileOutputStream fos = new FileOutputStream(new File(outputPath))) {
                workbook.write(fos);
            }
            
            logger.info("Koltsegvetes fajl letrehozva: {}", outputPath);
        }
    }

    /**
     * Anyagok hozzaadasa az existalo koltsegvetes fajlhoz
     */
    public void addMaterialsToBudget(String budgetPath, List<Material> materials) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(budgetPath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Anyaglista");
            if (sheet == null) {
                sheet = workbook.createSheet("Anyaglista");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Anyag_ID", "regi ELMŰ-s Cikkszam", "EON CIKKSZAM", 
                                   "Megnevezese", "Tervezett mennyiseg (KIVIT)", "M. e.", "Megjegyzese"};
                
                CellStyle headerStyle = createHeaderStyle(workbook);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
            }

            int lastRow = sheet.getLastRowNum();

            for (Material material : materials) {
                Row row = sheet.createRow(++lastRow);
                row.createCell(0).setCellValue(material.getMaterialId());
                row.createCell(1).setCellValue(material.getElmukCikkszam());
                row.createCell(2).setCellValue(material.getEonCikkszam());
                row.createCell(3).setCellValue(material.getMegnevezese());
                row.createCell(4).setCellValue(material.getTervezettMennyiseg() != null ? material.getTervezettMennyiseg() : 0);
                row.createCell(5).setCellValue(material.getMertkegyseg());
                row.createCell(6).setCellValue(material.getMegjegyzes());
            }

            // Oszlopszélességek beállítása
            sheet.setColumnWidth(0, 3000);   // Anyag_ID
            sheet.setColumnWidth(1, 4000);   // ELMŰ Cikkszám
            sheet.setColumnWidth(2, 4000);   // EON Cikkszám
            sheet.setColumnWidth(3, 6000);   // Megnevezése
            sheet.setColumnWidth(4, 4000);   // Tervezett mennyiség
            sheet.setColumnWidth(5, 2000);   // M.e.
            sheet.setColumnWidth(6, 3500);   // Megjegyzése

            try (FileOutputStream fos = new FileOutputStream(new File(budgetPath))) {
                workbook.write(fos);
            }
            
            logger.info("{} anyag hozzaadva", materials.size());
        }
    }

    /**
     * Munkatetelekel hozzaadasa az existalo koltsegvetes fajlhoz
     */
    public void addWorkItemsToBudget(String budgetPath, List<WorkItem> workItems) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(budgetPath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Munkatetelekel");
            if (sheet == null) {
                sheet = workbook.createSheet("Munkatetelekel");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Munka_ID", "EON CIKKSZAM", "BSZJ azonosito",
                                   "Elszamolasi tetel megnevezese", "Tervezett mennyiseg (KIVIT)", "ME", "Megjegyzese"};
                
                CellStyle headerStyle = createHeaderStyle(workbook);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
            }

            int lastRow = sheet.getLastRowNum();

            for (WorkItem workItem : workItems) {
                Row row = sheet.createRow(++lastRow);
                row.createCell(0).setCellValue(workItem.getWorkId());
                row.createCell(1).setCellValue(workItem.getEonCikkszam());
                row.createCell(2).setCellValue(workItem.getBszjAzonosito());
                row.createCell(3).setCellValue(workItem.getElszamolaTetelmegnevezese());
                row.createCell(4).setCellValue(workItem.getTervezettMennyiseg() != null ? workItem.getTervezettMennyiseg() : 0);
                row.createCell(5).setCellValue(workItem.getMertkegyseg());
                row.createCell(6).setCellValue(workItem.getMegjegyzes());
            }

            // Oszlopszélességek beállítása
            sheet.setColumnWidth(0, 3000);   // Munka_ID
            sheet.setColumnWidth(1, 4000);   // EON Cikkszám
            sheet.setColumnWidth(2, 3500);   // BSZJ azonosító
            sheet.setColumnWidth(3, 7000);   // Elszámolási tétel megnevezése
            sheet.setColumnWidth(4, 4000);   // Tervezett mennyiség
            sheet.setColumnWidth(5, 2000);   // ME
            sheet.setColumnWidth(6, 3500);   // Megjegyzése

            try (FileOutputStream fos = new FileOutputStream(new File(budgetPath))) {
                workbook.write(fos);
            }
            
            logger.info("{} munkatetelekel hozzaadva", workItems.size());
        }
    }

    // ==================== SEGD METODUSOK ====================

    /**
     * Header cella stílus létrehozása - szép formázással
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Háttérszín - sötétkék
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Betűszín - fehér
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // Szegélyek
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Szegély színe - fekete
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        
        // Középre igazítás
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Wordwrap
        style.setWrapText(true);
        
        return style;
    }

    /**
     * Adat cella stílus létrehozása
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Szegélyek
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Szegély színe - szürke
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        // Balra igazítás
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    /**
     * Anyagok sheet letrehozasa - szep formatazassal
     */
    private void createMaterialsSheet(Sheet sheet, List<Material> materials, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Anyag_ID", "regi ELMŰ-s Cikkszam", "EON CIKKSZAM", 
                           "Megnevezese", "Tervezett mennyiseg (KIVIT)", "M. e.", "Megjegyzese"};
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle dataStyle = createDataStyle(workbook);
        int rowNum = 1;
        for (Material material : materials) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(material.getMaterialId());
            cell0.setCellStyle(dataStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(material.getElmukCikkszam());
            cell1.setCellStyle(dataStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(material.getEonCikkszam());
            cell2.setCellStyle(dataStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(material.getMegnevezese());
            cell3.setCellStyle(dataStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(material.getTervezettMennyiseg() != null ? material.getTervezettMennyiseg() : 0);
            cell4.setCellStyle(dataStyle);
            
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(material.getMertkegyseg());
            cell5.setCellStyle(dataStyle);
            
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(material.getMegjegyzes());
            cell6.setCellStyle(dataStyle);
        }
        
        // Fejléc sora magasabbá
        headerRow.setHeightInPoints(25);
        
        // Oszlopszélességek beállítása
        sheet.setColumnWidth(0, 3000);   // Anyag_ID
        sheet.setColumnWidth(1, 4000);   // ELMŰ Cikkszám
        sheet.setColumnWidth(2, 4000);   // EON Cikkszám
        sheet.setColumnWidth(3, 6000);   // Megnevezése
        sheet.setColumnWidth(4, 4000);   // Tervezett mennyiség
        sheet.setColumnWidth(5, 2000);   // M.e.
        sheet.setColumnWidth(6, 3500);   // Megjegyzése
        
        // Fejléc fagyasztása
        sheet.createFreezePane(0, 1);
    }

    /**
     * Munkatetelekel sheet letrehozasa - szep formatazassal
     */
    private void createWorkItemsSheet(Sheet sheet, List<WorkItem> workItems, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Munka_ID", "EON CIKKSZAM", "BSZJ azonosito",
                           "Elszamolasi tetel megnevezese", "Tervezett mennyiseg (KIVIT)", "ME", "Megjegyzese"};
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle dataStyle = createDataStyle(workbook);
        int rowNum = 1;
        for (WorkItem workItem : workItems) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(workItem.getWorkId());
            cell0.setCellStyle(dataStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(workItem.getEonCikkszam());
            cell1.setCellStyle(dataStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(workItem.getBszjAzonosito());
            cell2.setCellStyle(dataStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(workItem.getElszamolaTetelmegnevezese());
            cell3.setCellStyle(dataStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(workItem.getTervezettMennyiseg() != null ? workItem.getTervezettMennyiseg() : 0);
            cell4.setCellStyle(dataStyle);
            
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(workItem.getMertkegyseg());
            cell5.setCellStyle(dataStyle);
            
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(workItem.getMegjegyzes());
            cell6.setCellStyle(dataStyle);
        }
        
        // Fejléc sora magasabbá
        headerRow.setHeightInPoints(25);
        
        // Oszlopszélességek beállítása
        sheet.setColumnWidth(0, 3000);   // Munka_ID
        sheet.setColumnWidth(1, 4000);   // EON Cikkszám
        sheet.setColumnWidth(2, 3500);   // BSZJ azonosító
        sheet.setColumnWidth(3, 7000);   // Elszámolási tétel megnevezése
        sheet.setColumnWidth(4, 4000);   // Tervezett mennyiség
        sheet.setColumnWidth(5, 2000);   // ME
        sheet.setColumnWidth(6, 3500);   // Megjegyzése
        
        // Fejléc fagyasztása
        sheet.createFreezePane(0, 1);
    }

    /**
     * Cell ertek String-kent
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        yield cell.getDateCellValue().toString();
                    } else {
                        double value = cell.getNumericCellValue();
                        if (value == Math.floor(value)) {
                            yield String.valueOf((long) value);
                        } else {
                            yield String.valueOf(value);
                        }
                    }
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case BLANK -> "";
                default -> "";
            };
        } catch (Exception e) {
            logger.warn("Hiba a cell ertek kiolvasasakor");
            return "";
        }
    }

    /**
     * Cell ertek Double-kent
     */
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        try {
            return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : 0.0;
        } catch (Exception e) {
            logger.warn("Hiba a double ertek kiolvasasakor");
            return 0.0;
        }
    }
}