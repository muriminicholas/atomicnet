package com.atomicnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

@SpringBootApplication(scanBasePackages = "com.atomicnet")
public class HotspotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotspotBackendApplication.class, args);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}