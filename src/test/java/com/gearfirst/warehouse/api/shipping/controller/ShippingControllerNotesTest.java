package com.gearfirst.warehouse.api.shipping.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
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
class ShippingControllerNotesTest {

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
    @DisplayName("GET /api/v1/shipping/notes?status=not-done - 통합 엔드포인트: 예정 목록")
    void getNotes_notDone_success() throws Exception {
        var list = List.of(
                new ShippingNoteSummaryResponse(2001L, "S-001", "ACME", 3, 50, "PENDING", "WH1", "2025-10-20T09:00:00Z", "2025-10-22T09:00:00Z",null),
                new ShippingNoteSummaryResponse(2002L, "S-002", "BETA", 1, 10, "IN_PROGRESS", "WH1", "2025-10-20T09:05:00Z", "2025-10-22T09:05:00Z",null)
        );
        when(shippingService.getNotDone(any())).thenReturn(list);

        mockMvc.perform(get("/api/v1/shipping/notes").param("status", "not-done").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[*].warehouseCode", everyItem(notNullValue())))
                .andExpect(jsonPath("$.data.items[*].requestedAt", everyItem(notNullValue())))
                .andExpect(jsonPath("$.data.items[*].completedAt", everyItem(anyOf(nullValue(), isA(String.class)))))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(2)));
    }

    @Test
    @DisplayName("GET /api/v1/shipping/notes?status=done - 통합 엔드포인트: 완료/지연 목록")
    void getNotes_done_success() throws Exception {
        var list = List.of(
                new ShippingNoteSummaryResponse(2003L, "S-003", "ACME", 3, 50, "COMPLETED", "WH1", "2025-10-20T08:00:00Z", "2025-10-22T08:00:00Z", "2025-10-20T10:00:00Z"),
                new ShippingNoteSummaryResponse(2004L, "S-004", "BETA", 1, 10, "DELAYED", "WH1", "2025-10-20T09:00:00Z", "2025-10-22T09:00:00Z", "2025-10-20T15:00:00Z")
        );
        when(shippingService.getDone(any())).thenReturn(list);

        mockMvc.perform(get("/api/v1/shipping/notes").param("status", "done").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[*].status", everyItem(is(in(List.of("COMPLETED","DELAYED"))))))
                .andExpect(jsonPath("$.data.items[*].completedAt", everyItem(notNullValue())))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(2)));
    }

    @Test
    @DisplayName("GET /api/v1/shipping/notes?status=all - 통합 엔드포인트: 전체 목록(머지)")
    void getNotes_all_success() throws Exception {
        when(shippingService.getNotDone(any())).thenReturn(List.of(
                new ShippingNoteSummaryResponse(2005L, "S-005", "ACME", 3, 50, "PENDING", "WH1", "2025-10-20T09:00:00Z", "2025-10-22T09:00:00Z", null)
        ));
        when(shippingService.getDone(any())).thenReturn(List.of(
                new ShippingNoteSummaryResponse(2006L, "S-006", "BETA", 1, 10, "COMPLETED", "WH1", "2025-10-20T08:00:00Z","2025-10-22T08:00:00Z", "2025-10-20T10:00:00Z")
        ));

        mockMvc.perform(get("/api/v1/shipping/notes").param("status", "all").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].warehouseCode", notNullValue()))
                .andExpect(jsonPath("$.data.items[0].requestedAt", notNullValue()))
                .andExpect(jsonPath("$.data.items[1].completedAt", notNullValue()));
    }
}
