package com.gearfirst.warehouse.api.parts.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearfirst.warehouse.api.parts.PartsController;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategorySummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CreateCategoryRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.CategoryRef;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.CreatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.service.PartCategoryService;
import com.gearfirst.warehouse.api.parts.service.PartQueryService;
import com.gearfirst.warehouse.api.parts.service.PartService;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.GlobalExceptionHandler;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PartsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PartCategoryService categoryService;

    @Mock
    private PartService partService;

    @Mock
    private PartQueryService partQueryService;

    @BeforeEach
    void setup() {
        PartsController controller = new PartsController(categoryService, partService, partQueryService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/parts/categories - 목록 성공")
    void listCategories_success() throws Exception {
        when(categoryService.list(ArgumentMatchers.anyString())).thenReturn(List.of(new CategorySummaryResponse(10L, "Filter", "Oil/Air")));

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
        when(categoryService.create(ArgumentMatchers.any(CreateCategoryRequest.class)))
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
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.isNull()
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
        var req = new CreatePartRequest("P-1001", "오일필터", 12000, 10L, "/img/p-1001.png", 0);
        var detail = new PartDetailResponse(1L, "P-1001", "오일필터", 12000,
                new CategoryRef(10L, "Filter"), "/img/p-1001.png", true, "2025-10-27T00:00:00Z", "2025-10-27T00:00:00Z", 0);
        when(partService.create(ArgumentMatchers.any(CreatePartRequest.class))).thenReturn(detail);

        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PART_CREATE_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.code", is("P-1001")));
    }

    @Test
    @DisplayName("GET /api/v1/parts/integrated - 통합 검색 파라미터 바인딩 및 응답 래핑")
    void integrated_search_success() throws Exception {
        // given
        var item = com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem.builder()
                .id(1L).code("P-1001").name("Engine Oil Filter").price(12000)
                .imageUrl("/img/p-1001.png").safetyStockQty(0).enabled(true)
                .categoryId(10L).categoryName("Filter")
                .carModels(java.util.List.of(
                        new com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary(1L, "Avante"),
                        new com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary(2L, "Sonata")
                ))
                .build();
        var envelope = com.gearfirst.warehouse.common.response.PageEnvelope.of(java.util.List.of(item), 0, 20, 1);
        when(partQueryService.searchIntegrated(
                ArgumentMatchers.any(com.gearfirst.warehouse.api.parts.dto.PartSearchCond.class),
                ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(envelope);

        // when/then
        mockMvc.perform(get("/api/v1/parts/integrated")
                        .param("q", "oil")
                        .param("categoryName", "Filter")
                        .param("carModelName", "Avante")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "name,asc")
                        .param("sort", "updatedAt,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is(SuccessStatus.SEND_PART_LIST_SUCCESS.getStatusCode())))
                .andExpect(jsonPath("$.data.items[0].code", is("P-1001")))
                .andExpect(jsonPath("$.data.items[0].categoryName", is("Filter")))
                .andExpect(jsonPath("$.data.items[0].carModels", hasSize(2)));
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
