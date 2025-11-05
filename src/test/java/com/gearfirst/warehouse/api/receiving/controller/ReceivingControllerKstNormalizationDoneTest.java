package com.gearfirst.warehouse.api.receiving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@WebMvcTest(controllers = ReceivingController.class)
class ReceivingControllerKstNormalizationDoneTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceivingService receivingService;

    @Test
    @DisplayName("GET /receiving/done: range 우선 및 역전 스왑")
    void done_rangeWins_andSwap() throws Exception {
        when(receivingService.getDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/receiving/done")
                        .queryParam("date", "2025-11-01")
                        .queryParam("dateFrom", "2025-11-03")
                        .queryParam("dateTo", "2025-11-02")
                        .queryParam("warehouseCode", "서울")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> wcCaptor = ArgumentCaptor.forClass(String.class);
        verify(receivingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), wcCaptor.capture());

        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-03", toCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("서울", wcCaptor.getValue());
    }

    @Test
    @DisplayName("GET /receiving/done: 단일 date면 단일 오버로드 호출")
    void done_singleDate_callsSingleOverload() throws Exception {
        when(receivingService.getDone(anyString())).thenReturn(List.of(
                new ReceivingNoteSummaryResponse(11L, "IN-OK", "SUP", 1, 10, "COMPLETED_OK", "W1", "2025-11-01T15:00:00Z", "2025-11-03T15:00:00Z", null)
        ));

        mockMvc.perform(get("/api/v1/receiving/done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].noteId", is(11)));

        verify(receivingService, times(1)).getDone(eq("2025-11-02"));
        verify(receivingService, never()).getDone(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("GET /receiving/done: dateFrom만 있으면 date=null로 범위 오버로드 호출(from만 설정)")
    void done_dateFromOnly_callsRangeOverload() throws Exception {
        when(receivingService.getDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/receiving/done")
                        .queryParam("dateFrom", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(receivingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), any());
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertNull(toCaptor.getValue());
    }

    @Test
    @DisplayName("GET /receiving/done: dateTo만 있으면 date=null로 범위 오버로드 호출(to만 설정)")
    void done_dateToOnly_callsRangeOverload() throws Exception {
        when(receivingService.getDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/receiving/done")
                        .queryParam("dateTo", "2025-11-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(receivingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), any());
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertNull(fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-03", toCaptor.getValue());
    }
}
