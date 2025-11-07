package com.boxsender.packages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    
    Optional<Package> findByTrackingNumber(String trackingNumber);
    
    // Advanced search
    @Query("SELECT p FROM Package p WHERE " +
           "(:tracking IS NULL OR :tracking = '' OR LOWER(p.trackingNumber) LIKE LOWER(CONCAT('%', :tracking, '%'))) " +
           "AND " +
           "(:recipientName IS NULL OR :recipientName = '' OR " +
           "LOWER(p.recipient.firstName) LIKE LOWER(CONCAT('%', :recipientName, '%')) OR " +
           "LOWER(p.recipient.lastName) LIKE LOWER(CONCAT('%', :recipientName, '%'))) " +
           "AND " +
           "(:status IS NULL OR :status = 'all' OR p.status = :status)")
    List<Package> advancedSearch(
        @Param("tracking") String tracking,
        @Param("recipientName") String recipientName,
        @Param("status") String status
    );
}