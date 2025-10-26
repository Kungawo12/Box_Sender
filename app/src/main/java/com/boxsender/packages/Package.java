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

@Entity
@Table(name = "packages")
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Column(nullable = false, length = 45)
    private String carrier;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 12)
    private String status; // e.g., "logged", "picked"

    @Column(name = "pickup_code", length = 12)
    private String pickupCode;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_packages_recipients"))
    private Recipient recipient;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_packages_employees"))
    private Employee employee;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private java.time.LocalDateTime updatedAt;

    // --- Constructors ---
    public Package() {}

    public Package(String trackingNumber, String carrier, String description, 
                        Recipient recipient, Employee employee) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.description = description;
        this.recipient = recipient;
        this.employee = employee;
        this.status = "logged";
        this.pickupCode = generatePickupCode();
        this.createdAt = java.time.LocalDateTime.now();
    }

    // Helper method to generate a random pickup code
    private String generatePickupCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getPickupCode() {
        return pickupCode;
    }
    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public Recipient getRecipient() {
        return recipient;
    }
    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Employee getEmployee() {
        return employee;
    }
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}