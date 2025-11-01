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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReceivingController.class)
class ReceivingControllerDoneTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceivingService receivingService;

    @Test
    @DisplayName("GET /api/v1/receiving/done - COMPLETED_* 만 반환 (ApiResponse 래핑)")
    void getDone_success() throws Exception {
        var list = List.of(
                new ReceivingNoteSummaryResponse(201L, "ABC Supply", 1, 10, "COMPLETED_OK", "2025-10-20T08:00:00Z", "2025-10-20T10:00:00Z"),
                new ReceivingNoteSummaryResponse(202L, "ABC Supply", 1, 10, "COMPLETED_ISSUE", "2025-10-20T13:00:00Z", "2025-10-20T15:00:00Z")
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
