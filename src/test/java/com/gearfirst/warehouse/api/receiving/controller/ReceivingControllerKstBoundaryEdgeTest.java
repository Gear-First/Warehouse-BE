package com.gearfirst.warehouse.api.receiving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
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
@WebMvcTest(controllers = ReceivingController.class)
class ReceivingControllerKstBoundaryEdgeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceivingService receivingService;

    @Test
    @DisplayName("GET /receiving/done: 단일 날짜(KST) 조회 시 +09:00 문자열 직렬화 예시를 반환 래핑")
    void done_singleDate_rendersKstStrings() throws Exception {
        // Given: the service returns items whose timestamps are already KST strings (+09:00)
        var list = List.of(
                new ReceivingNoteSummaryResponse(201L, "IN-KST-001", "공급A", 1, 10, "COMPLETED_OK", "WH1",
                        "2025-11-02T00:00:00+09:00", // KST midnight start
                        "2025-11-04T00:00:00+09:00",
                        "2025-11-02T23:59:59+09:00") // KST end of day (string example)
        );
        when(receivingService.getDone(eq("2025-11-02"))).thenReturn(list);

        // When
        mockMvc.perform(get("/api/v1/receiving/done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].requestedAt", endsWith("+09:00")))
                .andExpect(jsonPath("$.data.items[0].completedAt", endsWith("+09:00")));

        // And: verify single-date overload used
        verify(receivingService, times(1)).getDone(eq("2025-11-02"));
        verify(receivingService, times(0)).getDone(anyString(), anyString(), anyString(), anyString());
    }
}
