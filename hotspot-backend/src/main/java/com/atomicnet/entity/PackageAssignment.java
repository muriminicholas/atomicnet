package com.atomicnet.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PackageAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

private String checkoutRequestId; 
    private String username;
    private String packageType;
    private int bandwidthMbps;
    private int durationHours;
    private LocalDateTime startTime;
    private boolean active;

    // Getters and Setters
       public String getCheckoutRequestId() {
        return checkoutRequestId;
    }

    public void setCheckoutRequestId(String checkoutRequestId) {
        this.checkoutRequestId = checkoutRequestId;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }
    public int getBandwidthMbps() { return bandwidthMbps; }
    public void setBandwidthMbps(int bandwidthMbps) { this.bandwidthMbps = bandwidthMbps; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}