package com.boxsender.packages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    Optional<Package> findByTrackingNumber(String trackingNumber);

    Optional<Package> findByPickupCode(String pickupCode);

    List<Package> findByRecipientEmail(String recipientEmail);

    List<Package> findByStatus(String status);

    List<Package> findByLoggedByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
