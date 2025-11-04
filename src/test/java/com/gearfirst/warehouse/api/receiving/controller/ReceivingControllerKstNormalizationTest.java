package com.gearfirst.warehouse.api.receiving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.receiving.ReceivingController;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReceivingController.class)
class ReceivingControllerKstNormalizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceivingService receivingService;

    @Test
    @DisplayName("GET /receiving/not-done: range가 단일보다 우선하고, 역전(from>to) 시 스왑한다")
    void notDone_rangeWins_andSwapReversedRange() throws Exception {
        // Arrange: service returns empty list for simplicity
        when(receivingService.getNotDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        // Act
        mockMvc.perform(get("/api/v1/receiving/not-done")
                        .queryParam("date", "2025-11-01") // should be ignored because range is provided
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

        // Assert: service called with swapped normalized range (from=2025-11-02, to=2025-11-03)
        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> wcCaptor = ArgumentCaptor.forClass(String.class);
        verify(receivingService).getNotDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), wcCaptor.capture());

        // date is ignored when range present
        // normalized range swapped: from <= to
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-03", toCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("서울", wcCaptor.getValue());
    }

    @Test
    @DisplayName("GET /receiving/not-done: 단일 date만 있을 때 단일 오버로드 호출")
    void notDone_singleDate_callsSingleOverload() throws Exception {
        // Arrange
        when(receivingService.getNotDone(anyString())).thenReturn(List.of(
                new ReceivingNoteSummaryResponse(10L, "IN-001", "ACME", 1, 10, "PENDING", "서울", "2025-11-01T15:00:00Z", null)
        ));

        // Act
        mockMvc.perform(get("/api/v1/receiving/not-done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].noteId", is(10)));

        // Assert: ensure range overload was not used; single date overload was used
        verify(receivingService, times(1)).getNotDone(eq("2025-11-02"));
        verify(receivingService, never()).getNotDone(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("GET /receiving/not-done: dateFrom만 있을 때 date=null로 범위 오버로드 호출(from만 설정, to=null)")
    void notDone_dateFromOnly_callsRangeOverload_withNullDate() throws Exception {
        when(receivingService.getNotDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/receiving/not-done")
                        .queryParam("dateFrom", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> wcCaptor = ArgumentCaptor.forClass(String.class);
        verify(receivingService).getNotDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), wcCaptor.capture());
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertNull(toCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertNull(wcCaptor.getValue());
    }

    @Test
    @DisplayName("GET /receiving/not-done: dateTo만 있을 때 date=null로 범위 오버로드 호출(to만 설정, from=null)")
    void notDone_dateToOnly_callsRangeOverload_withNullDate() throws Exception {
        when(receivingService.getNotDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/receiving/not-done")
                        .queryParam("dateTo", "2025-11-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(0)));

        ArgumentCaptor<String> dateCaptor2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> wcCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(receivingService).getNotDone(dateCaptor2.capture(), fromCaptor2.capture(), toCaptor2.capture(), wcCaptor2.capture());
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor2.getValue());
        org.junit.jupiter.api.Assertions.assertNull(fromCaptor2.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-03", toCaptor2.getValue());
        org.junit.jupiter.api.Assertions.assertNull(wcCaptor2.getValue());
    }
}
