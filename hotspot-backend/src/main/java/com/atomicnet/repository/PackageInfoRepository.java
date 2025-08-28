package com.atomicnet.repository;

import com.atomicnet.entity.PackageInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {
    Optional<PackageInfo> findByType(String type);
}