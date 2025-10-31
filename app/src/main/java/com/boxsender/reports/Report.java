package com.boxsender.reports;

import com.boxsender.users.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Report Entity Class
 *
 * This class represents a report in the Box Sender system.
 * Reports can be generated for various purposes such as daily logs,
 * overdue packages, and recipient history.
 * Mapped to the "reports" table in the database.
 *
 * Report Types:
 * - DAILY: Daily package log for a specific date
 * - OVERDUE: List of packages that haven't been picked up
 * - RECIPIENT_HISTORY: All packages for a specific recipient
 * - SUMMARY: Summary statistics report
 *
 * Key features:
 * - Tracks who generated the report
 * - Stores report data in JSON or text format
 * - Automatically records generation timestamp
 */
@Entity
@Table(name = "reports")
public class Report {

    // Primary key - unique identifier for each report
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type of report (e.g., "DAILY", "OVERDUE", "RECIPIENT_HISTORY", "SUMMARY")
    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    // Title or description of the report
    @Column(nullable = false, length = 200)
    private String title;

    // Date/time when this report was generated
    @Column(name = "generated_date", nullable = false)
    private java.time.LocalDateTime generatedDate;

    // The employee who generated this report
    @ManyToOne
    @JoinColumn(name = "generated_by", nullable = false, foreignKey = @ForeignKey(name = "fk_reports_employees"))
    private Employee generatedBy;

    // The actual report data stored as JSON or text
    // TEXT column can hold large amounts of data
    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;

    // Optional date range for the report (e.g., "2024-01-01 to 2024-01-31")
    @Column(name = "date_range", length = 100)
    private String dateRange;

    // Number of records included in this report
    @Column(name = "record_count")
    private Integer recordCount;

    // --- Constructors ---

    /**
     * Default constructor required by JPA
     */
    public Report() {}

    /**
     * Constructor to create a new report
     *
     * @param reportType the type of report
     * @param title the report title
     * @param generatedBy the employee who generated the report
     * @param reportData the report data content
     */
    public Report(String reportType, String title, Employee generatedBy, String reportData) {
        this.reportType = reportType;
        this.title = title;
        this.generatedBy = generatedBy;
        this.reportData = reportData;
        this.generatedDate = java.time.LocalDateTime.now();
        this.recordCount = 0;
    }

    // --- Getter and Setter Methods ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public java.time.LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(java.time.LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Employee getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(Employee generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }
}
