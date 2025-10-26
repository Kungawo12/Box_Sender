package com.boxsender.packages;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageRepository packageRepository;
    private final EmployeeRepository employeeRepository;

    public PackageController(PackageRepository packageRepository, EmployeeRepository employeeRepository) {
        this.packageRepository = packageRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * POST /api/packages - Log a new package
     */
    @PostMapping
    public ResponseEntity<?> logPackage(@RequestBody LogPackageRequest request, Authentication auth) {
        try {
            // Get the currently logged-in employee
            String email = auth.getName();
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Validate request
            if (request.trackingNumber() == null || request.trackingNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tracking number is required"));
            }
            if (request.carrier() == null || request.carrier().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Carrier is required"));
            }
            if (request.recipientEmail() == null || request.recipientEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Recipient email is required"));
            }

            // Check if tracking number already exists
            if (packageRepository.findByTrackingNumber(request.trackingNumber()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Package with this tracking number already exists"));
            }

            // Generate unique pickup code
            String pickupCode = generatePickupCode();
            while (packageRepository.findByPickupCode(pickupCode).isPresent()) {
                pickupCode = generatePickupCode();
            }

            // Create and save package
            Package pkg = new Package(
                    request.trackingNumber(),
                    request.carrier(),
                    request.recipientFirstName(),
                    request.recipientLastName(),
                    request.recipientEmail(),
                    request.description(),
                    pickupCode,
                    employee.getId()
            );

            Package savedPackage = packageRepository.save(pkg);

            // Return response
            return ResponseEntity.ok(Map.of(
                    "id", savedPackage.getId(),
                    "trackingNumber", savedPackage.getTrackingNumber(),
                    "pickupCode", savedPackage.getPickupCode(),
                    "status", savedPackage.getStatus(),
                    "createdAt", savedPackage.getCreatedAt().toString()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/packages - Get all packages (or filter by status)
     */
    @GetMapping
    public ResponseEntity<?> getPackages(@RequestParam(required = false) String status, Authentication auth) {
        try {
            List<Package> packages;
            if (status != null && !status.isEmpty()) {
                packages = packageRepository.findByStatus(status);
            } else {
                packages = packageRepository.findAll();
            }
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/packages/{id} - Get package by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPackageById(@PathVariable Long id) {
        return packageRepository.findById(id)
                .map(pkg -> ResponseEntity.ok((Object) pkg))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/packages/tracking/{trackingNumber} - Get package by tracking number
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getPackageByTrackingNumber(@PathVariable String trackingNumber) {
        return packageRepository.findByTrackingNumber(trackingNumber)
                .map(pkg -> ResponseEntity.ok((Object) pkg))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/packages/my - Get packages logged by current user
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPackages(Authentication auth) {
        try {
            String email = auth.getName();
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            List<Package> packages = packageRepository.findByLoggedByEmployeeIdOrderByCreatedAtDesc(employee.getId());
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generate a random 6-character alphanumeric pickup code
     */
    private String generatePickupCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluded similar-looking chars
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}

/**
 * Request DTO for logging a package
 */
record LogPackageRequest(
        String trackingNumber,
        String carrier,
        String recipientFirstName,
        String recipientLastName,
        String recipientEmail,
        String description
) {}
