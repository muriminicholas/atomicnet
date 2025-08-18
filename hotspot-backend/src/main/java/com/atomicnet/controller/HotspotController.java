package com.atomicnet.controller;

import com.atomicnet.entity.User;
import com.atomicnet.entity.Voucher;
import com.atomicnet.repository.UserRepository;
import com.atomicnet.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HotspotController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @PostMapping("/package/{type}")
    public ResponseEntity<ApiResponse> selectPackage(@PathVariable String type) {
        if (type == null || type.trim().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Package type is required"));
        }
        return ResponseEntity.ok(new ApiResponse("success", "Package " + type + " activated successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty() || 
            request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Username and password are required"));
        }
        User user = userRepository.findById(request.getUsername()).orElse(null);
        if (user != null && user.getPassword().equals(request.getPassword())) {
            user.setActive(true);
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse("success", "Login successful"));
        }
        return ResponseEntity.status(401).body(new ApiResponse("error", "Invalid credentials"));
    }

    @PostMapping("/create_account")
    public ResponseEntity<ApiResponse> createAccount(@RequestBody LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty() || 
            request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Username and password are required"));
        }
        if (userRepository.existsById(request.getUsername())) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Username already exists"));
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("success", "Account created successfully"));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse> activateVoucher(@RequestBody VoucherRequest request) {
        if (request == null || request.getVoucherCode() == null || request.getVoucherCode().trim().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Voucher code is required"));
        }
        Voucher voucher = voucherRepository.findById(request.getVoucherCode()).orElse(null);
        if (voucher != null && !voucher.isUsed()) {
            voucher.setUsed(true);
            voucherRepository.save(voucher);
            return ResponseEntity.ok(new ApiResponse("success", "Voucher " + voucher.getCode() + " activated for " + voucher.getPackageType()));
        }
        return ResponseEntity.status(400).body(new ApiResponse("error", "Invalid or used voucher"));
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class VoucherRequest {
    private String voucherCode;

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
}

class ApiResponse {
    private String status;
    private String message;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}