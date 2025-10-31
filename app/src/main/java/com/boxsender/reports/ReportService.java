package com.boxsender.reports;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.boxsender.packages.Package;
import com.boxsender.packages.PackageRepository;
import com.boxsender.recipients.Recipient;
import com.boxsender.recipients.RecipientRepository;
import com.boxsender.users.Employee;

/**
 * Report Service
 *
 * This service handles the generation of various types of reports
 * for the Box Sender system.
 *
 * Report types supported:
 * - Daily package log
 * - Overdue packages
 * - Recipient history
 * - Summary statistics
 */
@Service
public class ReportService {

    private final ReportRepository reportRepo;
    private final PackageRepository packageRepo;
    private final RecipientRepository recipientRepo;

    /**
     * Constructor-based Dependency Injection
     */
    public ReportService(ReportRepository reportRepo,
                          PackageRepository packageRepo,
                          RecipientRepository recipientRepo) {
        this.reportRepo = reportRepo;
        this.packageRepo = packageRepo;
        this.recipientRepo = recipientRepo;
    }

    /**
     * Generate a daily package log report
     *
     * This report shows all packages logged on a specific date.
     *
     * @param date the date to generate the report for
     * @param employee the employee generating the report
     * @return the generated Report object
     */
    public Report generateDailyLog(LocalDate date, Employee employee) {
        // Get all packages
        List<Package> allPackages = packageRepo.findAll();

        // Filter packages for the specific date
        List<Package> packagesForDate = allPackages.stream()
            .filter(pkg -> pkg.getCreatedAt() != null &&
                           pkg.getCreatedAt().toLocalDate().equals(date))
            .toList();

        // Build report data as CSV-style text
        StringBuilder reportData = new StringBuilder();
        reportData.append("Tracking Number,Carrier,Recipient Name,Recipient Email,Status,Logged At\n");

        for (Package pkg : packagesForDate) {
            Recipient recipient = pkg.getRecipient();
            reportData.append(String.format("%s,%s,%s %s,%s,%s,%s\n",
                pkg.getTrackingNumber(),
                pkg.getCarrier(),
                recipient != null ? recipient.getFirstName() : "Unknown",
                recipient != null ? recipient.getLastName() : "",
                recipient != null ? recipient.getEmail() : "",
                pkg.getStatus(),
                pkg.getCreatedAt()
            ));
        }

        // Create and save report
        Report report = new Report(
            "DAILY",
            "Daily Package Log - " + date,
            employee,
            reportData.toString()
        );
        report.setDateRange(date.toString());
        report.setRecordCount(packagesForDate.size());

        return reportRepo.save(report);
    }

    /**
     * Generate an overdue packages report
     *
     * This report shows all packages that are still in "received" status
     * and are older than a specified number of days.
     *
     * @param daysThreshold packages older than this many days are considered overdue
     * @param employee the employee generating the report
     * @return the generated Report object
     */
    public Report generateOverdueReport(int daysThreshold, Employee employee) {
        // Calculate the cutoff date
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);

        // Get all packages with "received" status
        List<Package> receivedPackages = packageRepo.findByStatus("received");

        // Filter for packages older than threshold
        List<Package> overduePackages = receivedPackages.stream()
            .filter(pkg -> pkg.getCreatedAt() != null &&
                           pkg.getCreatedAt().isBefore(cutoffDate))
            .toList();

        // Build report data
        StringBuilder reportData = new StringBuilder();
        reportData.append(String.format("Overdue Packages Report (older than %d days)\n\n", daysThreshold));
        reportData.append("Tracking Number,Carrier,Recipient Name,Recipient Email,Days Waiting,Logged At\n");

