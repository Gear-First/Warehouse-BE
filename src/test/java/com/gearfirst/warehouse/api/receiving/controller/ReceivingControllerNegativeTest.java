package com.gearfirst.warehouse.api.receiving.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.service.ReceivingQueryService;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.exception.GlobalExceptionHandler;
import com.gearfirst.warehouse.common.exception.NotFoundException;
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
class ReceivingControllerNegativeTest {

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
    @DisplayName("GET /api/v1/receiving/{noteId} - 존재하지 않으면 404 반환")
    void getDetail_notFound() throws Exception {
        when(receivingService.getDetail(anyLong())).thenThrow(new NotFoundException("Receiving note not found: 9999"));

        mockMvc.perform(get("/api/v1/receiving/{noteId}", 9999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PATCH /api/v1/receiving/{noteId}/lines/{lineId} - Bean Validation 실패 시 400 반환")
    void updateLine_validationBeanError() throws Exception {
        // inspectedQty 누락, hasIssue 누락
        var invalidJson = "{ }";

        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 100L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)));
    }
}
