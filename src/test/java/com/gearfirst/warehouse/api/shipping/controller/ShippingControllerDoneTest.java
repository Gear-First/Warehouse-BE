package com.gearfirst.warehouse.api.shipping.controller;

import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerDoneTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("GET /api/v1/shipping/done - 완료/지연 리스트를 반환한다")
    void getDone_success() throws Exception {
        // given
        var list = List.of(
                new ShippingNoteSummaryResponse(2001L, "ACME", 3, 50, "COMPLETED", "2025-10-20T10:00:00Z"),
                new ShippingNoteSummaryResponse(2002L, "BETA", 1, 10, "DELAYED", "2025-10-21T11:00:00Z")
        );
        when(shippingService.getDone(any())).thenReturn(list);

        // when & then
        mockMvc.perform(get("/api/v1/shipping/done")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.message", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[*].status", everyItem(is(in(List.of("COMPLETED", "DELAYED"))))))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(2)));
    }
}
