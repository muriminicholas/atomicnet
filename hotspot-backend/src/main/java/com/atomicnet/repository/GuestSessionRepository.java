package com.atomicnet.repository;

import com.atomicnet.entity.GuestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {

    List<GuestSession> findByActiveTrue();

    Optional<GuestSession> findByPhoneNumberAndActiveTrue(String phoneNumber);

    GuestSession findByIpAddress(String ipAddress);
    boolean existsByPhoneNumber(String phoneNumber);
}