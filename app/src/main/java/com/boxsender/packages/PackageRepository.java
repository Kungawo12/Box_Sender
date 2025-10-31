package com.boxsender.packages;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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
}