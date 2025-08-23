package com.atomicnet.repository;

import com.atomicnet.entity.PackageAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackageAssignmentRepository extends JpaRepository<PackageAssignment, Long> {
    Optional<PackageAssignment> findByUsernameAndActiveTrue(String username);
    Optional<PackageAssignment> findByUsername(String username);
}