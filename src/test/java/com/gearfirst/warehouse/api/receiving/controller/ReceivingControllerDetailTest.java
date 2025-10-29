package com.gearfirst.warehouse.api.receiving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.*;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReceivingController.class)
class ReceivingControllerDetailTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceivingService receivingService;

    @Test
    @DisplayName("GET /api/v1/receiving/{noteId} - 상세 성공(ApiResponse 래핑)")
    void getDetail_success() throws Exception {
        var lines = List.of(new ReceivingNoteLineResponse(1L,
                new ReceivingProductResponse(1L, "LOT-A", "P-1001", "오일필터", "/img"),
                50, 48, 2, "REJECTED"));
        var detail = new ReceivingNoteDetailResponse(101L, "ABC Supply", 3, 120, "IN_PROGRESS", null,
                        null, null, null, null, null, null, null, null, null,
                        lines);
        when(receivingService.getDetail(anyLong())).thenReturn(detail);

        mockMvc.perform(get("/api/v1/receiving/{noteId}", 101L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_RECEIVING_NOTE_DETAIL_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.noteId", is(101)))
                .andExpect(jsonPath("$.data.lines", hasSize(1)))
                .andExpect(jsonPath("$.data.lines[0].status", anyOf(is("PENDING"), is("ACCEPTED"), is("REJECTED"))));
    }
}
