package com.gearfirst.warehouse.api.receiving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.*;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReceivingController.class)
class ReceivingControllerUpdateAndCompleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceivingService receivingService;

    @Test
    @DisplayName("PATCH /api/v1/receiving/{noteId}/lines/{lineId} - hasIssue=false이면 ACCEPTED로 전이하고 note는 IN_PROGRESS")
    void updateLine_doneOk_success() throws Exception {
        // given
        var req = new UpdateLineRequest(18, false);
        var lines = List.of(new ReceivingNoteLineResponse(10L,
                new ReceivingProductResponse(4L, "LOT-P-2001", "P-2001", "스페이서", "/img/p2001"),
                20, 18, 0, "ACCEPTED"));
        var detail = new ReceivingNoteDetailResponse(102L, "BCD Parts", 2, 45, "IN_PROGRESS", null, lines);
        when(receivingService.updateLine(eq(102L), eq(10L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class))).thenReturn(detail);

        // when & then
        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 102L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.lines[0].status", is("ACCEPTED")))
                .andExpect(jsonPath("$.data.lines[0].inspectedQty", is(18)))
                .andExpect(jsonPath("$.data.lines[0].issueQty", is(0)));
    }

    @Test
    @DisplayName("PATCH /api/v1/receiving/{noteId}/lines/{lineId} - hasIssue=true이면 REJECTED로 전이하고 issueQty=ordered-inspected")
    void updateLine_doneIssue_success() throws Exception {
        // given
        var req = new UpdateLineRequest(20, true);
        var lines = List.of(new ReceivingNoteLineResponse(11L,
                new ReceivingProductResponse(5L, "LOT-P-2002", "P-2002", "클립", "/img/p2002"),
                25, 20, 5, "REJECTED"));
        var detail = new ReceivingNoteDetailResponse(102L, "BCD Parts", 2, 45, "IN_PROGRESS", null, lines);
        when(receivingService.updateLine(eq(102L), eq(11L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class))).thenReturn(detail);

        // when & then
        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 102L, 11L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.lines[0].status", is("REJECTED")))
                .andExpect(jsonPath("$.data.lines[0].inspectedQty", is(20)))
                .andExpect(jsonPath("$.data.lines[0].issueQty", is(5)));
    }

    @Test
    @DisplayName("PATCH /api/v1/receiving/{noteId}/lines/{lineId} - inspectedQty > orderedQty면 400")
    void updateLine_validationError_inspectedExceedsOrdered() throws Exception {
        var req = new UpdateLineRequest(1000, false);
        when(receivingService.updateLine(eq(101L), eq(3L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class)))
                .thenThrow(new BadRequestException(ErrorStatus.RECEIVING_ORDERED_QTY_EXCEEDS_INSPECTED_QTY));

        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 101L, 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("PATCH /api/v1/receiving/{noteId}/lines/{lineId} - 완료 라인 재수정 시 409")
    void updateLine_conflict_whenLineAlreadyDone() throws Exception {
        var req = new UpdateLineRequest(40, false);
        when(receivingService.updateLine(eq(101L), eq(2L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class)))
                .thenThrow(new ConflictException(ErrorStatus.CONFLICT_RECEIVING_LINE_ALREADY_DONE));

        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 101L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("POST /api/v1/receiving/{noteId}:complete - 라인이 모두 ACCEPTED/REJECTED일 때 완료되고 appliedQtyTotal은 ACCEPTED 합")
    void complete_success() throws Exception {
        // stub two updates (responses are not asserted deeply in this test)
        when(receivingService.updateLine(eq(102L), eq(10L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class)))
                .thenReturn(new ReceivingNoteDetailResponse(102L, "BCD Parts", 2, 45, "IN_PROGRESS", null, List.of()));
        when(receivingService.updateLine(eq(102L), eq(11L), org.mockito.ArgumentMatchers.any(UpdateLineRequest.class)))
                .thenReturn(new ReceivingNoteDetailResponse(102L, "BCD Parts", 2, 45, "IN_PROGRESS", null, List.of()));
        when(receivingService.complete(102L))
                .thenReturn(new ReceivingCompleteResponse("2025-10-27T05:00:00Z", 20));

        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 102L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLineRequest(20, false))))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/receiving/{noteId}/lines/{lineId}", 102L, 11L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLineRequest(0, true))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/receiving/{noteId}:complete", 102L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.completedAt", notNullValue()))
                .andExpect(jsonPath("$.data.appliedQtyTotal", is(20)));
    }

    @Test
    @DisplayName("POST /api/v1/receiving/{noteId}:complete - 진행 중 라인이 있으면 409")
    void complete_conflict_whenNotAllDone() throws Exception {
        when(receivingService.complete(101L))
                .thenThrow(new ConflictException(ErrorStatus.CONFLICT_RECEIVING_CANNOT_COMPLETE_WHEN_NOT_DONE));

        mockMvc.perform(post("/api/v1/receiving/{noteId}:complete", 101L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(409)));
    }
}
