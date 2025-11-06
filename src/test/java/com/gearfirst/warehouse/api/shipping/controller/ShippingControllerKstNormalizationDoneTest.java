package com.gearfirst.warehouse.api.shipping.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.exception.GlobalExceptionHandler;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ShippingControllerKstNormalizationDoneTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ShippingService shippingService;

    @BeforeEach
    void setup() {
        ShippingController controller = new ShippingController(shippingService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /shipping/done: range 우선 및 역전 스왑")
    void done_rangeWins_andSwap() throws Exception {
        when(shippingService.getDone(
                ArgumentMatchers.<String>nullable(String.class),
                ArgumentMatchers.<String>nullable(String.class),
                ArgumentMatchers.<String>nullable(String.class),
                any()
        )).thenReturn(List.of());

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

        Assertions.assertNull(dateCaptor.getValue());
        Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        Assertions.assertEquals("2025-11-03", toCaptor.getValue());
        Assertions.assertEquals("서울", wcCaptor.getValue());
    }

    @Test
    @DisplayName("GET /shipping/done: 단일 date면 단일 오버로드 호출")
    void done_singleDate_callsSingleOverload() throws Exception {
        when(shippingService.getDone(eq("2025-11-02"))).thenReturn(List.of(
                new ShippingNoteSummaryResponse(11L, "OUT-OK", "ACME", 1, 10, "COMPLETED", "W1", "2025-11-01T15:00:00Z", "2025-11-03T15:00:00Z", null)
        ));

        mockMvc.perform(get("/api/v1/shipping/done")
                        .queryParam("date", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].noteId", is(11)));

        verify(shippingService, times(1)).getDone(eq("2025-11-02"));
        verify(shippingService, never()).getDone(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("GET /shipping/done: dateFrom만 있으면 date=null로 범위 오버로드 호출(from만 설정)")
    void done_dateFromOnly_callsRangeOverload() throws Exception {
        when(shippingService.getDone(
                ArgumentMatchers.<String>nullable(String.class),
                ArgumentMatchers.<String>nullable(String.class),
                ArgumentMatchers.<String>nullable(String.class),
                any()
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shipping/done")
                        .queryParam("dateFrom", "2025-11-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(shippingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), any());
        Assertions.assertNull(dateCaptor.getValue());
        Assertions.assertEquals("2025-11-02", fromCaptor.getValue());
        Assertions.assertNull(toCaptor.getValue());
    }

    @Test
    @DisplayName("GET /shipping/done: dateTo만 있으면 date=null로 범위 오버로드 호출(to만 설정)")
    void done_dateToOnly_callsRangeOverload() throws Exception {
        when(shippingService.getDone(
                ArgumentMatchers.<String>nullable(String.class),
                ArgumentMatchers.<String>nullable(String.class),
                ArgumentMatchers.<String>nullable(String.class),
                any()
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shipping/done")
                        .queryParam("dateTo", "2025-11-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));

        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(shippingService).getDone(dateCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), any());
        Assertions.assertNull(dateCaptor.getValue());
        Assertions.assertNull(fromCaptor.getValue());
        Assertions.assertEquals("2025-11-03", toCaptor.getValue());
    }
}
