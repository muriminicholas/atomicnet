package com.atomicnet.entity;

import jakarta.persistence.*;


@Entity
public class PackageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment

    private String type;
    private int price;
    private int durationHours;
    private int bandwidthMbps;

    public PackageInfo(String type, int price, int durationHours, int bandwidthMbps) {
        this.type = type;
        this.price = price;
        this.durationHours = durationHours;
        this.bandwidthMbps = bandwidthMbps;
    }

    public String getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public int getBandwidthMbps() {
        return bandwidthMbps;
    }
}
