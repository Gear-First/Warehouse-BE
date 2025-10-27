package com.gearfirst.warehouse.api.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerNegativeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("GET /api/v1/shipping/{noteId} - 존재하지 않으면 404 반환")
    void getDetail_notFound() throws Exception {
        // given
        when(shippingService.getDetail(anyLong())).thenThrow(new NotFoundException("Shipping note not found: 9999"));

        // when & then
        mockMvc.perform(get("/api/v1/shipping/{id}", 9999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipping/{noteId}/lines/{lineId} - 음수나 누락 등 Bean Validation 실패 시 400 반환")
    void updateLine_validationBeanError() throws Exception {
        // given: allocatedQty 누락, pickedQty 음수, status 누락
        var invalidJson = "{\n" +
                "  \"pickedQty\": -1\n" +
                "}";

        // when & then
        mockMvc.perform(patch("/api/v1/shipping/{noteId}/lines/{lineId}", 100L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipping/{noteId}/lines/{lineId} - 필수 필드 누락 시 400 반환")
    void updateLine_requiredFieldMissing() throws Exception {
        // given: pickedQty 누락
        var invalidJson = "{\n  \"allocatedQty\": 5\n}";

        // when & then
        mockMvc.perform(patch("/api/v1/shipping/{noteId}/lines/{lineId}", 100L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)));
    }
}
