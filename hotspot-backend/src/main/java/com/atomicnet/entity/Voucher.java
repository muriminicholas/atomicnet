package com.atomicnet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    private String code;
    private String packageType;
    private boolean used;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
