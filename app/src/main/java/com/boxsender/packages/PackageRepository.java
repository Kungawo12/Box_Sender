package com.boxsender.packages;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<Package, Long> {
    Optional<Package> findByTrackingNumber(String trackingNumber);
    List<Package> findByRecipientId(Long recipientId);
    List<Package> findByStatus(String status);
    List<Package> findByEmployeeId(Long employeeId);
}