package com.boxsender.packages;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boxsender.recipients.Recipient;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    
    // Simple query: Find by tracking number
    Optional<Package> findByTrackingNumber(String trackingNumber);
    
    /**
     * Advanced search with multiple filters
     * Uses JPQL (Java Persistence Query Language)
     */
    @Query("SELECT p FROM Package p WHERE " +
            // Filter 1: Tracking number (partial match, case-insensitive)
            "(:tracking IS NULL OR :tracking = '' OR LOWER(p.trackingNumber) LIKE LOWER(CONCAT('%', :tracking, '%'))) " +
            "AND " +
           // Filter 2: Recipient name (searches both first and last name)
            "(:recipientName IS NULL OR :recipientName = '' OR " +
            "LOWER(p.recipient.firstName) LIKE LOWER(CONCAT('%', :recipientName, '%')) OR " +
            "LOWER(p.recipient.lastName) LIKE LOWER(CONCAT('%', :recipientName, '%'))) " +
            "AND " +
           // Filter 3: Status
            "(:status IS NULL OR :status = 'all' OR p.status = :status)")
    List<Package> advancedSearch(
        @Param("tracking") String tracking,
        @Param("recipientName") String recipientName,
        @Param("status") String status
    );
}