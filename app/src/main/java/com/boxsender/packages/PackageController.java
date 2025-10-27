package com.boxsender.packages;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * POST /api/packages - Log a new package
     * Takes full name, parses into first/last, creates recipient, saves package, sends email
     */
    @PostMapping
    public ResponseEntity<?> logPackage(@Valid @RequestBody LogPackageRequest body, 
                                        Authentication auth) {
        try {
            // 1. Get the currently logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // 2. Check if tracking number already exists
            if (packageRepo.findByTrackingNumber(body.trackingNumber()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tracking number already exists"));
            }

            String firstName = body.recipientName();
            String lastName = body.recipientNameecipient.LastName != null ? body.recipient.LastName : ""; // Last name is optional

            // 4. Find or create recipient
            Recipient recipient = recipientRepo.findByEmail(body.recipientEmail())
                .orElseGet(() -> {
                    Recipient newRecipient = new Recipient(
                        firstName,
                        lastName,
                        body.recipientEmail(),
                        null // department can be null for now
                    );
                    return recipientRepo.save(newRecipient);
                });

            // 5. Create and save the package
            Package pkg = new Package(
                body.trackingNumber(),
                body.carrier(),
                body.description(),
                recipient,
                employee
            );

            Package savedPackage = packageRepo.save(pkg);

            // 6. Send email notification to recipient (non-blocking)
            try {
                emailService.sendPackageNotification(
                    recipient.getEmail(),
                    recipient.getFirstName(),
                    savedPackage.getTrackingNumber(),
                    savedPackage.getPickupCode()
                );
            } catch (Exception e) {
                // Log but don't throw - package was already saved successfully
                System.err.println("Email notification failed but package was saved: " + e.getMessage());
            }

            // 7. Return success response with package details
            return ResponseEntity.ok(Map.of(
                "id", savedPackage.getId(),
                "trackingNumber", savedPackage.getTrackingNumber(),
                "pickupCode", savedPackage.getPickupCode(),
                "status", savedPackage.getStatus(),
                "recipientName", recipient.getFirstName() + " " + recipient.getLastName(),
                "message", "Package logged successfully. Notification email sent to " + recipient.getEmail()
            ));

        } catch (Exception e) {
            System.err.println("Error logging package: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to log package: " + e.getMessage()));
        }
    }

    /**
     * Request DTO for logging a package
     * Note: recipientName is a single full name field that gets parsed
     */
    public record LogPackageRequest(
        String trackingNumber,
        String carrier,
        String description,
        String recipientName,        // Full name (e.g., "John Smith")
        String recipientEmail
    ) {}
}