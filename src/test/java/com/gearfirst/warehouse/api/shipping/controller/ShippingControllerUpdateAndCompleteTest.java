package com.gearfirst.warehouse.api.shipping.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteLineResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingProductResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
        var req = new ShippingUpdateLineRequest(10);
        var lines = List.of(new ShippingNoteLineResponse(1L,
                new ShippingProductResponse(11L, "LOT-A", "S-01", "볼트", "/img"),
                10, 10, "READY"));
        var detail = new ShippingNoteDetailResponse(4001L, "ACME", 1, 10, "IN_PROGRESS", null,
                null, null, null, null, null, null, null, null, null,
                lines);
        when(shippingService.updateLine(eq(4001L), eq(1L),
                ArgumentMatchers.any(ShippingUpdateLineRequest.class))).thenReturn(detail);

        // when & then
        mockMvc.perform(patch("/api/v1/shipping/{noteId}/lines/{lineId}", 4001L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(
                        jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.lines[0].status", is("READY")));
    }

    @Test
    @DisplayName("POST /api/v1/shipping/{noteId}:complete - READY만 있으면 COMPLETED와 completedAt 반환 (ApiResponse 래핑)")
    void complete_completed() throws Exception {
        // given
        var resp = new ShippingCompleteResponse("2025-10-22T10:00:00Z", 100);
        when(shippingService.complete(eq(5001L),
                org.mockito.ArgumentMatchers.any(ShippingCompleteRequest.class))).thenReturn(resp);

        // when & then
        var completeReq = ShippingCompleteRequest.builder()
                .assigneeName("김담당").assigneeDept("물류팀").assigneePhone("010-9876-5432").build();
        mockMvc.perform(post("/api/v1/shipping/{noteId}:complete", 5001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_COMPLETE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.completedAt", notNullValue()))
                .andExpect(jsonPath("$.data.totalShippedQty", is(100)));
    }

    @Test
    @DisplayName("POST /api/v1/shipping/{noteId}:complete - SHORTAGE 포함이면 DELAYED(completedAt 포함)로 처리 (ApiResponse 래핑)")
    void complete_delayed() throws Exception {
        // given
        var resp = new ShippingCompleteResponse("2025-10-22T11:00:00Z", 80);
        when(shippingService.complete(eq(5002L),
                org.mockito.ArgumentMatchers.any(ShippingCompleteRequest.class))).thenReturn(resp);

        // when & then
        var completeReq = ShippingCompleteRequest.builder()
                .assigneeName("김담당").assigneeDept("물류팀").assigneePhone("010-9876-5432").build();
        mockMvc.perform(post("/api/v1/shipping/{noteId}:complete", 5002L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_COMPLETE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.completedAt", notNullValue()))
                .andExpect(jsonPath("$.data.totalShippedQty", is(80)));
    }
}
