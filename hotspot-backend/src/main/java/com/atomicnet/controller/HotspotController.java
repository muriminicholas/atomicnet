package com.atomicnet.controller;

import com.atomicnet.config.MpesaConfig;
import com.atomicnet.dto.ApiResponse;
import com.atomicnet.dto.LoginRequest;
import com.atomicnet.dto.MpesaCallback;
import com.atomicnet.dto.PackageActivationRequest;
import com.atomicnet.dto.PaymentRequest;
import com.atomicnet.dto.VoucherRequest;
import com.atomicnet.entity.GuestSession;
import com.atomicnet.entity.PackageAssignment;
import com.atomicnet.entity.PackageInfo;
import com.atomicnet.entity.User;
import com.atomicnet.entity.Voucher;
import com.atomicnet.repository.GuestSessionRepository;
import com.atomicnet.repository.PackageAssignmentRepository;
import com.atomicnet.repository.PackageInfoRepository;
import com.atomicnet.repository.UserRepository;
import com.atomicnet.repository.VoucherRepository;
import com.atomicnet.service.MpesaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
public class HotspotController {

    private static final Logger logger = LoggerFactory.getLogger(HotspotController.class);

    @Autowired
    private GuestSessionRepository guestSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private PackageAssignmentRepository packageAssignmentRepository;

    @Autowired
    private PackageInfoRepository packageInfoRepository;

    @Autowired
    private MpesaConfig mpesaConfig;

