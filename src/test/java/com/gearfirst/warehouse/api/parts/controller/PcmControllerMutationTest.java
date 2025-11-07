package com.gearfirst.warehouse.api.parts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.parts.PcmController;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.CreateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.PartCarModelDetail;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.UpdateMappingRequest;
import com.gearfirst.warehouse.api.parts.persistence.CarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.service.PartCarModelService;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PcmController.class)
class PcmControllerMutationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PartCarModelService pcmService;

    @MockBean
    private CarModelJpaRepository carModelRepo;

    @Test
    @DisplayName("POST /api/v1/parts/{partId}/car-models - 생성 성공 또는 재활성화")
    void createMapping_success() throws Exception {
        var req = new CreateMappingRequest(501L, "note", true);
        var detail = new PartCarModelDetail(1001L, 501L, "note", true, "2025-10-28T00:00:00Z", "2025-10-28T00:00:00Z");
        when(pcmService.createMapping(anyLong(), any(CreateMappingRequest.class))).thenReturn(detail);

        mockMvc.perform(post("/api/v1/parts/{partId}/car-models", 1001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PCM_CREATE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.carModelId", is(501)));
    }

    @Test
    @DisplayName("PATCH /api/v1/parts/{partId}/car-models/{carModelId} - 수정 성공")
    void updateMapping_success() throws Exception {
        var req = new UpdateMappingRequest("updated", true);
        var detail = new PartCarModelDetail(1001L, 501L, "updated", true, "2025-10-28T00:00:00Z", "2025-10-28T00:00:00Z");
        when(pcmService.updateMapping(anyLong(), anyLong(), any(UpdateMappingRequest.class))).thenReturn(detail);

        mockMvc.perform(patch("/api/v1/parts/{partId}/car-models/{carModelId}", 1001L, 501L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PCM_UPDATE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.note", is("updated")));
    }

    @Test
    @DisplayName("DELETE /api/v1/parts/{partId}/car-models/{carModelId} - 삭제 성공(soft)")
    void deleteMapping_success() throws Exception {
        doNothing().when(pcmService).deleteMapping(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/parts/{partId}/car-models/{carModelId}", 1001L, 501L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PCM_DELETE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.deleted", is(true)));
    }
}
