package com.mybharat.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * ExcelUtils — Reusable utility for reading/writing Excel files.
 *
 * Usage:
 *   List<String> events = ExcelUtils.readColumn("src/test/resources/testdata/MegaEvents.xlsx", "MegaEvents", 0);
 *   String firstEvent = ExcelUtils.readCell("path.xlsx", "Sheet1", 1, 0);
 */
public class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    /**
     * Read all non-empty values from a specific column (skipping header row).
     *
     * @param filePath  absolute or relative path to .xlsx file
     * @param sheetName sheet name
     * @param colIndex  0-based column index
     * @return list of cell values as strings
     */
    public static List<String> readColumn(String filePath, String sheetName, int colIndex) {
        List<String> values = new ArrayList<>();
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") + File.separator + filePath);
        }

        log.info("[ExcelUtils] Reading column {} from sheet '{}' in: {}", colIndex, sheetName, file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
                log.warn("[ExcelUtils] Sheet '{}' not found, using first sheet: '{}'", sheetName, sheet.getSheetName());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header (row 0)
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell cell = row.getCell(colIndex);
                if (cell == null) continue;

                String value = getCellValueAsString(cell).trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }

            log.info("[ExcelUtils] Read {} values from column {}", values.size(), colIndex);
        } catch (IOException e) {
            log.error("[ExcelUtils] Failed to read Excel: {}", e.getMessage());
            throw new RuntimeException("Excel read failed: " + e.getMessage(), e);
        }

        return values;
    }

    /**
     * Read a single cell value.
     *
     * @param filePath  path to .xlsx file
     * @param sheetName sheet name
     * @param rowIndex  0-based row index
     * @param colIndex  0-based column index
     * @return cell value as string
     */
    public static String readCell(String filePath, String sheetName, int rowIndex, int colIndex) {
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") + File.separator + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            Row row = sheet.getRow(rowIndex);
            if (row == null) return "";

            Cell cell = row.getCell(colIndex);
            if (cell == null) return "";

            return getCellValueAsString(cell).trim();
        } catch (IOException e) {
            log.error("[ExcelUtils] Failed to read cell: {}", e.getMessage());
            throw new RuntimeException("Excel read failed: " + e.getMessage(), e);
        }
    }

    /**
     * Create an Excel file with a header and data rows.
     *
     * @param filePath  path to create the .xlsx file
     * @param sheetName sheet name
     * @param header    header row values
     * @param data      data rows (list of string arrays)
     */
    public static void createExcel(String filePath, String sheetName, String[] header, List<String[]> data) {
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") + File.separator + filePath);
        }

        file.getParentFile().mkdirs();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                headerRow.createCell(i).setCellValue(header[i]);
            }

            // Data rows
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    row.createCell(j).setCellValue(rowData[j]);
                }
            }

            workbook.write(fos);
            log.info("[ExcelUtils] Excel created: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("[ExcelUtils] Failed to create Excel: {}", e.getMessage());
            throw new RuntimeException("Excel creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Append a single value to the next empty row in column 0 of an existing Excel file.
     *
     * @param filePath  path to existing .xlsx file
     * @param sheetName sheet name
     * @param value     value to append
     */
    public static void appendValue(String filePath, String sheetName, String value) {
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") + File.separator + filePath);
        }

        log.info("[ExcelUtils] Appending '{}' to sheet '{}' in: {}", value, sheetName, file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
                log.warn("[ExcelUtils] Sheet '{}' not found, using first sheet: '{}'", sheetName, sheet.getSheetName());
            }

            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(value);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            log.info("[ExcelUtils] Appended '{}' at row {}", value, nextRow);
        } catch (IOException e) {
            log.error("[ExcelUtils] Failed to append to Excel: {}", e.getMessage());
            throw new RuntimeException("Excel append failed: " + e.getMessage(), e);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }
}
