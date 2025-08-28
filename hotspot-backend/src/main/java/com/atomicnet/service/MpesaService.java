package com.atomicnet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class MpesaService {
    private static final Logger logger = LoggerFactory.getLogger(MpesaService.class);

    @Autowired
    private OkHttpClient client;

    @Autowired
    private ObjectMapper mapper;

    public String getAccessToken(String consumerKey, String consumerSecret) throws IOException {
        logger.info("Fetching MPESA access token");
        String auth = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes());
        Request request = new Request.Builder()
            .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
            .header("Authorization", "Basic " + auth)
            .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to authenticate with MPESA: {}", response.body().string());
                throw new IOException("Failed to authenticate with MPESA");
            }
            Map<String, String> tokenData = mapper.readValue(response.body().string(), new TypeReference<Map<String, String>>() {});
            String accessToken = tokenData.get("access_token");
            if (accessToken == null) {
                logger.error("Access token not found in response");
                throw new IOException("Failed to parse MPESA token");
            }
            return accessToken;
        }
    }

    public void initiateStkPush(String accessToken, String shortcode, String passkey, String callbackUrl,
                                String phoneNumber, int amount, String packageType, String transactionId) throws IOException {
        logger.info("Initiating STK push for transaction: {}", transactionId);
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = Base64.getEncoder().encodeToString((shortcode + passkey + timestamp).getBytes());
        String payload = String.format(
            "{\"BusinessShortCode\":\"%s\",\"Password\":\"%s\",\"Timestamp\":\"%s\",\"TransactionType\":\"CustomerPayBillOnline\"," +
            "\"Amount\":\"%d\",\"PartyA\":\"%s\",\"PartyB\":\"%s\",\"PhoneNumber\":\"%s\",\"CallBackURL\":\"%s\"," +
            "\"AccountReference\":\"Atomicnet\",\"TransactionDesc\":\"Payment for %s\"}",
            shortcode, password, timestamp, amount, phoneNumber, shortcode, phoneNumber, callbackUrl, packageType);

        Request request = new Request.Builder()
            .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
            .post(RequestBody.create(payload, MediaType.parse("application/json")))
            .header("Authorization", "Bearer " + accessToken)
            .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("STK push failed: {}", response.body().string());
                throw new IOException("Failed to initiate STK push");
            }
        }
    }
}