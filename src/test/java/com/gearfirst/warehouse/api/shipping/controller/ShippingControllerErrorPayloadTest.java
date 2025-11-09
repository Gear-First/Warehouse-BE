package com.gearfirst.warehouse.api.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.shipping.ShippingController;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShippingController.class)
class ShippingControllerErrorPayloadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShippingService shippingService;

    @Test
    @DisplayName("PATCH /api/v1/shipping/{noteId}/lines/{lineId} - 409 시 CommonApiResponse 에러 페이로드 형태")
    void updateLine_conflict_errorPayload() throws Exception {
        when(shippingService.updateLine(eq(7001L), eq(1L), any(ShippingUpdateLineRequest.class)))
                .thenThrow(new ConflictException(ErrorStatus.CONFLICT_NOTE_STATUS_WHILE_COMPLETE));

        var body = new ShippingUpdateLineRequest(10);
        mockMvc.perform(patch("/api/v1/shipping/{noteId}/lines/{lineId}", 7001L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is(ErrorStatus.CONFLICT_NOTE_STATUS_WHILE_COMPLETE.getMessage())));
    }
}
