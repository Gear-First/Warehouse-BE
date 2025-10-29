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
class ShippingControllerNotDoneTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("GET /api/v1/shipping/not-done - 성공적으로 not-done 리스트를 반환한다")
    void getNotDone_success() throws Exception {
        // given
        var list = List.of(
                new ShippingNoteSummaryResponse(1001L, "ACME", 2, 30, "PENDING", null),
                new ShippingNoteSummaryResponse(1002L, "BETA", 1, 10, "IN_PROGRESS", null)
        );
        when(shippingService.getNotDone(any())).thenReturn(list);

        // when & then
        mockMvc.perform(get("/api/v1/shipping/not-done")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.message", is(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS.getMessage())))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].status", anyOf(is("PENDING"), is("IN_PROGRESS"))))
                .andExpect(jsonPath("$.data.items[1].status", anyOf(is("PENDING"), is("IN_PROGRESS"))))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(2)));
    }
}
