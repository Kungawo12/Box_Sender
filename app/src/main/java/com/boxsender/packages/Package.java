package com.boxsender.packages;

import com.boxsender.recipients.Recipient;
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
 * Package Entity Class
 *
 * This class represents a physical package in the Box Sender tracking system.
 * It stores all information about packages that arrive and need to be picked up.
 * Mapped to the "packages" table in the database.
 *
 * Package Lifecycle:
 * 1. Employee logs a new package with status "received"
 * 2. System sends email notification to the recipient
 * 3. Recipient picks up the package
 * 4. Employee marks the package status as "picked"
 *
 * Database Relationships:
 * - Many packages can belong to one recipient (ManyToOne)
 * - Many packages can be logged by one employee (ManyToOne)
 *
 * Key features:
 * - Unique tracking numbers to identify each package
 * - Carrier information (UPS, FedEx, USPS, etc.)
 * - Status tracking (received/picked up)
 * - Automatic timestamp management
 */
@Entity  // Marks this class as a JPA entity
@Table(name = "packages")  // Maps to "packages" table in database
public class Package {

    // Primary key - unique identifier for each package
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    // Tracking number from the carrier - must be unique
    // Used to identify the specific package
    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    // Name of the shipping carrier (e.g., "UPS", "FedEx", "USPS", "DHL")
    // Required field, max 45 characters
    @Column(nullable = false, length = 45)
    private String carrier;

    // Additional description or notes about the package
    // Optional field, can hold large text (TEXT type in database)
    @Column(columnDefinition = "TEXT")
    private String description;

    // Current status of the package
    // Values: "received" (package logged and waiting) or "picked" (recipient collected it)
    @Column(length = 20)
    private String status;

    // Auto-generated pickup verification code
    // A 6-character alphanumeric code sent to the recipient for secure package pickup
    // Example: "A7K2M9"
    @Column(name = "pickup_code", length = 10)
    private String pickupCode;

    // The person who will receive this package
    // ManyToOne relationship: many packages can belong to one recipient
    // Foreign key relationship with recipients table
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_packages_recipients"))
    private Recipient recipient;

    // The employee who logged this package into the system
    // ManyToOne relationship: many packages can be logged by one employee
    // Foreign key relationship with employees table
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_packages_employees"))
    private Employee employee;

    // Timestamp when this package was logged into the system
    // Automatically set to current time when record is created
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime createdAt;

    // Timestamp of last update to this package record
    // Automatically updated by database when any field changes
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private java.time.LocalDateTime updatedAt;

    // --- Constructors ---

    /**
     * Default constructor required by JPA
     */
    public Package() {}

    /**
     * Constructor to create a new package with all essential information
     * Sets the initial status to "received" and creation timestamp to now
     *
     * @param trackingNumber the carrier's tracking number for this package
     * @param carrier the name of the shipping carrier
     * @param description optional description or notes about the package
     * @param recipient the person who will receive this package
     * @param employee the employee who is logging this package
     */
    public Package(String trackingNumber, String carrier, String description,
                        Recipient recipient, Employee employee) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.description = description;
        this.recipient = recipient;
        this.employee = employee;
        this.status = "received";  // Default status when package arrives
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }

    // --- Getter and Setter Methods ---
    // Provide controlled access to private fields following JavaBean conventions

    /**
     * Gets the package's unique identifier
     * @return the package ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the package's unique identifier
     * @param id the package ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the carrier's tracking number for this package
     * @return the tracking number
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * Sets the tracking number for this package
     * @param trackingNumber the tracking number to set
     */
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    /**
     * Gets the name of the shipping carrier
     * @return the carrier name (e.g., "UPS", "FedEx")
     */
    public String getCarrier() {
        return carrier;
    }

    /**
     * Sets the shipping carrier name
     * @param carrier the carrier name to set
     */
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    /**
     * Gets the description or notes about this package
     * @return the package description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description or notes about this package
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the current status of the package
     * @return the status ("received" or "picked")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the package status
     * @param status the status to set ("received" or "picked")
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the pickup verification code
     * @return the 6-character pickup code
     */
    public String getPickupCode() {
        return pickupCode;
    }

    /**
     * Sets the pickup verification code
     * @param pickupCode the pickup code to set
     */
    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    /**
     * Gets the recipient who will receive this package
     * @return the Recipient object
     */
    public Recipient getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient for this package
     * @param recipient the Recipient object to set
     */
    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    /**
     * Gets the employee who logged this package
     * @return the Employee object
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Sets the employee who logged this package
     * @param employee the Employee object to set
     */
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    /**
     * Gets the timestamp when this package was created
     * @return the creation timestamp
     */
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp of the last update
     * @return the update timestamp
     */
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the update timestamp
     * @param updatedAt the update timestamp to set
     */
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}