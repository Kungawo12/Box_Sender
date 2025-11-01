package com.boxsender.packages;

import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    // Secure random generator for pickup codes
    // Uses cryptographically strong random number generation
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    // Character set for pickup codes (excludes confusing characters like 0, O, I, 1)
    private static final String PICKUP_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

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
     * Authentication: Required (ADMIN or MAILROOM_STAFF role only)
     * Authorization: Only ADMIN and MAILROOM_STAFF can log packages
     *
     * This method handles the entire package logging workflow:
     * 1. Validates the logged-in employee exists
     * 2. Checks if tracking number is unique
     * 3. Finds existing recipient or creates a new one
     * 4. Saves the package to the database
     * 5. Sends email notification to the recipient
     *
     * Role-Based Access Control:
     * - ADMIN: Full access (can log packages)
     * - MAILROOM_STAFF: Full access (can log packages)
     * - EMPLOYEE: No access (403 Forbidden)
     *
     * @param body the request body containing package details (validated)
     * @param auth the authentication object containing logged-in employee's email
     * @return ResponseEntity with success or error response
     *         - 200 OK: Package logged successfully with package details
     *         - 400 Bad Request: Tracking number already exists
     *         - 403 Forbidden: User doesn't have ADMIN or MAILROOM_STAFF role
     *         - 500 Internal Server Error: Unexpected error occurred
     */
    @PostMapping  // Maps to POST requests at /api/packages
    @PreAuthorize("hasAnyRole('ADMIN', 'MAILROOM_STAFF')")  // Only ADMIN and MAILROOM_STAFF can log packages
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

            // Step 5b: Generate unique pickup code for package verification
            pkg.setPickupCode(generatePickupCode());

            // Step 6: Save package to database
            Package savedPackage = packageRepo.save(pkg);

            // Step 7: Send email notification to recipient with pickup code
            // Using try-catch because email failure shouldn't fail the entire operation
            boolean emailSent = true;
            String emailMessage = "";
            try {
                emailService.sendPackageNotification(
                    recipient.getEmail(),
                    recipient.getFirstName(),
                    savedPackage.getTrackingNumber(),
                    savedPackage.getPickupCode()
                );
                emailMessage = "Pickup code email sent to " + recipient.getEmail();
            } catch (Exception e) {
                // Email failure is not critical - package is already saved
                // Log the error but continue (package was successfully logged)
                emailSent = false;
                emailMessage = "Warning: Email notification failed. Please contact recipient manually.";
                System.err.println("Email notification failed: " + e.getMessage());
            }

            // Step 8: Return success response with package details
            return ResponseEntity.ok(Map.of(
                "id", savedPackage.getId(),
                "trackingNumber", savedPackage.getTrackingNumber(),
                "status", savedPackage.getStatus(),
                "pickupCode", savedPackage.getPickupCode(),
                "recipientName", recipient.getFirstName() + " " + recipient.getLastName(),
                "recipientEmail", recipient.getEmail(),
                "emailSent", emailSent,
                "message", "Package logged successfully! " + emailMessage
            ));

        } catch (Exception e) {
            // Catch any unexpected errors and return 500 error
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to log package: " + e.getMessage()));
        }
    }

    /**
     * Mark a package as picked up with verification code
     *
     * HTTP Endpoint: PUT /api/packages/{id}/pickup
     * Authentication: Required (employee must be logged in)
     *
     * This method handles the secure package pickup workflow:
     * 1. Validates the package exists
     * 2. Verifies the pickup code matches
     * 3. Checks if package is still in "received" status
     * 4. Updates status to "picked"
     * 5. Records the pickup timestamp
     *
     * @param id the package ID
     * @param body the request body containing signature, notes, and pickup code
     * @param auth the authentication object containing logged-in employee's email
     * @return ResponseEntity with success or error response
     *         - 200 OK: Package marked as picked up successfully
     *         - 400 Bad Request: Package already picked up or invalid pickup code
     *         - 404 Not Found: Package not found
     *         - 500 Internal Server Error: Unexpected error occurred
     */
    @PutMapping("/{id}/pickup")
    public ResponseEntity<?> confirmPickup(
        @PathVariable Long id,
        @RequestBody PickupRequest body,
        Authentication auth) {

        try {
            // Get logged-in employee
            Employee employee = employeeRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Find package
            Package pkg = packageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + id));

            // Check if already picked up
            if ("picked".equals(pkg.getStatus())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Package already picked up"));
            }

            // Validate pickup code
            if (body.pickupCode() == null || body.pickupCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Pickup code is required"));
            }

            if (!body.pickupCode().trim().equalsIgnoreCase(pkg.getPickupCode())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid pickup code. Please check the code sent to the recipient."));
            }

            // Update status
            pkg.setStatus("picked");
            pkg.setUpdatedAt(java.time.LocalDateTime.now());

            // Save changes
            Package updatedPkg = packageRepo.save(pkg);

            return ResponseEntity.ok(Map.of(
                "message", "Package successfully marked as picked up",
                "packageId", updatedPkg.getId(),
                "trackingNumber", updatedPkg.getTrackingNumber(),
                "status", updatedPkg.getStatus(),
                "pickedUpAt", updatedPkg.getUpdatedAt()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to confirm pickup: " + e.getMessage()));
        }
    }

    /**
     * Comprehensive search for packages with sorting support
     *
     * HTTP Endpoint: GET /api/packages/search
     * Authentication: Required (employee must be logged in)
     *
     * Query parameters (all optional):
     * - trackingNumber: Search by tracking number (partial, case-insensitive match)
     * - pickupCode: Search by pickup code (exact, case-insensitive match) 🔐
     * - carrier: Search by carrier name (partial, case-insensitive match)
     * - description: Search by package description (partial, case-insensitive match)
     * - recipientFirstName: Search by recipient first name (partial, case-insensitive match)
     * - recipientLastName: Search by recipient last name (partial, case-insensitive match)
     * - recipientEmail: Search by recipient email (partial, case-insensitive match)
     * - status: Filter by status (exact match: "received" or "picked")
     * - sortBy: Field to sort by (e.g., "createdAt", "trackingNumber", "carrier", "status")
     * - sortOrder: Sort direction ("asc" or "desc", defaults to "desc")
     *
     * Examples:
     * - GET /api/packages/search?trackingNumber=1Z999
     * - GET /api/packages/search?pickupCode=A7K2M9
     * - GET /api/packages/search?status=received&sortBy=createdAt&sortOrder=desc
     * - GET /api/packages/search?carrier=UPS&recipientEmail=john
     * - GET /api/packages/search?sortBy=trackingNumber&sortOrder=asc
     *
     * @param trackingNumber optional tracking number to search for
     * @param pickupCode optional pickup code to search for (6-character verification code)
     * @param carrier optional carrier name to search for
     * @param description optional package description to search for
     * @param recipientFirstName optional recipient first name to search for
     * @param recipientLastName optional recipient last name to search for
     * @param recipientEmail optional recipient email to search for
     * @param status optional status to filter by
     * @param sortBy optional field to sort by (defaults to "createdAt")
     * @param sortOrder optional sort direction: "asc" or "desc" (defaults to "desc")
     * @param auth the authentication object containing logged-in employee's email
     * @return ResponseEntity with list of matching packages or error
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPackages(
        @RequestParam(required = false) String trackingNumber,
        @RequestParam(required = false) String pickupCode,
        @RequestParam(required = false) String carrier,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) String recipientFirstName,
        @RequestParam(required = false) String recipientLastName,
        @RequestParam(required = false) String recipientEmail,
        @RequestParam(required = false) String status,
        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String sortOrder,
        Authentication auth) {

        try {
            // Build sort specification
            Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

            // Convert empty strings to null for proper query handling
            String trackingNumberParam = (trackingNumber != null && !trackingNumber.trim().isEmpty()) ? trackingNumber.trim() : null;
            String pickupCodeParam = (pickupCode != null && !pickupCode.trim().isEmpty()) ? pickupCode.trim() : null;
            String carrierParam = (carrier != null && !carrier.trim().isEmpty()) ? carrier.trim() : null;
            String descriptionParam = (description != null && !description.trim().isEmpty()) ? description.trim() : null;
            String recipientFirstParam = (recipientFirstName != null && !recipientFirstName.trim().isEmpty()) ? recipientFirstName.trim() : null;
            String recipientLastParam = (recipientLastName != null && !recipientLastName.trim().isEmpty()) ? recipientLastName.trim() : null;
            String recipientEmailParam = (recipientEmail != null && !recipientEmail.trim().isEmpty()) ? recipientEmail.trim() : null;
            String statusParam = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

            // Perform comprehensive search
            java.util.List<Package> packages = packageRepo.searchPackages(
                trackingNumberParam,
                pickupCodeParam,
                carrierParam,
                descriptionParam,
                recipientFirstParam,
                recipientLastParam,
                recipientEmailParam,
                statusParam,
                sort
            );

            return ResponseEntity.ok(packages);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to search packages: " + e.getMessage()));
        }
    }

    /**
     * Get all packages (for dashboard and general viewing)
     *
     * HTTP Endpoint: GET /api/packages
     * Authentication: Required
     *
     * @param auth the authentication object
     * @return ResponseEntity with list of all packages
     */
    @GetMapping
    public ResponseEntity<?> getAllPackages(Authentication auth) {
        try {
            java.util.List<Package> packages = packageRepo.findAll();
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve packages: " + e.getMessage()));
        }
    }

    /**
     * Generate a secure 6-character pickup code
     * Uses alphanumeric characters excluding confusing ones (0, O, I, 1)
     *
     * Example output: "A7K2M9"
     *
     * @return a randomly generated 6-character pickup code
     */
    private String generatePickupCode() {
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(PICKUP_CODE_CHARS.length());
            code.append(PICKUP_CODE_CHARS.charAt(index));
        }
        return code.toString();
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

    /**
     * Data Transfer Object (DTO) for package pickup requests
     *
     * @param signature digital signature or name of person picking up
     * @param notes optional notes about the pickup
     * @param pickupCode the verification code provided by the recipient
     */
    public record PickupRequest(
        String signature,
        String notes,
        String pickupCode
    ) {}
}