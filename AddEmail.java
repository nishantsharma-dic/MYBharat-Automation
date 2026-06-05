import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AddEmail {
    public static void main(String[] args) throws Exception {
        String path = "resources/VO_prod.xlsx";
        FileInputStream fis = new FileInputStream(path);
        Workbook wb = new XSSFWorkbook(fis);
        fis.close();
        Sheet sheet = wb.getSheetAt(0);
        int nextRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(nextRow);
        row.createCell(0).setCellValue("mayanksinghrana@yopmail.com");
        FileOutputStream fos = new FileOutputStream(path);
        wb.write(fos);
        fos.close();
        wb.close();
        System.out.println("Done - added mayanksinghrana@yopmail.com to row " + nextRow);
    }
}
