package com.gearfirst.warehouse.api.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerKstBoundaryEdgeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("GET /shipping/done: 단일 날짜(KST) 조회 시 +09:00 문자열 직렬화 예시를 반환 래핑")
    void done_singleDate_rendersKstStrings() throws Exception {
        // Given: the service returns items whose timestamps are KST strings (+09:00)
        var list = List.of(
                new ShippingNoteSummaryResponse(301L, "OUT-KST-001", "납품처A", 1, 10, "COMPLETED", "WH1",
                        "2025-11-02T00:00:00+09:00", // KST midnight
                        "2025-11-04T00:00:00+09:00",
                        "2025-11-02T23:59:59+09:00")
        );
        when(shippingService.getDone(eq("2025-11-02"))).thenReturn(list);

        mockMvc.perform(get("/api/v1/shipping/done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].requestedAt", endsWith("+09:00")))
                .andExpect(jsonPath("$.data.items[0].completedAt", endsWith("+09:00")));

        verify(shippingService, times(1)).getDone(eq("2025-11-02"));
        verify(shippingService, times(0)).getDone(anyString(), anyString(), anyString(), anyString());
    }
}
