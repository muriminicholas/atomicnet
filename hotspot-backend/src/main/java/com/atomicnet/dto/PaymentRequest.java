package com.atomicnet.dto;
//Purpose: DTO for initiating a payment, containing phone number and package type.


public class PaymentRequest {
    private String phoneNumber;
    private String packageType;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
}

