package com.atomicnet.dto;
//Purpose: DTO for handling MPESA callback data. This is more complex due to nested structures inferred from the callback handler.


import java.util.List;

public class MpesaCallback {
    private Body body;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public static class Body {
        private StkCallback stkCallback;

        public StkCallback getStkCallback() {
            return stkCallback;
        }

        public void setStkCallback(StkCallback stkCallback) {
            this.stkCallback = stkCallback;
        }
    }

    public static class StkCallback {
        private int resultCode;
        private String checkoutRequestID;
        private CallbackMetadata callbackMetadata;

        public int getResultCode() {
            return resultCode;
        }

        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public String getCheckoutRequestID() {
            return checkoutRequestID;
        }

        public void setCheckoutRequestID(String checkoutRequestID) {
            this.checkoutRequestID = checkoutRequestID;
        }

        public CallbackMetadata getCallbackMetadata() {
            return callbackMetadata;
        }

        public void setCallbackMetadata(CallbackMetadata callbackMetadata) {
            this.callbackMetadata = callbackMetadata;
        }
    }

    public static class CallbackMetadata {
        private List<Item> item;

        public List<Item> getItem() {
            return item;
        }

        public void setItem(List<Item> item) {
            this.item = item;
        }
    }

    public static class Item {
        private String name;
        private Object value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
