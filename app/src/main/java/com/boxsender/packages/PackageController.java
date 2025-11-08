package com.boxsender.packages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.email.EmailService;
import com.boxsender.recipients.Recipient;
import com.boxsender.recipients.RecipientRepository;
import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageRepository packageRepo;
    private final RecipientRepository recipientRepo;
    private final EmployeeRepository employeeRepo;
    private final EmailService emailService;

    public PackageController(PackageRepository packageRepo, 
                            RecipientRepository recipientRepo,
                            EmployeeRepository employeeRepo,
                            EmailService emailService) {
        this.packageRepo = packageRepo;
        this.recipientRepo = recipientRepo;
        this.employeeRepo = employeeRepo;
        this.emailService = emailService;
    }

    /**
     * POST /api/packages - Log a new package when it's received
     */
    @PostMapping
    public ResponseEntity<?> logPackage(@Valid @RequestBody LogPackageRequest body, 
                                        Authentication auth) {
        try {
            // Get the currently logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Check if tracking number already exists
            if (packageRepo.findByTrackingNumber(body.trackingNumber()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tracking number already exists"));
            }

            // Get recipient names from request
            String firstName = body.recipientFirst();
            String lastName = body.recipientLast() != null ? body.recipientLast() : "";

            // Find or create recipient
            Recipient recipient = recipientRepo.findByEmail(body.recipientEmail())
                .orElseGet(() -> {
                    Recipient newRecipient = new Recipient(
                        firstName,
                        lastName,
                        body.recipientEmail(),
                        null
                    );
                    return recipientRepo.save(newRecipient);
                });

            // Create and save the package
            Package pkg = new Package(
                body.trackingNumber(),
                body.carrier(),
                body.description(),
                recipient,
                employee
            );

            Package savedPackage = packageRepo.save(pkg);

            // Send email notification to recipient
            try {
                emailService.sendPackageNotification(
                    recipient.getEmail(),
                    recipient.getFirstName(),
                    savedPackage.getTrackingNumber()
                );
            } catch (Exception e) {
                // Email failure is not critical - package is already saved
                System.err.println("Email notification failed: " + e.getMessage());
            }

            // Return success response
            return ResponseEntity.ok(Map.of(
                "id", savedPackage.getId(),
                "trackingNumber", savedPackage.getTrackingNumber(),
                "status", savedPackage.getStatus(),
                "recipientName", recipient.getFirstName() + " " + recipient.getLastName(),
                "message", "Package logged successfully. Notification email sent to " + recipient.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to log package: " + e.getMessage()));
        }
    }

    /**
     *  GET /api/packages/search - Search packages with filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPackages(
        @RequestParam(value = "tracking", required = false, defaultValue = "") String tracking,
        @RequestParam(value = "recipientName", required = false, defaultValue = "") String recipientName,
        @RequestParam(value = "status", required = false, defaultValue = "all") String status
    ) {
        // Use advanced search
        List<Package> packages = packageRepo.advancedSearch(
            tracking.isEmpty() ? null : tracking,
            recipientName.isEmpty() ? null : recipientName,
            status
        );

        // Convert to JSON format
        List<Map<String, Object>> response = packages.stream()
            .map(pkg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", pkg.getId());
                map.put("trackingNumber", pkg.getTrackingNumber());
                map.put("carrier", pkg.getCarrier());
                map.put("description", pkg.getDescription());
                map.put("recipientName", pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName());
                map.put("recipientEmail", pkg.getRecipient().getEmail());
                map.put("status", pkg.getStatus());
                map.put("createdAt", pkg.getCreatedAt().toString());
                map.put("pickedUpAt", pkg.getPickedUpAt() != null ? pkg.getPickedUpAt().toString() : null);
                map.put("pickedUpBy", pkg.getPickedUpBy());
                map.put("signature", pkg.getSignature());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
 *  POST /api/packages/pickup - Mark package as picked up
 * Body: { "trackingNumber": "ABC123", "pickedUpBy": "John Doe", "signature": "John Doe" }
 */
@PostMapping("/pickup")
public ResponseEntity<?> pickupPackage(@RequestBody Map<String, String> body) {
    String trackingNumber = body.get("trackingNumber");
    String pickedUpBy = body.get("pickedUpBy");
    String signature = body.get("signature");

    // Validate tracking number
    if (trackingNumber == null || trackingNumber.isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Tracking number is required"));
    }

    // Validate signature
    if (signature == null || signature.isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Signature is required"));
    }

    // Find package by tracking number only
    Optional<Package> pkgOpt = packageRepo.findByTrackingNumber(trackingNumber);

    if (pkgOpt.isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Package not found with tracking number: " + trackingNumber));
    }

    Package pkg = pkgOpt.get();

    // Check if already picked up
    if ("picked_up".equals(pkg.getStatus())) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Package already picked up on " + pkg.getPickedUpAt()));
    }

    // Mark as picked up with signature
    pkg.markAsPickedUp(
        pickedUpBy != null && !pickedUpBy.isEmpty() ? pickedUpBy : "Unknown",
        signature
    );
    packageRepo.save(pkg);

    // Return success
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Package picked up successfully!");
    response.put("trackingNumber", pkg.getTrackingNumber());
    response.put("pickedUpAt", pkg.getPickedUpAt().toString());
    response.put("pickedUpBy", pkg.getPickedUpBy());
    response.put("signature", pkg.getSignature());

    return ResponseEntity.ok(response);
}

    /**
     *  GET /api/activity - Get recent package activity
     */
    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
        @RequestParam(value = "limit", required = false, defaultValue = "100") int limit
    ) {
        // Get all packages, sorted by most recent first
        List<Package> packages = packageRepo.findAll();
        
        // Sort by created date (newest first)
        packages.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Limit results
        if (packages.size() > limit) {
            packages = packages.subList(0, limit);
        }
        
        // Convert to activity events
        List<Map<String, Object>> activities = new ArrayList<>();
        
        for (Package pkg : packages) {
            // Add "RECEIVED" event (when package was logged)
            Map<String, Object> receivedEvent = new HashMap<>();
            receivedEvent.put("action", "RECEIVED");
            receivedEvent.put("when", pkg.getCreatedAt().toString());
            receivedEvent.put("trackingNumber", pkg.getTrackingNumber());
            receivedEvent.put("recipient", pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName());
            String receivedDetails = "Carrier: " + pkg.getCarrier();
            if (pkg.getDescription() != null && !pkg.getDescription().isEmpty()) {
                receivedDetails += " - " + pkg.getDescription();
            }
            receivedEvent.put("details", receivedDetails);
        
            activities.add(receivedEvent);
            
            // Add "PICKED_UP" event (if package was picked up)
            if ("picked_up".equals(pkg.getStatus()) && pkg.getPickedUpAt() != null) {
                Map<String, Object> pickedUpEvent = new HashMap<>();
                pickedUpEvent.put("action", "PICKED_UP");
                pickedUpEvent.put("when", pkg.getPickedUpAt().toString());
                pickedUpEvent.put("trackingNumber", pkg.getTrackingNumber());
                pickedUpEvent.put("recipient", pkg.getRecipient().getFirstName() + " " + pkg.getRecipient().getLastName());
                String pickupDetails = "Picked up by: " + pkg.getPickedUpBy();
                if (pkg.getSignature() != null && !pkg.getSignature().isEmpty()) {
                pickupDetails += " (Signature: " + pkg.getSignature() + ")";
                }
                pickedUpEvent.put("details", pickupDetails);
            }
        }
        
        // Sort all activities by timestamp (newest first)
        activities.sort((a, b) -> {
            String timeA = (String) a.get("when");
            String timeB = (String) b.get("when");
            return timeB.compareTo(timeA);
        });
        
        // Limit final results
        if (activities.size() > limit) {
            activities = activities.subList(0, limit);
        }
        
        return ResponseEntity.ok(activities);
    }
    /**
     * DTO for logging packages
     */
    public record LogPackageRequest(
        String trackingNumber,
        String carrier,
        String description,
        String recipientFirst,
        String recipientLast,
        String recipientEmail
    ) {}
}
