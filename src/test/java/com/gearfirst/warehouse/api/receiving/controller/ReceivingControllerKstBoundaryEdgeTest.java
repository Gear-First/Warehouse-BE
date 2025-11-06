package com.gearfirst.warehouse.api.receiving.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.service.ReceivingQueryService;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.exception.GlobalExceptionHandler;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ReceivingControllerKstBoundaryEdgeTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReceivingService receivingService;

    @Mock
    private ReceivingQueryService receivingQueryService;

    @BeforeEach
    void setup() {
        ReceivingController controller = new ReceivingController(receivingService, receivingQueryService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /receiving/done: 단일 날짜(KST) 조회 시 +09:00 문자열 직렬화 예시를 반환 래핑")
    void done_singleDate_rendersKstStrings() throws Exception {
        // Given: the service returns items whose timestamps are already KST strings (+09:00)
        var list = List.of(
                new ReceivingNoteSummaryResponse(201L, "IN-KST-001", "공급A", 1, 10, "COMPLETED_OK", "WH1",
                        "2025-11-02T00:00:00+09:00",
                        "2025-11-04T00:00:00+09:00",
                        "2025-11-02T23:59:59+09:00")
        );
        when(receivingService.getDone(eq("2025-11-02"))).thenReturn(list);

        mockMvc.perform(get("/api/v1/receiving/done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].requestedAt", endsWith("+09:00")))
                .andExpect(jsonPath("$.data.items[0].completedAt", endsWith("+09:00")));

        verify(receivingService, times(1)).getDone(eq("2025-11-02"));
        verify(receivingService, times(0)).getDone(anyString(), anyString(), anyString(), anyString());
    }
}
