package com.atomicnet.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mpesa")
public class MpesaConfig {

    private String consumerKey;
    private String consumerSecret;
    private String shortcode;
    private String passkey;
    private String callbackUrl;

    // Getters and Setters
    public String getConsumerKey() { return consumerKey; }
    public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }
    public String getConsumerSecret() { return consumerSecret; }
    public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }
    public String getShortcode() { return shortcode; }
    public void setShortcode(String shortcode) { this.shortcode = shortcode; }
    public String getPasskey() { return passkey; }
    public void setPasskey(String passkey) { this.passkey = passkey; }
    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    @PostConstruct
    public void validateProperties() {
        if (consumerKey == null || consumerKey.isEmpty()) throw new IllegalStateException("MPESA consumerKey missing");
        if (consumerSecret == null || consumerSecret.isEmpty()) throw new IllegalStateException("MPESA consumerSecret missing");
        if (shortcode == null || shortcode.isEmpty()) throw new IllegalStateException("MPESA shortcode missing");
        if (passkey == null || passkey.isEmpty()) throw new IllegalStateException("MPESA passkey missing");
        if (callbackUrl == null || callbackUrl.isEmpty()) throw new IllegalStateException("MPESA callbackUrl missing");
    }
}