        for (Package pkg : overduePackages) {
            Recipient recipient = pkg.getRecipient();
            long daysWaiting = java.time.temporal.ChronoUnit.DAYS.between(
                pkg.getCreatedAt(),
                LocalDateTime.now()
            );

            reportData.append(String.format("%s,%s,%s %s,%s,%d,%s\n",
                pkg.getTrackingNumber(),
                pkg.getCarrier(),
                recipient != null ? recipient.getFirstName() : "Unknown",
                recipient != null ? recipient.getLastName() : "",
                recipient != null ? recipient.getEmail() : "",
                daysWaiting,
                pkg.getCreatedAt()
            ));
        }

        // Create and save report
        Report report = new Report(
            "OVERDUE",
            String.format("Overdue Packages (>%d days)", daysThreshold),
            employee,
            reportData.toString()
        );
        report.setRecordCount(overduePackages.size());

        return reportRepo.save(report);
    }

    /**
     * Generate a recipient history report
     *
     * This report shows all packages for a specific recipient.
     *
     * @param recipientEmail the email of the recipient
     * @param employee the employee generating the report
     * @return the generated Report object
     */
    public Report generateRecipientHistory(String recipientEmail, Employee employee) {
        // Find recipient
        Recipient recipient = recipientRepo.findByEmail(recipientEmail)
            .orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientEmail));

        // Get all packages for this recipient
        List<Package> packages = packageRepo.findByRecipientId(recipient.getId());

        // Build report data
        StringBuilder reportData = new StringBuilder();
        reportData.append(String.format("Package History for: %s %s (%s)\n\n",
            recipient.getFirstName(),
            recipient.getLastName(),
            recipient.getEmail()
        ));
        reportData.append("Tracking Number,Carrier,Description,Status,Logged At,Picked Up At\n");

        for (Package pkg : packages) {
            String pickedUpAt = "picked".equals(pkg.getStatus()) && pkg.getUpdatedAt() != null
                ? pkg.getUpdatedAt().toString()
                : "Not picked up";

            reportData.append(String.format("%s,%s,%s,%s,%s,%s\n",
                pkg.getTrackingNumber(),
                pkg.getCarrier(),
                pkg.getDescription() != null ? pkg.getDescription() : "",
                pkg.getStatus(),
                pkg.getCreatedAt(),
                pickedUpAt
            ));
        }

        // Create and save report
        Report report = new Report(
            "RECIPIENT_HISTORY",
            "Package History - " + recipient.getFirstName() + " " + recipient.getLastName(),
            employee,
            reportData.toString()
        );
        report.setRecordCount(packages.size());

        return reportRepo.save(report);
    }

    /**
     * Generate a summary statistics report
     *
     * This report provides overall statistics about the package system.
     *
     * @param employee the employee generating the report
     * @return the generated Report object
     */
    public Report generateSummaryReport(Employee employee) {
        // Gather statistics
        long totalPackages = packageRepo.count();
        long receivedPackages = packageRepo.countByStatus("received");
        long pickedPackages = packageRepo.countByStatus("picked");
        long totalRecipients = recipientRepo.count();

        // Calculate pickup rate
        double pickupRate = totalPackages > 0
            ? (double) pickedPackages / totalPackages * 100
            : 0;

        // Build report data
        StringBuilder reportData = new StringBuilder();
        reportData.append("Box Sender System Summary Report\n");
        reportData.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        reportData.append("=== Package Statistics ===\n");
        reportData.append("Total Packages: ").append(totalPackages).append("\n");
        reportData.append("Awaiting Pickup: ").append(receivedPackages).append("\n");
        reportData.append("Picked Up: ").append(pickedPackages).append("\n");
        reportData.append(String.format("Pickup Rate: %.1f%%\n", pickupRate));
        reportData.append("\n=== Recipient Statistics ===\n");
        reportData.append("Total Recipients: ").append(totalRecipients).append("\n");

        // Create and save report
        Report report = new Report(
            "SUMMARY",
            "System Summary Report",
            employee,
            reportData.toString()
        );
        report.setRecordCount((int) totalPackages);

        return reportRepo.save(report);
    }
}
