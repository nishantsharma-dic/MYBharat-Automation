import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class WriteExcel {
    public static void main(String[] args) throws Exception {
        String path = "resources/Partner_prod.xlsx";
        FileInputStream fis = new FileInputStream(path);
        Workbook wb = new XSSFWorkbook(fis);
        fis.close();
        
        Sheet sheet = wb.getSheetAt(0);
        // Clear row 1 and write new email
        Row row = sheet.getRow(1);
        if (row == null) row = sheet.createRow(1);
        Cell cell = row.getCell(0);
        if (cell == null) cell = row.createCell(0);
        cell.setCellValue("create_org_09@yopmail.com");
        
        FileOutputStream fos = new FileOutputStream(path);
        wb.write(fos);
        fos.close();
        wb.close();
        System.out.println("Done - updated email to: create_org_09@yopmail.com");
    }
}
