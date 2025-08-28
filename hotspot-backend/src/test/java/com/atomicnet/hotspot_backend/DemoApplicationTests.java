package com.atomicnet.hotspot_backend;

import com.atomicnet.service.MpesaService;
import com.atomicnet.controller.HotspotController;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;



@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

    @MockBean
    private MpesaService mpesaService;

    @Autowired
    private HotspotController hotspotController;

    @Test
    void contextLoads() throws IOException {
        // Mock the OkHttpClient behavior
        OkHttpClient okHttpClient = Mockito.mock(OkHttpClient.class);

        Response mockTokenResponse = Mockito.mock(Response.class);
        ResponseBody mockTokenBody = Mockito.mock(ResponseBody.class);
        when(mockTokenBody.string()).thenReturn("{\"access_token\":\"mockAccessToken\"}");
        when(mockTokenResponse.isSuccessful()).thenReturn(true);
        when(mockTokenResponse.body()).thenReturn(mockTokenBody);

        Response mockStkResponse = Mockito.mock(Response.class);
        when(mockStkResponse.isSuccessful()).thenReturn(true);

        okhttp3.Call mockCall = Mockito.mock(okhttp3.Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute())
            .thenReturn(mockTokenResponse)
            .thenReturn(mockStkResponse);

        assertNotNull(hotspotController);
    }
}