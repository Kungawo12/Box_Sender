package com.boxsender.recipients;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Recipient Entity Class
 *
 * This class represents a package recipient in the Box Sender system.
 * Recipients are people who receive packages that are logged in the system.
 * Mapped to the "recipients" table in the database.
 *
 * When a package arrives, it is assigned to a recipient who will be notified
 * via email that their package is ready for pickup.
 *
 * Key features:
 * - Each recipient has a unique email address
 * - Recipients are organized by department
 * - Automatically tracks creation and update timestamps
 * - One recipient can receive multiple packages (one-to-many relationship)
 */
@Entity  // Marks this class as a JPA entity
@Table(name = "recipients")  // Maps to "recipients" table in database
public class Recipient {

    // Primary key - unique identifier for each recipient
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    // Recipient's first name - required field, max 45 characters
    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    // Recipient's last name - required field, max 45 characters
    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    // Recipient's email address - used for notifications
    // Must be unique and is required
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    // Department where the recipient works (e.g., "IT", "Marketing", "Sales")
    // Optional field, max 120 characters
    @Column(length = 120)
    private String department;

    // Timestamp when this recipient record was created
    // Automatically set to current time when record is inserted
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime createdAt;

    // Timestamp of last update to this recipient record
    // Automatically updated by database when record changes
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private java.time.LocalDateTime updatedAt;

    // --- Constructors ---

    /**
     * Default constructor required by JPA
     */
    public Recipient() {}

    /**
     * Constructor to create a new recipient with all essential information
     * @param firstName the recipient's first name
     * @param lastName the recipient's last name
     * @param email the recipient's email address for notifications
     * @param department the department where the recipient works
     */
    public Recipient(String firstName, String lastName, String email, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }

    // --- Getter and Setter Methods ---
    // Provide controlled access to private fields following JavaBean conventions

    /**
     * Gets the recipient's unique identifier
     * @return the recipient ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the recipient's unique identifier
     * @param id the recipient ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the recipient's first name
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the recipient's first name
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the recipient's last name
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the recipient's last name
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the recipient's email address
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the recipient's email address
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the recipient's department
     * @return the department name
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets the recipient's department
     * @param department the department name to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Gets the timestamp when this recipient was created
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
     * Gets the timestamp of the last update to this recipient
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