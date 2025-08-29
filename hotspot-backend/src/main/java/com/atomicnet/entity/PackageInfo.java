package com.atomicnet.entity;

import jakarta.persistence.*;

@Entity
public class PackageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private int price;
    private int durationHours;
    private int bandwidthMbps;

    // No-arg constructor for JPA
    public PackageInfo() {}

    public PackageInfo(String type, int price, int durationHours, int bandwidthMbps) {
        this.type = type;
        this.price = price;
        this.durationHours = durationHours;
        this.bandwidthMbps = bandwidthMbps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public int getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(int bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }
}