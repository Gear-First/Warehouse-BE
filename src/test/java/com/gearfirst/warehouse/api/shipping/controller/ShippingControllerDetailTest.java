package com.gearfirst.warehouse.api.shipping.controller;

import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.*;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerDetailTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("GET /api/v1/shipping/{noteId} - 상세 조회 성공")
    void getDetail_success() throws Exception {
        // given
        var lines = List.of(
                new ShippingNoteLineResponse(1L,
                        new ShippingProductResponse(11L, "LOT-A", "S-01", "볼트", "/img/b"),
                        10, 10, 8, "READY")
        );
        var detail = new ShippingNoteDetailResponse(3001L, "ACME", 1, 10, "IN_PROGRESS", null,
                        null, null, null, null, null, null, null, null, null,
                        lines);
        when(shippingService.getDetail(anyLong())).thenReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/shipping/{id}", 3001L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(SuccessStatus.SEND_SHIPPING_NOTE_DETAIL_SUCCESS.getStatusCode()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_SHIPPING_NOTE_DETAIL_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.noteId", is(3001)))
                .andExpect(jsonPath("$.data.lines", hasSize(1)))
                .andExpect(jsonPath("$.data.lines[0].status", is("READY")));
    }
}