    @Autowired
    private MpesaService mpesaService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/package/{type}")
    @Transactional
    public ResponseEntity<ApiResponse> selectPackage(@PathVariable String type, @RequestBody(required = false) PackageRequest packageRequest) {
        logger.info("Selecting package: {}", type);
        if (type == null || type.trim().isEmpty()) {
            logger.error("Package type is null or empty");
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Package type is required"));
        }
        PackageInfo packageInfo = packageInfoRepository.findByType(type.toLowerCase()).orElse(null);
        if (packageInfo == null) {
            logger.error("Package type {} not found in database", type);
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Invalid package type: " + type));
        }
        logger.info("Found package: type={}, price={}, duration={}, bandwidth={}",
            packageInfo.getType(), packageInfo.getPrice(), packageInfo.getDurationHours(), packageInfo.getBandwidthMbps());
        return ResponseEntity.ok(new ApiResponse("success",
            String.format("Package %s selected. Please pay Ksh.%d via MPESA to activate %d Mbps for %d hours.",
                packageInfo.getType(), packageInfo.getPrice(), packageInfo.getBandwidthMbps(), packageInfo.getDurationHours()),
            packageInfo)); // Return PackageInfo for frontend
    }
   
 
    @PostMapping("/initiate_payment")
    @Transactional
    public ResponseEntity<ApiResponse> initiatePayment(@RequestBody PaymentRequest request) {
        logger.info("Initiating payment for phone: {}", request.getPhoneNumber());
        try {
            // Fetch the package info based on the type
            PackageInfo packageInfo = packageInfoRepository.findByType(request.getPackageType().toLowerCase()).orElse(null);
            if (packageInfo == null) {
                logger.error("Invalid package type: {}", request.getPackageType());
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Invalid package type"));
            }

            // Check if the phone number exists in the system
            if (!guestSessionRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                logger.error("Phone number not registered: {}", request.getPhoneNumber());
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Phone number not registered"));
            }

            // Generate a unique transaction ID
            String transactionId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

            // Get the M-Pesa access token
            String accessToken = mpesaService.getAccessToken("consumerKey", "consumerSecret");

            // Initiate the STK Push
            mpesaService.initiateStkPush(
                accessToken,
                "shortcode", "passkey", "callbackUrl",
                request.getPhoneNumber(), packageInfo.getPrice(), packageInfo.getType(), transactionId
            );

            // Save the pending package assignment in the database
            PackageAssignment pending = new PackageAssignment();
            pending.setCheckoutRequestId(transactionId);
            pending.setUsername("pending_" + transactionId);
            pending.setPackageType(packageInfo.getType());
            pending.setBandwidthMbps(packageInfo.getBandwidthMbps());
            pending.setDurationHours(packageInfo.getDurationHours());
            pending.setStartTime(null);
            pending.setActive(false);
            packageAssignmentRepository.save(pending);

            return ResponseEntity.ok(new ApiResponse("success", "STK push initiated. Please check your phone."));
        } catch (IOException e) {
            logger.error("Payment initiation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", "Internal server error"));
        }
    }

    @PostMapping("/mpesa/callback")
    @Transactional
    public ResponseEntity<Void> handleMpesaCallback(@RequestBody MpesaCallback callback) {
        logger.info("Received MPESA callback: {}", callback);

        if (callback.getBody().getStkCallback().getResultCode() == 0) {
            String transactionId = callback.getBody().getStkCallback().getCheckoutRequestID();

            PackageAssignment pending = packageAssignmentRepository.findByCheckoutRequestId(transactionId).orElse(null);

            if (pending != null) {
                String phoneNumber = callback.getBody().getStkCallback().getCallbackMetadata().getItem().stream()
                        .filter(item -> item.getName().equals("PhoneNumber"))
                        .map(item -> item.getValue().toString())
                        .findFirst()
                        .orElse(null);

                if (phoneNumber != null) {
                    GuestSession session = new GuestSession();
                    session.setPhoneNumber(phoneNumber);
                    session.setStartTime(null);  // Set start time properly based on your business logic
                    session.setActive(true);
                    session.setPaid(true);
                    guestSessionRepository.save(session);

                    pending.setActive(true);
                    packageAssignmentRepository.save(pending);

                    logger.info("✅ Guest session started for phone: {}", phoneNumber);
                } else {
                    logger.warn("⚠️ Phone number missing in MPESA callback metadata");
                }
            } else {
                logger.warn("⚠️ No pending assignment found for transaction: {}", transactionId);
            }
        } else {
            logger.warn("❌ MPESA callback failed with result code: {}", callback.getBody().getStkCallback().getResultCode());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for username: {}", request.getUsername());
        User user = userRepository.findById(request.getUsername()).orElse(null);
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setActive(true);
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse("success", "Login successful"));
        }
        return ResponseEntity.status(401).body(new ApiResponse("error", "Invalid credentials"));
    }

    @PostMapping("/guest/start")
    @Transactional
    public ResponseEntity<ApiResponse> startGuest(@RequestParam String ip, @RequestParam String mac) {
        GuestSession session = new GuestSession();
        session.setIpAddress(ip);
        session.setMacAddress(mac);
        session.setStartTime(LocalDateTime.now());
        session.setActive(false);
        session.setPaid(false);
        guestSessionRepository.save(session);
        return ResponseEntity.ok(new ApiResponse("success", "Guest session created. Please complete payment to activate."));
    }

    @PostMapping("/create_account")
    @Transactional
    public ResponseEntity<ApiResponse> createAccount(@Valid @RequestBody LoginRequest request) {
        logger.info("Creating account for username: {}", request.getUsername());
        if (userRepository.existsById(request.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Username already exists"));
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("success", "Account created successfully"));
    }

    @PostMapping("/activate")
    @Transactional
    public ResponseEntity<ApiResponse> activateVoucher(@Valid @RequestBody VoucherRequest request) {
        logger.info("Activating voucher: {}", request.getVoucherCode());
        Voucher voucher = voucherRepository.findById(request.getVoucherCode()).orElse(null);
        if (voucher != null && !voucher.isUsed()) {
            voucher.setUsed(true);
            voucherRepository.save(voucher);
            return ResponseEntity.ok(new ApiResponse("success", "Voucher " + voucher.getCode() + " activated for "
                + voucher.getPackageType()));
        }
        return ResponseEntity.badRequest().body(new ApiResponse("error", "Invalid or used voucher"));
    }

    @PostMapping("/activate_package")
    @Transactional
    public ResponseEntity<ApiResponse> activatePackage(@Valid @RequestBody PackageActivationRequest request) {
        logger.info("Activating package {} for user: {}", request.getPackageType(), request.getUsername());
        PackageInfo packageInfo = packageInfoRepository.findByType(request.getPackageType().toLowerCase()).orElse(null);
        if (packageInfo == null) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Invalid package type"));
        }
        User user = userRepository.findById(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "User not found"));
        }
        packageAssignmentRepository.findByUsernameAndActiveTrue(request.getUsername())
            .ifPresent(existing -> {
                existing.setActive(false);
                packageAssignmentRepository.save(existing);
            });
        PackageAssignment assignment = new PackageAssignment();
        assignment.setUsername(request.getUsername());
        assignment.setPackageType(packageInfo.getType());
        assignment.setBandwidthMbps(packageInfo.getBandwidthMbps());
        assignment.setDurationHours(packageInfo.getDurationHours());
        assignment.setStartTime(LocalDateTime.now());
        assignment.setActive(true);
        packageAssignmentRepository.save(assignment);
        return ResponseEntity.ok(new ApiResponse("success",
            String.format("Package %s activated for %s: %d Mbps for %d hours.",
                packageInfo.getType(), request.getUsername(), packageInfo.getBandwidthMbps(), packageInfo.getDurationHours())));
    }

    @PostMapping("/reconnect")
    @Transactional
    public ResponseEntity<ApiResponse> reconnect(@Valid @RequestBody ReconnectRequest request) {
        logger.info("Reconnecting with MPESA code: {}", request.getMpesaCode());
        PackageAssignment assignment = packageAssignmentRepository.findByCheckoutRequestId(request.getMpesaCode()).orElse(null);
        if (assignment == null) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Invalid MPESA transaction code"));
        }
        if (assignment.isActive()) {
            return ResponseEntity.ok(new ApiResponse("success", "Session already active"));
        }
        assignment.setActive(true);
        assignment.setStartTime(LocalDateTime.now());
        packageAssignmentRepository.save(assignment);
        return ResponseEntity.ok(new ApiResponse("success", "Reconnect successful"));
    }
}

class PackageRequest {
    private int amount;
    private int duration;
    private int bandwidth;

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getBandwidth() { return bandwidth; }
    public void setBandwidth(int bandwidth) { this.bandwidth = bandwidth; }
}

class ReconnectRequest {
    private String mpesaCode;
    public String getMpesaCode() { return mpesaCode; }
    public void setMpesaCode(String mpesaCode) { this.mpesaCode = mpesaCode; }
}