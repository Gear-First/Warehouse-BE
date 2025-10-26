package com.gearfirst.warehouse.api.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.*;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerUpdateAndCompleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("PATCH /api/v1/shipping/{noteId}/lines/{lineId} - 성공적으로 라인을 업데이트한다")
    void updateLine_success() throws Exception {
        // given
        var req = new UpdateLineRequest(10, 8);
        var lines = List.of(new ShippingNoteLineResponse(1L,
                new ShippingProductResponse(11L, "LOT-A", "S-01", "볼트", "/img"),
                10, 10, 8, "READY"));
        var detail = new ShippingNoteDetailResponse(4001L, "ACME", 1, 10, "IN_PROGRESS", null, lines);
        when(shippingService.updateLine(eq(4001L), eq(1L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class))).thenReturn(detail);

        // when & then
        mockMvc.perform(patch("/api/v1/shipping/{noteId}/lines/{lineId}", 4001L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.lines[0].status", is("READY")));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipping/{noteId}/lines/{lineId} - pickedQty > allocatedQty면 422를 반환한다")
    void updateLine_validationError() throws Exception {
        // given
        var req = new UpdateLineRequest(5, 8);
        when(shippingService.updateLine(eq(4002L), eq(1L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class)))
                .thenThrow(new BadRequestException(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY));

        // when & then
        mockMvc.perform(patch("/api/v1/shipping/{noteId}/lines/{lineId}", 4002L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY.getStatusCode()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY.getStatusCode())))
                .andExpect(jsonPath("$.message", is(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY.getMessage())));
    }

    @Test
    @DisplayName("POST /api/v1/shipping/{noteId}:complete - READY만 있으면 COMPLETED와 completedAt 반환")
    void complete_completed() throws Exception {
        // given
        var resp = new ShippingCompleteResponse("2025-10-22T10:00:00Z", 100);
        when(shippingService.complete(5001L)).thenReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/shipping/{noteId}:complete", 5001L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt", notNullValue()))
                .andExpect(jsonPath("$.totalShippedQty", is(100)));
    }

    @Test
    @DisplayName("POST /api/v1/shipping/{noteId}:complete - SHORTAGE 포함이면 DELAYED(completedAt 포함)로 처리")
    void complete_delayed() throws Exception {
        // given
        var resp = new ShippingCompleteResponse("2025-10-22T11:00:00Z", 80);
        when(shippingService.complete(5002L)).thenReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/shipping/{noteId}:complete", 5002L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt", notNullValue()))
                .andExpect(jsonPath("$.totalShippedQty", is(80)));
    }
}
