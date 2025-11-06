package com.gearfirst.warehouse.api.receiving.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.*;
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
class ReceivingControllerDetailTest {

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
    @DisplayName("GET /api/v1/receiving/{noteId} - 상세 성공(ApiResponse 래핑)")
    void getDetail_success() throws Exception {
        var lines = List.of(new ReceivingNoteLineResponse(1L,
                new ReceivingProductResponse(1L, "LOT-A", "P-1001", "오일필터", "/img"),
                50, 48, "REJECTED"));
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
