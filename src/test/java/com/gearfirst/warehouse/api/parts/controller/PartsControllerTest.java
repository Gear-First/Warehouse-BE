package com.gearfirst.warehouse.api.parts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.parts.PartsController;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.*;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.*;
import com.gearfirst.warehouse.api.parts.service.PartCategoryService;
import com.gearfirst.warehouse.api.parts.service.PartService;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
// Mockito matchers (use FQN calls below to avoid clash with Hamcrest Matchers.any)
// import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PartsController.class)
class PartsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PartCategoryService categoryService;

    @MockBean
    private PartService partService;

    @Test
    @DisplayName("GET /api/v1/parts/categories - 목록 성공")
    void listCategories_success() throws Exception {
        when(categoryService.list(org.mockito.ArgumentMatchers.anyString())).thenReturn(List.of(new CategorySummaryResponse(10L, "Filter", "Oil/Air")));

        mockMvc.perform(get("/api/v1/parts/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PART_CATEGORY_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data[0].name", is("Filter")));
    }

    @Test
    @DisplayName("POST /api/v1/parts/categories - 중복 이름이면 409")
    void createCategory_conflict() throws Exception {
        var req = new CreateCategoryRequest("Filter", "Oil/Air");
        when(categoryService.create(org.mockito.ArgumentMatchers.any(CreateCategoryRequest.class)))
                .thenThrow(new ConflictException(ErrorStatus.PART_CATEGORY_NAME_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/parts/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("GET /api/v1/parts - 목록 성공(PageEnvelope)")
    void listParts_success() throws Exception {
        var envelope = PageEnvelope.of(
                List.of(new PartSummaryResponse(1001L, "P-1001", "오일필터", new CategoryRef(10L, "Filter"))),
                0, 20, 1
        );
        when(partService.list(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(envelope);

        mockMvc.perform(get("/api/v1/parts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PART_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items[0].code", is("P-1001")))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.total", is(1)));
    }

    @Test
    @DisplayName("POST /api/v1/parts - 생성 성공")
    void createPart_success() throws Exception {
        var req = new CreatePartRequest("P-1001", "오일필터", 12000, 10L, "/img/p-1001.png");
        var detail = new PartDetailResponse(1L, "P-1001", "오일필터", 12000,
                new CategoryRef(10L, "Filter"), "/img/p-1001.png", true, "2025-10-27T00:00:00Z", "2025-10-27T00:00:00Z");
        when(partService.create(org.mockito.ArgumentMatchers.any(CreatePartRequest.class))).thenReturn(detail);

        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PART_CREATE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.code", is("P-1001")));
    }

    @Test
    @DisplayName("DELETE /api/v1/parts/{id} - 삭제 성공(soft)")
    void deletePart_success() throws Exception {
        mockMvc.perform(delete("/api/v1/parts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PART_DELETE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.deleted", is(true)));
    }
}
