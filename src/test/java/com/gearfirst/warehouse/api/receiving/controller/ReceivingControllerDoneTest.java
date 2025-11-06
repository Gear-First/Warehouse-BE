package com.gearfirst.warehouse.api.receiving.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.service.ReceivingQueryService;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.exception.GlobalExceptionHandler;
import com.gearfirst.warehouse.common.response.PageEnvelope;
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
class ReceivingControllerDoneTest {

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
    @DisplayName("GET /api/v1/receiving/done - COMPLETED_* 만 반환 (ApiResponse 래핑)")
    void getDone_success() throws Exception {
        var list = List.of(
                new ReceivingNoteSummaryResponse(201L, "IN-WH1-20251020-010", "ABC Supply", 1, 10, "COMPLETED_OK", "WH1", "2025-10-20T08:00:00Z", "2025-10-22T08:00:00Z", "2025-10-20T10:00:00Z"),
                new ReceivingNoteSummaryResponse(202L, "IN-WH1-20251020-011", "ABC Supply", 1, 10, "COMPLETED_ISSUE", "WH1", "2025-10-20T13:00:00Z", "2025-10-22T13:00:00Z", "2025-10-20T15:00:00Z")
        );
        when(receivingService.getDone(any())).thenReturn(list);

        mockMvc.perform(get("/api/v1/receiving/done").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].status", startsWith("COMPLETED")))
                .andExpect(jsonPath("$.data.items[1].status", startsWith("COMPLETED")))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(2)));
    }
}
