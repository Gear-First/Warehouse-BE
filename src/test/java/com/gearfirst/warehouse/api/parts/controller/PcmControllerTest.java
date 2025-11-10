package com.gearfirst.warehouse.api.parts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.parts.PcmController;
import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.CategoryRef;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.persistence.CarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartCarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.service.PartCarModelService;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PcmController.class)
class PcmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PartCarModelService pcmService;

    @MockBean
    private CarModelJpaRepository carModelRepo;

    @MockBean
    private PartCarModelJpaRepository pcmRepo;


    @Test
    @DisplayName("GET /api/v1/parts/{partId}/car-models - 목록 성공(PageEnvelope)")
    void listCarModelsByPart_success() throws Exception {
        when(pcmService.listCarModelsByPart(anyLong(), any())).thenReturn(List.of(
                new CarModelSummary(501L, "Avante")
        ));

        mockMvc.perform(get("/api/v1/parts/{partId}/car-models", 1001L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PCM_CARMODEL_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items[0].name", is("Avante")))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(1)));
    }

    @Test
    @DisplayName("GET /api/v1/car-models/{carModelId}/parts - 목록 성공(PageEnvelope)")
    void listPartsByCarModel_success() throws Exception {
        when(pcmService.listPartsByCarModel(anyLong(), any(), any(), any())).thenReturn(List.of(
                new PartSummaryResponse(1001L, "P-1001", "오일필터", new CategoryRef(10L, "Filter"))
        ));

        mockMvc.perform(get("/api/v1/car-models/{carModelId}/parts", 501L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PCM_PART_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items[0].code", is("P-1001")))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(1)));
    }
}
