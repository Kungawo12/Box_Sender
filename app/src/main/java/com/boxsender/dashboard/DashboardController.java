package com.boxsender.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.packages.Package;
import com.boxsender.packages.PackageRepository;
import com.boxsender.recipients.RecipientRepository;

/**
 * Dashboard Controller
 *
 * This REST controller provides endpoints for dashboard statistics
 * and recent activity information.
 *
 * Base URL: /api/dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PackageRepository packageRepo;
    private final RecipientRepository recipientRepo;

    /**
     * Constructor-based Dependency Injection
     */
    public DashboardController(PackageRepository packageRepo,
                                RecipientRepository recipientRepo) {
        this.packageRepo = packageRepo;
        this.recipientRepo = recipientRepo;
    }

    /**
     * Get dashboard statistics
     *
     * HTTP Endpoint: GET /api/dashboard/stats
     * Authentication: Required
     *
     * Returns statistics including:
     * - Total packages in system
     * - Packages awaiting pickup
     * - Packages picked up
     * - Packages picked up today
     * - Overdue packages (>7 days)
     * - Total recipients
     *
     * @param auth the authentication object
     * @return ResponseEntity with dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(Authentication auth) {
        try {
            // Get counts
            long totalPackages = packageRepo.count();
            long pendingPickups = packageRepo.countByStatus("received");
            long pickedUpTotal = packageRepo.countByStatus("picked");
            long totalRecipients = recipientRepo.count();

            // Count packages picked up today
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            List<Package> allPickedPackages = packageRepo.findByStatus("picked");
            long pickedUpToday = allPickedPackages.stream()
                .filter(pkg -> pkg.getUpdatedAt() != null &&
                               pkg.getUpdatedAt().isAfter(todayStart))
                .count();

            // Count overdue packages (received more than 7 days ago)
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Package> receivedPackages = packageRepo.findByStatus("received");
            long overduePackages = receivedPackages.stream()
                .filter(pkg -> pkg.getCreatedAt() != null &&
                               pkg.getCreatedAt().isBefore(sevenDaysAgo))
                .count();

            // Calculate pickup rate
            double pickupRate = totalPackages > 0
                ? (double) pickedUpTotal / totalPackages * 100
                : 0;

            return ResponseEntity.ok(Map.of(
                "totalPackages", totalPackages,
                "pendingPickups", pendingPickups,
                "pickedUpTotal", pickedUpTotal,
                "pickedUpToday", pickedUpToday,
                "overduePackages", overduePackages,
                "totalRecipients", totalRecipients,
                "pickupRate", String.format("%.1f%%", pickupRate)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    /**
     * Get recent package activity
     *
     * HTTP Endpoint: GET /api/dashboard/recent
     * Authentication: Required
     *
     * Returns the most recently logged packages (up to 20)
     *
     * @param auth the authentication object
     * @return ResponseEntity with list of recent packages
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivity(Authentication auth) {
        try {
            // Get all packages and sort by created date descending
            List<Package> allPackages = packageRepo.findAll();

            List<Package> recentPackages = allPackages.stream()
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .limit(20)
                .toList();

            return ResponseEntity.ok(recentPackages);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve recent activity: " + e.getMessage()));
        }
    }

    /**
     * Get overdue packages list
     *
     * HTTP Endpoint: GET /api/dashboard/overdue
     * Authentication: Required
     *
     * Returns packages that have been waiting for pickup for more than 7 days
     *
     * @param auth the authentication object
     * @return ResponseEntity with list of overdue packages
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverduePackages(Authentication auth) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Package> receivedPackages = packageRepo.findByStatus("received");

            List<Package> overduePackages = receivedPackages.stream()
                .filter(pkg -> pkg.getCreatedAt() != null &&
                               pkg.getCreatedAt().isBefore(sevenDaysAgo))
                .toList();

            return ResponseEntity.ok(overduePackages);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve overdue packages: " + e.getMessage()));
        }
    }
}
