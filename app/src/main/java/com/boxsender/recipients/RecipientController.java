package com.boxsender.recipients;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recipient Controller
 *
 * This REST controller handles HTTP requests related to recipient management.
 * It provides API endpoints for CRUD operations on recipients.
 *
 * Base URL: /api/recipients
 *
 * Key responsibilities:
 * - List all recipients
 * - Get individual recipient details
 * - Create new recipients
 * - Update recipient information
 * - Delete recipients
 */
@RestController
@RequestMapping("/api/recipients")
public class RecipientController {

    private final RecipientRepository recipientRepo;

    /**
     * Constructor-based Dependency Injection
     * @param recipientRepo repository for recipient database operations
     */
    public RecipientController(RecipientRepository recipientRepo) {
        this.recipientRepo = recipientRepo;
    }

    /**
     * Get all recipients
     *
     * HTTP Endpoint: GET /api/recipients
     * Authentication: Required
     *
     * @param auth the authentication object
     * @return ResponseEntity with list of all recipients
     */
    @GetMapping
    public ResponseEntity<?> getAllRecipients(Authentication auth) {
        try {
            List<Recipient> recipients = recipientRepo.findAll();
            return ResponseEntity.ok(recipients);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve recipients: " + e.getMessage()));
        }
    }

    /**
     * Get a specific recipient by ID
     *
     * HTTP Endpoint: GET /api/recipients/{id}
     * Authentication: Required
     *
     * @param id the recipient ID
     * @param auth the authentication object
     * @return ResponseEntity with recipient data or error
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipient(@PathVariable Long id, Authentication auth) {
        try {
            Recipient recipient = recipientRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found with ID: " + id));

            return ResponseEntity.ok(recipient);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve recipient: " + e.getMessage()));
        }
    }

    /**
     * Create a new recipient
     *
     * HTTP Endpoint: POST /api/recipients
     * Authentication: Required
     *
     * @param body the request body containing recipient details
     * @param auth the authentication object
     * @return ResponseEntity with created recipient or error
     */
    @PostMapping
    public ResponseEntity<?> createRecipient(@RequestBody CreateRecipientRequest body,
                                              Authentication auth) {
        try {
            // Check if email already exists
            if (recipientRepo.findByEmail(body.email()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Recipient with this email already exists"));
            }

            // Create new recipient
            Recipient recipient = new Recipient(
                body.firstName(),
                body.lastName() != null ? body.lastName() : "",
                body.email(),
                body.department()
            );

            Recipient savedRecipient = recipientRepo.save(recipient);

            return ResponseEntity.ok(Map.of(
                "message", "Recipient created successfully",
                "recipient", savedRecipient
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to create recipient: " + e.getMessage()));
        }
    }

    /**
     * Update an existing recipient
     *
     * HTTP Endpoint: PUT /api/recipients/{id}
     * Authentication: Required
     *
     * @param id the recipient ID
     * @param body the request body containing updated recipient details
     * @param auth the authentication object
     * @return ResponseEntity with updated recipient or error
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipient(@PathVariable Long id,
                                              @RequestBody UpdateRecipientRequest body,
                                              Authentication auth) {
        try {
            // Find existing recipient
            Recipient recipient = recipientRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found with ID: " + id));

            // Update fields if provided
            if (body.firstName() != null && !body.firstName().isEmpty()) {
                recipient.setFirstName(body.firstName());
            }

            if (body.lastName() != null && !body.lastName().isEmpty()) {
                recipient.setLastName(body.lastName());
            }

            if (body.email() != null && !body.email().isEmpty()) {
                // Check if new email is already used by another recipient
                recipientRepo.findByEmail(body.email()).ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Email already in use by another recipient");
                    }
                });
                recipient.setEmail(body.email());
            }

            if (body.department() != null) {
                recipient.setDepartment(body.department());
            }

            // Update timestamp
            recipient.setUpdatedAt(java.time.LocalDateTime.now());

            // Save changes
            Recipient updatedRecipient = recipientRepo.save(recipient);

            return ResponseEntity.ok(Map.of(
                "message", "Recipient updated successfully",
                "recipient", updatedRecipient
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to update recipient: " + e.getMessage()));
        }
    }

    /**
     * Delete a recipient
     *
     * HTTP Endpoint: DELETE /api/recipients/{id}
     * Authentication: Required
     *
     * Note: This is a hard delete. In production, you might want to implement
     * soft delete to preserve data integrity if there are packages associated
     * with this recipient.
     *
     * @param id the recipient ID
     * @param auth the authentication object
     * @return ResponseEntity with success message or error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipient(@PathVariable Long id, Authentication auth) {
        try {
            // Check if recipient exists
            if (!recipientRepo.existsById(id)) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Recipient not found with ID: " + id));
            }

            // Delete recipient
            recipientRepo.deleteById(id);

            return ResponseEntity.ok(Map.of(
                "message", "Recipient deleted successfully",
                "recipientId", id
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to delete recipient: " + e.getMessage()));
        }
    }

    /**
     * Data Transfer Object for creating a recipient
     *
     * @param firstName recipient's first name (required)
     * @param lastName recipient's last name (optional)
     * @param email recipient's email address (required, must be unique)
     * @param department recipient's department (optional)
     */
    public record CreateRecipientRequest(
        String firstName,
        String lastName,
        String email,
        String department
    ) {}

    /**
     * Data Transfer Object for updating a recipient
     * All fields are optional - only provided fields will be updated
     *
     * @param firstName recipient's first name
     * @param lastName recipient's last name
     * @param email recipient's email address
     * @param department recipient's department
     */
    public record UpdateRecipientRequest(
        String firstName,
        String lastName,
        String email,
        String department
    ) {}
}
