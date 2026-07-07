package com.mybharat.utils;

import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcel {
    public static void main(String[] args) throws Exception {
        String path = "resources/Partner_prod.xlsx";
        try (FileInputStream fis = new FileInputStream(new File(path));
             Workbook workbook = new XSSFWorkbook(fis)) {
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                System.out.println("=== Sheet: " + sheet.getSheetName() + " ===");
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    StringBuilder sb = new StringBuilder();
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            sb.append(cell.toString()).append("\t");
                        }
                    }
                    System.out.println(sb.toString().trim());
                }
            }
        }
    }
}
