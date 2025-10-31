package com.boxsender.packages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Package Repository Interface
 *
 * This interface provides data access operations for the Package entity.
 * Extends JpaRepository to inherit standard CRUD operations automatically.
 *
 * Spring Data JPA implements this interface at runtime using the method naming
 * conventions to generate appropriate SQL queries.
 *
 * Inherited CRUD operations from JpaRepository:
 * - save(Package) - Insert or update a package
 * - findById(Long) - Find package by ID
 * - findAll() - Get all packages
 * - deleteById(Long) - Delete package by ID
 * - and many more...
 *
 * Custom Query Methods:
 * This repository defines several custom finder methods to support
 * different search operations needed by the application:
 * - Search by tracking number
 * - Find all packages for a specific recipient
 * - Filter packages by status (received/picked)
 * - Find all packages logged by a specific employee
 */
public interface PackageRepository extends JpaRepository<Package, Long> {

    /**
     * Finds a package by its tracking number
     * Tracking numbers are unique, so this returns at most one package
     *
     * Used when searching for a specific package or verifying if a tracking number exists
     * Spring generates: SELECT * FROM packages WHERE tracking_number = ?
     *
     * @param trackingNumber the tracking number to search for
     * @return Optional containing the Package if found, or empty if not found
     */
    Optional<Package> findByTrackingNumber(String trackingNumber);

    /**
     * Finds all packages for a specific recipient
     * A recipient can have multiple packages (one-to-many relationship)
     *
     * Used to display all packages belonging to one person
     * Spring generates: SELECT * FROM packages WHERE recipient_id = ?
     *
     * @param recipientId the ID of the recipient
     * @return List of all packages for that recipient (empty list if none found)
     */
    List<Package> findByRecipientId(Long recipientId);

    /**
     * Finds all packages with a specific status
     * Status values: "received" (waiting for pickup) or "picked" (already collected)
     *
     * Used to display all packages awaiting pickup or view pickup history
     * Spring generates: SELECT * FROM packages WHERE status = ?
     *
     * @param status the status to filter by ("received" or "picked")
     * @return List of all packages with that status (empty list if none found)
     */
    List<Package> findByStatus(String status);

    /**
     * Finds all packages that were logged by a specific employee
     * Used for tracking which employee logged which packages
     *
     * Spring generates: SELECT * FROM packages WHERE employee_id = ?
     *
     * @param employeeId the ID of the employee
     * @return List of all packages logged by that employee (empty list if none found)
     */
    List<Package> findByEmployeeId(Long employeeId);

    /**
     * Finds packages by tracking number (partial match)
     * Useful for search functionality where users may not know the full tracking number
     *
     * Spring generates: SELECT * FROM packages WHERE tracking_number LIKE %?%
     *
     * @param trackingNumber partial or full tracking number to search for
     * @return List of packages matching the tracking number pattern
     */
    List<Package> findByTrackingNumberContaining(String trackingNumber);

    /**
     * Finds packages by recipient email (partial match)
     * Useful for searching all packages for a recipient by email
     *
     * Spring generates: SELECT p FROM Package p WHERE p.recipient.email LIKE %?%
     *
     * @param email partial or full recipient email to search for
     * @return List of packages for recipients matching the email pattern
     */
    List<Package> findByRecipientEmailContaining(String email);

    /**
     * Count packages by status
     * Useful for dashboard statistics (e.g., how many packages are waiting to be picked up)
     *
     * Spring generates: SELECT COUNT(*) FROM packages WHERE status = ?
     *
     * @param status the status to count ("received" or "picked")
     * @return Number of packages with that status
     */
    long countByStatus(String status);

    /**
     * Comprehensive search method supporting multiple criteria
     * Uses JPQL (Java Persistence Query Language) for flexible searching
     *
     * This method allows searching by:
     * - Tracking number (partial match)
     * - Carrier (partial match)
     * - Package description (partial match)
     * - Recipient first name (partial match)
     * - Recipient last name (partial match)
     * - Recipient email (partial match)
     * - Status (exact match)
     *
     * All text searches are case-insensitive
     *
     * @param trackingNumber partial tracking number to search for
     * @param carrier partial carrier name to search for
     * @param description partial description to search for
     * @param recipientFirstName partial recipient first name to search for
     * @param recipientLastName partial recipient last name to search for
     * @param recipientEmail partial recipient email to search for
     * @param status exact status to filter by
     * @param sort sorting specification (e.g., Sort.by("createdAt").descending())
     * @return List of packages matching the criteria, sorted as specified
     */
    @Query("SELECT p FROM Package p " +
           "WHERE (:trackingNumber IS NULL OR LOWER(p.trackingNumber) LIKE LOWER(CONCAT('%', :trackingNumber, '%'))) " +
           "AND (:carrier IS NULL OR LOWER(p.carrier) LIKE LOWER(CONCAT('%', :carrier, '%'))) " +
           "AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:recipientFirstName IS NULL OR LOWER(p.recipient.firstName) LIKE LOWER(CONCAT('%', :recipientFirstName, '%'))) " +
           "AND (:recipientLastName IS NULL OR LOWER(p.recipient.lastName) LIKE LOWER(CONCAT('%', :recipientLastName, '%'))) " +
           "AND (:recipientEmail IS NULL OR LOWER(p.recipient.email) LIKE LOWER(CONCAT('%', :recipientEmail, '%'))) " +
           "AND (:status IS NULL OR p.status = :status)")
    List<Package> searchPackages(
        String trackingNumber,
        String carrier,
        String description,
        String recipientFirstName,
        String recipientLastName,
        String recipientEmail,
        String status,
        Sort sort
    );

    /**
     * Find all packages with custom sorting
     *
     * This overloaded version of findAll accepts a Sort parameter
     * Inherited from JpaRepository but documented here for clarity
     *
     * Common sorting examples:
     * - Sort.by("createdAt").descending() - Newest first
     * - Sort.by("trackingNumber").ascending() - Alphabetical by tracking number
     * - Sort.by("status", "createdAt") - By status, then by date
     *
     * @param sort the sorting specification
     * @return List of all packages sorted as specified
     */
    List<Package> findAll(Sort sort);
}