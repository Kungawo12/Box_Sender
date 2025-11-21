package com.boxsender.packages;

import java.time.LocalDateTime;

import com.boxsender.recipients.Recipient;
import com.boxsender.users.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity             //database table
@Table(name = "packages")
public class Package {

    @Id         //Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //Auto-increment
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Column(nullable = false, length = 45)
    private String carrier;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String status = "received"; // "received" or "picked_up" (Default value)

    // Relationship: Many packages → One recipient
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    // Relationship: Many packages → One employee (who logged it)
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pickup information
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "picked_up_by", length = 200)
    private String pickedUpBy;

    @Column(name = "signature", length = 500)
    private String signature;  // Person signs their name

    // --- CONSTRUCTORS ---
    
    public Package() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "received";
    }

    // Constructor with parameters
    public Package(String trackingNumber, String carrier, String description, 
                Recipient recipient, Employee employee) {
        this();     // Call default constructor first
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.description = description;
        this.recipient = recipient;
        this.employee = employee;
    }

    /**
     * Mark package as picked up
     * @param pickedUpBy - Name of person who picked it up
     * @param signature - Their signature
     * This is not just a setter - it updates multiple fields!
     */
    public void markAsPickedUp(String pickedUpBy, String signature) {
        this.status = "picked_up";
        this.pickedUpAt = LocalDateTime.now();
        this.pickedUpBy = pickedUpBy;
        this.signature = signature;
        this.updatedAt = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Recipient getRecipient() { return recipient; }
    public void setRecipient(Recipient recipient) { this.recipient = recipient; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public String getPickedUpBy() { return pickedUpBy; }
    public void setPickedUpBy(String pickedUpBy) { this.pickedUpBy = pickedUpBy; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }


    /**
     * Lifecycle callback: Called before updating database
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}