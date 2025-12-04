package com.boxsender.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;  // 
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.boxsender.packages.Package;
import com.boxsender.packages.PackageRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class ReportService {

    private final PackageRepository packageRepo;

    public ReportService(PackageRepository packageRepo) {
        this.packageRepo = packageRepo;
    }

    /**
     * Generate Package Summary Report as PDF
     */
    public byte[] generatePackageSummaryPDF(LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        // Get packages in date range
        List<Package> packages = packageRepo.findAll().stream()
            .filter(p -> p.getCreatedAt().isAfter(startDate) && p.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());

        // Create PDF document
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        
        document.open();

        // Title (use iText Font for PDF)
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        Paragraph title = new Paragraph("Package Summary Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Date range
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Paragraph dateRange = new Paragraph(
            "Date Range: " + startDate.format(formatter) + " to " + endDate.format(formatter),
            normalFont
        );
        dateRange.setAlignment(Element.ALIGN_CENTER);
        document.add(dateRange);
        document.add(new Paragraph(" ")); // Spacer

        // Summary statistics
        long totalPackages = packages.size();
        long receivedPackages = packages.stream().filter(p -> "received".equals(p.getStatus())).count();
        long pickedUpPackages = packages.stream().filter(p -> "picked_up".equals(p.getStatus())).count();

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(50);
        addCell(statsTable, "Total Packages:", true);
        addCell(statsTable, String.valueOf(totalPackages), false);
        addCell(statsTable, "Ready for Pickup:", true);
        addCell(statsTable, String.valueOf(receivedPackages), false);
        addCell(statsTable, "Picked Up:", true);
        addCell(statsTable, String.valueOf(pickedUpPackages), false);
        document.add(statsTable);
        document.add(new Paragraph(" "));

        // Packages by carrier
        Map<String, Long> packagesByCarrier = packages.stream()
            .collect(Collectors.groupingBy(Package::getCarrier, Collectors.counting()));

        Paragraph carrierTitle = new Paragraph("Packages by Carrier", 
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD));
        document.add(carrierTitle);
        
        PdfPTable carrierTable = new PdfPTable(2);
        carrierTable.setWidthPercentage(50);
        addCell(carrierTable, "Carrier", true);
        addCell(carrierTable, "Count", true);
        packagesByCarrier.forEach((carrier, count) -> {
            addCell(carrierTable, carrier, false);
            addCell(carrierTable, String.valueOf(count), false);
        });
        document.add(carrierTable);
        document.add(new Paragraph(" "));

        // Package details table
        Paragraph detailsTitle = new Paragraph("Package Details", 
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD));
        document.add(detailsTitle);

        PdfPTable detailsTable = new PdfPTable(5);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{2, 1.5f, 2, 1.5f, 2});

        // Headers
        addCell(detailsTable, "Tracking #", true);
        addCell(detailsTable, "Carrier", true);
        addCell(detailsTable, "Recipient", true);
        addCell(detailsTable, "Status", true);
        addCell(detailsTable, "Date", true);

        // Data
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        for (Package pkg : packages) {
            addCell(detailsTable, pkg.getTrackingNumber(), false);
            addCell(detailsTable, pkg.getCarrier(), false);
            addCell(detailsTable, pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName(), false);
            addCell(detailsTable, pkg.getStatus(), false);
            addCell(detailsTable, pkg.getCreatedAt().format(dateFormatter), false);
        }
        document.add(detailsTable);

        // Footer
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
            "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC)
        );
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    /**
     * Generate Package Summary Report as Excel
     */
    public byte[] generatePackageSummaryExcel(LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        System.out.println("=== Starting Excel generation ===");
        System.out.println("Start date: " + startDate);
        System.out.println("End date: " + endDate);
        
        try {
            List<Package> packages = packageRepo.findAll().stream()
                .filter(p -> p.getCreatedAt().isAfter(startDate) && p.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());
            
            System.out.println("Found " + packages.size() + " packages");

            Workbook workbook = new XSSFWorkbook();
            System.out.println("Workbook created successfully");
            
            // Summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            int rowNum = 0;

            // Title (use POI Font for Excel)
            Row titleRow = summarySheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Package Summary Report");
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();  // 
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Date range
            rowNum++;
            Row dateRow = summarySheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Date Range:");
            dateRow.createCell(1).setCellValue(startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
                                              " to " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            // Statistics
            rowNum++;
            long totalPackages = packages.size();
            long receivedPackages = packages.stream().filter(p -> "received".equals(p.getStatus())).count();
            long pickedUpPackages = packages.stream().filter(p -> "picked_up".equals(p.getStatus())).count();

            Row statRow1 = summarySheet.createRow(rowNum++);
            statRow1.createCell(0).setCellValue("Total Packages:");
            statRow1.createCell(1).setCellValue(totalPackages);

            Row statRow2 = summarySheet.createRow(rowNum++);
            statRow2.createCell(0).setCellValue("Ready for Pickup:");
            statRow2.createCell(1).setCellValue(receivedPackages);

            Row statRow3 = summarySheet.createRow(rowNum++);
            statRow3.createCell(0).setCellValue("Picked Up:");
            statRow3.createCell(1).setCellValue(pickedUpPackages);

            // Packages by carrier
            rowNum++;
            Row carrierHeaderRow = summarySheet.createRow(rowNum++);
            carrierHeaderRow.createCell(0).setCellValue("Carrier");
            carrierHeaderRow.createCell(1).setCellValue("Count");

            Map<String, Long> packagesByCarrier = packages.stream()
                .collect(Collectors.groupingBy(Package::getCarrier, Collectors.counting()));

            for (Map.Entry<String, Long> entry : packagesByCarrier.entrySet()) {
                Row carrierRow = summarySheet.createRow(rowNum++);
                carrierRow.createCell(0).setCellValue(entry.getKey());
                carrierRow.createCell(1).setCellValue(entry.getValue());
            }

            // Details sheet
            Sheet detailsSheet = workbook.createSheet("Package Details");
            rowNum = 0;

            // Header row
            Row headerRow = detailsSheet.createRow(rowNum++);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();  // 
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Tracking #", "Carrier", "Recipient Name", "Recipient Email", "Description", "Status", "Created At", "Picked Up At", "Picked Up By"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            for (Package pkg : packages) {
                Row row = detailsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(pkg.getTrackingNumber());
                row.createCell(1).setCellValue(pkg.getCarrier());
                
                // Add null check for recipient
                if (pkg.getRecipient() != null) {
                    row.createCell(2).setCellValue(pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName());
                    row.createCell(3).setCellValue(pkg.getRecipient().getEmail());
                } else {
                    row.createCell(2).setCellValue("Unknown");
                    row.createCell(3).setCellValue("");
                }
                
                row.createCell(4).setCellValue(pkg.getDescription() != null ? pkg.getDescription() : "");
                row.createCell(5).setCellValue(pkg.getStatus());
                row.createCell(6).setCellValue(pkg.getCreatedAt().format(dateFormatter));
                row.createCell(7).setCellValue(pkg.getPickedUpAt() != null ? pkg.getPickedUpAt().format(dateFormatter) : "");
                row.createCell(8).setCellValue(pkg.getPickedUpBy() != null ? pkg.getPickedUpBy() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                detailsSheet.autoSizeColumn(i);
                if (i < 2) {  // Only first 2 columns of summary
                    summarySheet.autoSizeColumn(i);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();
            
            System.out.println("Excel generated successfully, size: " + out.size() + " bytes");
            return out.toByteArray();
            
        } catch (Exception e) {
            System.err.println("ERROR in Excel generation: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Helper method to add cell to PDF table
     */
    private void addCell(PdfPTable table, String text, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 
                                    isHeader ? 10 : 9, 
                                    isHeader ? com.itextpdf.text.Font.BOLD : com.itextpdf.text.Font.NORMAL)));
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        cell.setPadding(5);
        table.addCell(cell);
    }
}