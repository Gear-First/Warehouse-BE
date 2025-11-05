package com.gearfirst.warehouse.api.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
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

@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerKstNormalizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("GET /shipping/done: range 우선 및 역전 스왑 확인")
    void done_rangeWins_andSwap() throws Exception {
        when(shippingService.getDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shipping/done")
                        .queryParam("date", "2025-11-01")
                        .queryParam("dateFrom", "2025-11-03")
                        .queryParam("dateTo", "2025-11-02")
                        .queryParam("warehouseCode", "서울")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> wcCaptor = ArgumentCaptor.forClass(String.class);
        verify(shippingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), wcCaptor.capture());

        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-03", toCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("서울", wcCaptor.getValue());
    }

    @Test
    @DisplayName("GET /shipping/done: 단일 date만 있을 때 단일 오버로드 호출")
    void done_singleDate_callsSingleOverload() throws Exception {
        when(shippingService.getDone(anyString())).thenReturn(List.of(
                new ShippingNoteSummaryResponse(20L, "OUT-001", "ACME", 1, 10, "COMPLETED", "서울", "2025-11-01T15:00:00Z", "2025-11-03T15:00:00Z",null)
        ));

        mockMvc.perform(get("/api/v1/shipping/done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].noteId", is(20)));

        verify(shippingService, times(1)).getDone(eq("2025-11-02"));
        verify(shippingService, never()).getDone(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("GET /shipping/done: dateFrom만 있을 때 date=null로 범위 오버로드 호출(from만 설정, to=null)")
    void done_dateFromOnly_callsRangeOverload_withNullDate() throws Exception {
        when(shippingService.getDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shipping/done")
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
        verify(shippingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), wcCaptor.capture());
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertNull(toCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertNull(wcCaptor.getValue());
    }

    @Test
    @DisplayName("GET /shipping/done: dateTo만 있을 때 date=null로 범위 오버로드 호출(to만 설정, from=null)")
    void done_dateToOnly_callsRangeOverload_withNullDate() throws Exception {
        when(shippingService.getDone(anyString(), anyString(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shipping/done")
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
        verify(shippingService).getDone(dateCaptor2.capture(), fromCaptor2.capture(), toCaptor2.capture(), wcCaptor2.capture());
        org.junit.jupiter.api.Assertions.assertNull(dateCaptor2.getValue());
        org.junit.jupiter.api.Assertions.assertNull(fromCaptor2.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("2025-11-03", toCaptor2.getValue());
        org.junit.jupiter.api.Assertions.assertNull(wcCaptor2.getValue());
    }
}
