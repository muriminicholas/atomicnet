package com.atomicnet.service;

import com.atomicnet.repository.GuestSessionRepository;
import com.atomicnet.entity.GuestSession;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GuestSessionService {

    private final GuestSessionRepository repository;

    public GuestSessionService(GuestSessionRepository repository) {
        this.repository = repository;
    }

    public GuestSession startSession(String ip, String mac) {
        GuestSession session = new GuestSession();
        session.setIpAddress(ip);
        session.setMacAddress(mac);
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        return repository.save(session);
    }

    public GuestSession endSession(Long id) {
        GuestSession session = repository.findById(id).orElseThrow();
        session.setActive(false);
        session.setEndTime(LocalDateTime.now());
        return repository.save(session);
    }

    public List<GuestSession> getActiveSessions() {
        return repository.findByActiveTrue();
    }
}

