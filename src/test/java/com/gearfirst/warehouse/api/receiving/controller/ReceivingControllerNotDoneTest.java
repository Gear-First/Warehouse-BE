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
class ReceivingControllerNotDoneTest {

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
    @DisplayName("GET /api/v1/receiving/not-done - PENDING/IN_PROGRESS만 반환 (ApiResponse 래핑)")
    void getNotDone_success() throws Exception {
        var list = List.of(
                new ReceivingNoteSummaryResponse(101L, "IN-WH1-20251020-001", "ABC Supply", 3, 120, "PENDING", "WH1", "2025-10-20T09:00:00Z", "2025-10-22T09:00:00Z", null),
                new ReceivingNoteSummaryResponse(102L, "IN-WH1-20251020-002", "BCD Parts", 2, 45, "IN_PROGRESS", "WH1", "2025-10-20T09:05:00Z", "2025-10-22T09:05:00Z", null)
        );
        when(receivingService.getNotDone(any())).thenReturn(list);

        mockMvc.perform(get("/api/v1/receiving/not-done").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].status", anyOf(is("PENDING"), is("IN_PROGRESS"))))
                .andExpect(jsonPath("$.data.items[1].status", anyOf(is("PENDING"), is("IN_PROGRESS"))))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(2)));
    }
}
