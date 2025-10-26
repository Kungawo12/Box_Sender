package com.boxsender.packages;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String trackingNumber;

    @Column(nullable = false, length = 50)
    private String carrier;

    @Column(length = 100)
    private String recipientFirstName;

    @Column(length = 100)
    private String recipientLastName;

    @Column(nullable = false, length = 200)
    private String recipientEmail;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true, length = 10)
    private String pickupCode;

    @Column(nullable = false, length = 20)
    private String status = "RECEIVED";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime pickedUpAt;

    @Column(nullable = false)
    private Long loggedByEmployeeId;

    @Column
    private Long pickedUpByEmployeeId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Package() {}

    public Package(String trackingNumber, String carrier, String recipientFirstName,
                   String recipientLastName, String recipientEmail, String description,
                   String pickupCode, Long loggedByEmployeeId) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.recipientFirstName = recipientFirstName;
        this.recipientLastName = recipientLastName;
        this.recipientEmail = recipientEmail;
        this.description = description;
        this.pickupCode = pickupCode;
        this.loggedByEmployeeId = loggedByEmployeeId;
    }

    // Getters and Setters
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

    public String getRecipientFirstName() {
        return recipientFirstName;
    }

    public void setRecipientFirstName(String recipientFirstName) {
        this.recipientFirstName = recipientFirstName;
    }

    public String getRecipientLastName() {
        return recipientLastName;
    }

    public void setRecipientLastName(String recipientLastName) {
        this.recipientLastName = recipientLastName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public Long getLoggedByEmployeeId() {
        return loggedByEmployeeId;
    }

    public void setLoggedByEmployeeId(Long loggedByEmployeeId) {
        this.loggedByEmployeeId = loggedByEmployeeId;
    }

    public Long getPickedUpByEmployeeId() {
        return pickedUpByEmployeeId;
    }

    public void setPickedUpByEmployeeId(Long pickedUpByEmployeeId) {
        this.pickedUpByEmployeeId = pickedUpByEmployeeId;
    }
}
