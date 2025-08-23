package com.atomicnet.dto;
//Purpose: DTO for activating a package for a user.


public class PackageActivationRequest {
    private String username;
    private String packageType;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
}