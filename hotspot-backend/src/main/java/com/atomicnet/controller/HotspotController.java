package com.atomicnet.controller;

import com.atomicnet.dto.ApiResponse;
import com.atomicnet.dto.LoginRequest;
import com.atomicnet.dto.MpesaCallback;
import com.atomicnet.dto.PackageActivationRequest;
import com.atomicnet.dto.PaymentRequest;
import com.atomicnet.dto.VoucherRequest;
import com.atomicnet.entity.PackageAssignment;
import com.atomicnet.entity.PackageInfo;
import com.atomicnet.entity.User;
import com.atomicnet.entity.Voucher;
import com.atomicnet.repository.PackageAssignmentRepository;
import com.atomicnet.repository.UserRepository;
import com.atomicnet.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import okhttp3.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HotspotController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private PackageAssignmentRepository packageAssignmentRepository;

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.callback-url}")
    private String callbackUrl;

    private static final Map<String, PackageInfo> PACKAGES = new HashMap<>();
    static {
        PACKAGES.put("one_hour", new PackageInfo("one_hour", 10, 1, 5));
        PACKAGES.put("two_hour", new PackageInfo("two_hour", 15, 2, 5));
        PACKAGES.put("four_hour", new PackageInfo("four_hour", 25, 4, 5));
        PACKAGES.put("six_hour", new PackageInfo("six_hour", 30, 6, 5));
        PACKAGES.put("one_day", new PackageInfo("one_day", 40, 24, 5));
        PACKAGES.put("two_day", new PackageInfo("two_day", 70, 48, 5));
        PACKAGES.put("weekly", new PackageInfo("weekly", 250, 168, 5));
        PACKAGES.put("monthly", new PackageInfo("monthly", 900, 720, 5));
    }

    private final OkHttpClient client = new OkHttpClient();

    @PostMapping("/package/{type}")
    public ResponseEntity<ApiResponse> selectPackage(@PathVariable String type) {
        if (type == null || type.trim().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Package type is required"));
        }
        PackageInfo packageInfo = PACKAGES.get(type);
        if (packageInfo == null) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Invalid package type"));
        }
        return ResponseEntity.ok(new ApiResponse("success",
            String.format("Package %s selected. Please pay Ksh.%d via MPESA to activate %d Mbps for %d hours.",
                packageInfo.getType(), packageInfo.getPrice(), packageInfo.getBandwidthMbps(), packageInfo.getDurationHours())));
    }

    @PostMapping("/initiate_payment")
    public ResponseEntity<ApiResponse> initiatePayment(@org.springframework.web.bind.annotation.RequestBody PaymentRequest request) throws IOException {
        if (request == null || request.getPhoneNumber() == null || !request.getPhoneNumber().matches("\\+2547\\d{8}")) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Valid MPESA phone number is required"));
        }
        PackageInfo packageInfo = PACKAGES.get(request.getPackageType());
        if (packageInfo == null) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Invalid package type"));
        }
        if (!userRepository.existsById(request.getPhoneNumber())) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Phone number not registered. Please create an account."));
        }

        // Get OAuth token
        String auth = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes());
        Request tokenRequest = new Request.Builder()
            .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
            .header("Authorization", "Basic " + auth)
            .build();
        Response tokenResponse = client.newCall(tokenRequest).execute();
        if (!tokenResponse.isSuccessful()) {
            return ResponseEntity.status(500).body(new ApiResponse("error", "Failed to authenticate with MPESA"));
        }
        String accessToken = tokenResponse.body().string().split("\"access_token\":\"")[1].split("\"")[0];

        // Initiate STK push
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = Base64.getEncoder().encodeToString((shortcode + passkey + timestamp).getBytes());
        String transactionId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String payload = String.format(
            "{\"BusinessShortCode\":\"%s\",\"Password\":\"%s\",\"Timestamp\":\"%s\",\"TransactionType\":\"CustomerPayBillOnline\"," +
            "\"Amount\":\"%d\",\"PartyA\":\"%s\",\"PartyB\":\"%s\",\"PhoneNumber\":\"%s\",\"CallBackURL\":\"%s\"," +
            "\"AccountReference\":\"Atomicnet\",\"TransactionDesc\":\"Payment for %s\"}",
            shortcode, password, timestamp, packageInfo.getPrice(), request.getPhoneNumber(), shortcode,
            request.getPhoneNumber(), callbackUrl, packageInfo.getType());

        Request stkRequest = new Request.Builder()
            .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
            .post(okhttp3.RequestBody.create(payload, MediaType.parse("application/json")))
            .header("Authorization", "Bearer " + accessToken)
            .build();
        Response stkResponse = client.newCall(stkRequest).execute();
        if (!stkResponse.isSuccessful()) {
            return ResponseEntity.status(500).body(new ApiResponse("error", "Failed to initiate STK push"));
        }

        // Store pending payment
        PackageAssignment pending = new PackageAssignment();
        pending.setUsername("pending_" + transactionId);
        pending.setPackageType(packageInfo.getType());
        pending.setBandwidthMbps(packageInfo.getBandwidthMbps());
        pending.setDurationHours(packageInfo.getDurationHours());
        pending.setStartTime(LocalDateTime.now());
        pending.setActive(false);
        packageAssignmentRepository.save(pending);

        return ResponseEntity.ok(new ApiResponse("success", "STK push initiated. Please check your phone."));
    }

    @PostMapping("/mpesa/callback")
    public ResponseEntity<Void> handleMpesaCallback(@org.springframework.web.bind.annotation.RequestBody MpesaCallback callback) {
        if (callback.getBody().getStkCallback().getResultCode() == 0) {
            String transactionId = callback.getBody().getStkCallback().getCheckoutRequestID();
            PackageAssignment pending = packageAssignmentRepository.findByUsername("pending_" + transactionId).orElse(null);
            if (pending != null) {
                String username = callback.getBody().getStkCallback().getCallbackMetadata().getItem().stream()
                    .filter(item -> item.getName().equals("PhoneNumber"))
                    .map(item -> item.getValue().toString())
                    .findFirst()
                    .orElse(null);
                if (username != null && userRepository.existsById(username)) {
                    packageAssignmentRepository.findByUsernameAndActiveTrue(username)
                        .ifPresent(existing -> {
                            existing.setActive(false);
                            packageAssignmentRepository.save(existing);
                        });
                    pending.setUsername(username);
                    pending.setActive(true);
                    pending.setStartTime(LocalDateTime.now());
                    packageAssignmentRepository.save(pending);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
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
    public ResponseEntity<ApiResponse> createAccount(@org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
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
    public ResponseEntity<ApiResponse> activateVoucher(@org.springframework.web.bind.annotation.RequestBody VoucherRequest request) {
        if (request == null || request.getVoucherCode() == null || request.getVoucherCode().trim().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Voucher code is required"));
        }
        Voucher voucher = voucherRepository.findById(request.getVoucherCode()).orElse(null);
        if (voucher != null && !voucher.isUsed()) {
            voucher.setUsed(true);
            voucherRepository.save(voucher);
            return ResponseEntity.ok(new ApiResponse("success", "Voucher " + voucher.getCode() + " activated for "

 + voucher.getPackageType()));
        }
        return ResponseEntity.status(400).body(new ApiResponse("error", "Invalid or used voucher"));
    }

    @PostMapping("/activate_package")
    public ResponseEntity<ApiResponse> activatePackage(@org.springframework.web.bind.annotation.RequestBody PackageActivationRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPackageType() == null || request.getPackageType().trim().isEmpty()) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Username and package type are required"));
        }
        PackageInfo packageInfo = PACKAGES.get(request.getPackageType());
        if (packageInfo == null) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "Invalid package type"));
        }
        User user = userRepository.findById(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(400).body(new ApiResponse("error", "User not found"));
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
}