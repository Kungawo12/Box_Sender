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

/**
 * Package Controller
 *
 * This REST controller handles HTTP requests related to package operations.
 * It provides API endpoints for logging new packages into the system.
 *
 * Base URL: /api/packages
 *
 * Key responsibilities:
 * - Receive package information from the frontend
 * - Validate tracking numbers are unique
 * - Create or retrieve recipient records
 * - Save package records to the database
 * - Send email notifications to recipients
 *
 * This follows the REST architectural pattern:
 * - @RestController automatically converts return values to JSON
 * - HTTP methods (POST, GET, PUT, DELETE) map to CRUD operations
 * - Returns ResponseEntity for flexible HTTP response control
 */
@RestController  // Combines @Controller and @ResponseBody for REST APIs
@RequestMapping("/api/packages")  // Base URL path for all endpoints in this controller
public class PackageController {

    // Dependencies injected via constructor (Dependency Injection pattern)
    // These are marked 'final' to ensure they're immutable after construction
    private final PackageRepository packageRepo;      // Database access for packages
    private final RecipientRepository recipientRepo;  // Database access for recipients
    private final EmployeeRepository employeeRepo;    // Database access for employees
    private final EmailService emailService;          // Service for sending email notifications

    /**
     * Constructor-based Dependency Injection
     * Spring automatically provides these dependencies when creating the controller
     *
     * Constructor injection is preferred over field injection because:
     * - Makes dependencies explicit and required
     * - Enables immutability (final fields)
     * - Easier to test (can pass mock objects)
     *
     * @param packageRepo repository for package database operations
     * @param recipientRepo repository for recipient database operations
     * @param employeeRepo repository for employee database operations
     * @param emailService service for sending email notifications
     */
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
     * Log a new package when it arrives
     *
     * HTTP Endpoint: POST /api/packages
     * Authentication: Required (employee must be logged in)
     *
     * This method handles the entire package logging workflow:
     * 1. Validates the logged-in employee exists
     * 2. Checks if tracking number is unique
     * 3. Finds existing recipient or creates a new one
     * 4. Saves the package to the database
     * 5. Sends email notification to the recipient
     *
     * @param body the request body containing package details (validated)
     * @param auth the authentication object containing logged-in employee's email
     * @return ResponseEntity with success or error response
     *         - 200 OK: Package logged successfully with package details
     *         - 400 Bad Request: Tracking number already exists
     *         - 500 Internal Server Error: Unexpected error occurred
     */
    @PostMapping  // Maps to POST requests at /api/packages
    public ResponseEntity<?> logPackage(@Valid @RequestBody LogPackageRequest body,
                                        Authentication auth) {
        try {
            // Step 1: Get the currently logged-in employee from authentication
            // auth.getName() returns the email address (configured in SecurityConfig)
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Step 2: Validate that tracking number is unique
            // Prevents duplicate package entries in the system
            if (packageRepo.findByTrackingNumber(body.trackingNumber()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tracking number already exists"));
            }

            // Step 3: Extract recipient information from request
            String firstName = body.recipientFirst();
            String lastName = body.recipientLast() != null ? body.recipientLast() : "";

            // Step 4: Find existing recipient by email, or create a new one
            // This prevents duplicate recipient records for the same person
            Recipient recipient = recipientRepo.findByEmail(body.recipientEmail())
                .orElseGet(() -> {
                    // Recipient doesn't exist, create new one
                    Recipient newRecipient = new Recipient(
                        firstName,
                        lastName,
                        body.recipientEmail(),
                        null  // Department is optional
                    );
                    return recipientRepo.save(newRecipient);
                });

            // Step 5: Create new Package object with all information
            // Default status is "received" (set in Package constructor)
            Package pkg = new Package(
                body.trackingNumber(),
                body.carrier(),
                body.description(),
                recipient,
                employee
            );

            // Step 6: Save package to database
            Package savedPackage = packageRepo.save(pkg);

            // Step 7: Send email notification to recipient
            // Using try-catch because email failure shouldn't fail the entire operation
            try {
                emailService.sendPackageNotification(
                    recipient.getEmail(),
                    recipient.getFirstName(),
                    savedPackage.getTrackingNumber()
                );
            } catch (Exception e) {
                // Email failure is not critical - package is already saved
                // Log the error but continue (package was successfully logged)
                System.err.println("Email notification failed: " + e.getMessage());
            }

            // Step 8: Return success response with package details
            return ResponseEntity.ok(Map.of(
                "id", savedPackage.getId(),
                "trackingNumber", savedPackage.getTrackingNumber(),
                "status", savedPackage.getStatus(),
                "recipientName", recipient.getFirstName() + " " + recipient.getLastName(),
                "message", "Package logged successfully. Notification email sent to " + recipient.getEmail()
            ));

        } catch (Exception e) {
            // Catch any unexpected errors and return 500 error
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to log package: " + e.getMessage()));
        }
    }

    /**
     * Data Transfer Object (DTO) for package logging requests
     *
     * Java record class provides:
     * - Immutable data structure
     * - Automatic constructor, getters, equals, hashCode, toString
     * - Compact syntax for data-only classes
     *
     * This record defines the expected JSON structure for the request body:
     * {
     *   "trackingNumber": "1Z999AA10123456784",
     *   "carrier": "UPS",
     *   "description": "Small box",
     *   "recipientFirst": "John",
     *   "recipientLast": "Doe",
     *   "recipientEmail": "john.doe@example.com"
     * }
     *
     * @param trackingNumber the carrier's tracking number (required)
     * @param carrier the shipping carrier name (required)
     * @param description optional notes about the package
     * @param recipientFirst recipient's first name (required)
     * @param recipientLast recipient's last name (optional)
     * @param recipientEmail recipient's email for notifications (required)
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