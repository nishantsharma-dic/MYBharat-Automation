import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class ReadExcel {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("resources/Partner_prod.xlsx");
        Workbook wb = new XSSFWorkbook(fis);
        System.out.println("Number of sheets: " + wb.getNumberOfSheets());
        for (int s = 0; s < wb.getNumberOfSheets(); s++) {
            Sheet sheet = wb.getSheetAt(s);
            System.out.println("\nSheet: '" + sheet.getSheetName() + "' (lastRow=" + sheet.getLastRowNum() + ")");
            for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 10); i++) {
                Row row = sheet.getRow(i);
                if (row == null) { System.out.println("  Row " + i + ": null"); continue; }
                StringBuilder sb = new StringBuilder("  Row " + i + ": ");
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) sb.append("[null] ");
                    else sb.append("[" + cell.toString() + "] ");
                }
                System.out.println(sb.toString());
            }
        }
        wb.close();
        fis.close();
    }
}
