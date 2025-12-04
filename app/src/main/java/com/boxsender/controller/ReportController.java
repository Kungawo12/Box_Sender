package com.boxsender.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generate Package Summary Report as PDF
     * MAILROOM STAFF ONLY
     */
    @PreAuthorize("hasRole('MAILROOM_STAFF')")  // ✅ ONLY MAILROOM STAFF
    @GetMapping("/summary/pdf")
    public ResponseEntity<?> generateSummaryPDF(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        try {
            LocalDateTime start = parseDate(startDate, LocalDate.now().minusMonths(1));
            LocalDateTime end = parseDate(endDate, LocalDate.now().plusDays(1));

            byte[] pdfBytes = reportService.generatePackageSummaryPDF(start, end);

            String filename = "Package_Summary_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);

        } catch (Exception e) {
            System.err.println("=== PDF Generation Error ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate PDF: " + e.getMessage()));
        }
    }

    /**
     * Generate Package Summary Report as Excel
     * MAILROOM STAFF ONLY
     */
    @PreAuthorize("hasRole('MAILROOM_STAFF')")  // ✅ ONLY MAILROOM STAFF
    @GetMapping("/summary/excel")
    public ResponseEntity<?> generateSummaryExcel(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        try {
            LocalDateTime start = parseDate(startDate, LocalDate.now().minusMonths(1));
            LocalDateTime end = parseDate(endDate, LocalDate.now().plusDays(1));

            byte[] excelBytes = reportService.generatePackageSummaryExcel(start, end);

            String filename = "Package_Summary_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";

            ByteArrayResource resource = new ByteArrayResource(excelBytes);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(resource);

        } catch (Exception e) {
            System.err.println("=== Excel Generation Error ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate Excel: " + e.getMessage()));
        }
    }

    /**
     * Helper method to parse date string
     */
    private LocalDateTime parseDate(String dateStr, LocalDate defaultDate) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultDate.atStartOfDay();
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return date.atStartOfDay();
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + dateStr + ", using default: " + defaultDate);
            return defaultDate.atStartOfDay();
        }
    }
}