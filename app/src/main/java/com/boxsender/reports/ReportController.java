package com.boxsender.reports;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

/**
 * Report Controller
 *
 * This REST controller handles HTTP requests related to report generation.
 * It provides API endpoints for generating and retrieving various types of reports.
 *
 * Base URL: /api/reports
 *
 * Available reports:
 * - Daily package log
 * - Overdue packages
 * - Recipient history
 * - Summary statistics
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepo;
    private final EmployeeRepository employeeRepo;

    /**
     * Constructor-based Dependency Injection
     */
    public ReportController(ReportService reportService,
                             ReportRepository reportRepo,
                             EmployeeRepository employeeRepo) {
        this.reportService = reportService;
        this.reportRepo = reportRepo;
        this.employeeRepo = employeeRepo;
    }

    /**
     * Get all reports
     *
     * HTTP Endpoint: GET /api/reports
     * Authentication: Required
     *
     * @param auth the authentication object
     * @return ResponseEntity with list of all reports
     */
    @GetMapping
    public ResponseEntity<?> getAllReports(Authentication auth) {
        try {
            List<Report> reports = reportRepo.findAllByOrderByGeneratedDateDesc();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve reports: " + e.getMessage()));
        }
    }

    /**
     * Get a specific report by ID
     *
     * HTTP Endpoint: GET /api/reports/{id}
     * Authentication: Required
     *
     * @param id the report ID
     * @param auth the authentication object
     * @return ResponseEntity with report data
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id, Authentication auth) {
        try {
            Report report = reportRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));

            return ResponseEntity.ok(report);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve report: " + e.getMessage()));
        }
    }

    /**
     * Generate a daily package log report
     *
     * HTTP Endpoint: POST /api/reports/daily?date=YYYY-MM-DD
     * Authentication: Required
     *
     * @param date the date to generate the report for (format: YYYY-MM-DD)
     * @param auth the authentication object
     * @return ResponseEntity with the generated report
     */
    @PostMapping("/daily")
    public ResponseEntity<?> generateDailyLog(
        @RequestParam(required = false) String date,
        Authentication auth) {

        try {
            // Get logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Parse date or use today
            LocalDate reportDate = (date != null && !date.isEmpty())
                ? LocalDate.parse(date)
                : LocalDate.now();

            // Generate report
            Report report = reportService.generateDailyLog(reportDate, employee);

            return ResponseEntity.ok(Map.of(
                "message", "Daily log generated successfully",
                "report", report
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to generate daily log: " + e.getMessage()));
        }
    }

    /**
     * Generate an overdue packages report
     *
     * HTTP Endpoint: POST /api/reports/overdue?days=N
     * Authentication: Required
     *
     * @param days number of days threshold (default: 7)
     * @param auth the authentication object
     * @return ResponseEntity with the generated report
     */
    @PostMapping("/overdue")
    public ResponseEntity<?> generateOverdueReport(
        @RequestParam(required = false, defaultValue = "7") int days,
        Authentication auth) {

        try {
            // Get logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Generate report
            Report report = reportService.generateOverdueReport(days, employee);

            return ResponseEntity.ok(Map.of(
                "message", "Overdue report generated successfully",
                "report", report
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to generate overdue report: " + e.getMessage()));
        }
    }

    /**
     * Generate a recipient history report
     *
     * HTTP Endpoint: POST /api/reports/recipient?email=recipient@example.com
     * Authentication: Required
     *
     * @param email the recipient's email address
     * @param auth the authentication object
     * @return ResponseEntity with the generated report
     */
    @PostMapping("/recipient")
    public ResponseEntity<?> generateRecipientHistory(
        @RequestParam String email,
        Authentication auth) {

        try {
            // Get logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Generate report
            Report report = reportService.generateRecipientHistory(email, employee);

            return ResponseEntity.ok(Map.of(
                "message", "Recipient history generated successfully",
                "report", report
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to generate recipient history: " + e.getMessage()));
        }
    }

    /**
     * Generate a summary statistics report
     *
     * HTTP Endpoint: POST /api/reports/summary
     * Authentication: Required
     *
     * @param auth the authentication object
     * @return ResponseEntity with the generated report
     */
    @PostMapping("/summary")
    public ResponseEntity<?> generateSummaryReport(Authentication auth) {
        try {
            // Get logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Generate report
            Report report = reportService.generateSummaryReport(employee);

            return ResponseEntity.ok(Map.of(
                "message", "Summary report generated successfully",
                "report", report
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to generate summary report: " + e.getMessage()));
        }
    }
}
