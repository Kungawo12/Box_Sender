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

    public record LogPackageRequest(
        String trackingNumber,
        String carrier,
        String description,
        String recipientFirst,
        String recipientLast,
        String recipientEmail
    ) {}
}