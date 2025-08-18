package com.atomicnet.repository;

import com.atomicnet.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
}