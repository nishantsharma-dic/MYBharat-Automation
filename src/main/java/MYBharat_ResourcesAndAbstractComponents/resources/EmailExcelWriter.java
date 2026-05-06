package MYBharat_ResourcesAndAbstractComponents.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utility class that appends generated test emails to an Excel file for
 * post-run traceability.
 *
 * The output file path is resolved relative to the project root so the file
 * works on any machine (no hardcoded user-home paths).
 */
public class EmailExcelWriter {

    private static final Logger log = LogManager.getLogger(EmailExcelWriter.class);

    /**
     * Path relative to the Maven project root.
     * Override at runtime with -DemailsFile=<absolute-path> if needed.
     */
    private static final String DEFAULT_FILE_PATH =
        System.getProperty("user.dir") + "/test-output/emails.xlsx";

    private EmailExcelWriter() {
        // utility class – no instances
    }

    /**
     * Appends {@code email} to the next available row in the tracking Excel file.
     * Creates the file (and parent directories) if they do not exist.
     *
     * @param email email address to record
     */
    public static void appendEmail(String email) {
        String filePath = System.getProperty("emailsFile", DEFAULT_FILE_PATH);
        File file = new File(filePath);

        // Ensure parent directories exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        Workbook workbook = null;
        try {
            Sheet sheet;
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                }
                sheet = workbook.getSheetAt(0);
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Emails");
                // Header row
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Email");
            }

            int nextRowNum = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRowNum);
            Cell cell = row.createCell(0);
            cell.setCellValue(email);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            log.info("Email recorded to {}: {}", filePath, email);

        } catch (IOException e) {
            log.error("Failed to write email to Excel: {}", e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.warn("Failed to close workbook: {}", e.getMessage());
                }
            }
        }
    }
}
