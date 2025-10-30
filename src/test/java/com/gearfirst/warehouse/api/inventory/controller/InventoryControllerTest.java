package com.gearfirst.warehouse.api.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.inventory.InventoryController;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.PartRef;
import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("GET /api/v1/inventory/onhand - 목록 성공(ApiResponse<PageEnvelope>)")
    void listOnHand_success() throws Exception {
        var items = List.of(new OnHandSummary("1", new PartRef(1001L, "P-1001", "오일필터"), 128, "2025-10-27T00:00:00Z"));
        var envelope = PageEnvelope.of(items, 0, 20, 1);
        when(inventoryService.listOnHand(any(), any(), any(), any(), any(), anyInt(), anyInt(), any())).thenReturn(envelope);

        mockMvc.perform(get("/api/v1/inventory/onhand")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_INVENTORY_ONHAND_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.total", is(1)));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/onhand - 잘못된 page/size면 400")
    void listOnHand_badRequest() throws Exception {
        when(inventoryService.listOnHand(any(), any(), any(), any(), any(), eq(-1), anyInt(), any()))
                .thenThrow(new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION));

        mockMvc.perform(get("/api/v1/inventory/onhand")
                        .param("page", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)));
    }
}
