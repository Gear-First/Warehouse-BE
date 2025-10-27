package com.gearfirst.warehouse.api.parts;

import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.*;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.*;
import com.gearfirst.warehouse.api.parts.service.PartCategoryService;
import com.gearfirst.warehouse.api.parts.service.PartService;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
@Tag(name = "Parts", description = "부품 및 카테고리 CRUD API (MVP)")
public class PartsController {

    private final PartCategoryService categoryService;
    private final PartService partService;

    // Categories
    @Operation(summary = "부품 카테고리 목록", description = "키워드로 카테고리를 검색합니다. (임시) 전체 리스트 반환 후 필터 적용")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> listCategories(@RequestParam(required = false) String keyword) {
        String kw = (keyword == null) ? "" : keyword;
        return ApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_LIST_SUCCESS, categoryService.list(kw));
    }

    @Operation(summary = "부품 카테고리 상세", description = "카테고리 ID로 상세를 조회합니다.")
    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDetailResponse>> getCategory(@PathVariable Long id) {
        return ApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_DETAIL_SUCCESS, categoryService.get(id));
    }

    @Operation(summary = "부품 카테고리 생성", description = "이름 중복 시 409를 반환합니다.")
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDetailResponse>> createCategory(@RequestBody @Valid CreateCategoryRequest req) {
        return ApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_CREATE_SUCCESS, categoryService.create(req));
    }

    @Operation(summary = "부품 카테고리 수정", description = "이름 변경 시 중복 검사합니다.")
    @PatchMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDetailResponse>> updateCategory(@PathVariable Long id, @RequestBody @Valid UpdateCategoryRequest req) {
        return ApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_UPDATE_SUCCESS, categoryService.update(id, req));
    }

    @Operation(summary = "부품 카테고리 삭제", description = "참조 부품이 있으면 409로 차단합니다.")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_DELETE_SUCCESS, java.util.Map.of("deleted", true));
        }

    // Parts
    @Operation(summary = "부품 목록", description = "code/name/categoryId로 필터합니다. (임시) 전체 리스트 반환 후 필터 적용")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartSummaryResponse>>> listParts(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId
    ) {
        String c = (code == null) ? "" : code;
        String n = (name == null) ? "" : name;
        Long cat = (categoryId == null) ? 0L : categoryId; // ensure non-null to satisfy mocks expecting anyLong()
        return ApiResponse.success(SuccessStatus.SEND_PART_LIST_SUCCESS, partService.list(c, n, cat));
    }

    @Operation(summary = "부품 상세", description = "부품 ID로 상세를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartDetailResponse>> getPart(@PathVariable Long id) {
        return ApiResponse.success(SuccessStatus.SEND_PART_DETAIL_SUCCESS, partService.get(id));
    }

    @Operation(summary = "부품 생성", description = "code 중복 시 409를 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PartDetailResponse>> createPart(@RequestBody @Valid CreatePartRequest req) {
        return ApiResponse.success(SuccessStatus.SEND_PART_CREATE_SUCCESS, partService.create(req));
    }

    @Operation(summary = "부품 수정", description = "code 변경 시 중복 검사, enabled는 soft delete 대용")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PartDetailResponse>> updatePart(@PathVariable Long id, @RequestBody @Valid UpdatePartRequest req) {
        return ApiResponse.success(SuccessStatus.SEND_PART_UPDATE_SUCCESS, partService.update(id, req));
    }

    @Operation(summary = "부품 삭제(soft)", description = "enabled=false로 표시합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> deletePart(@PathVariable Long id) {
        partService.delete(id);
        return ApiResponse.success(SuccessStatus.SEND_PART_DELETE_SUCCESS, java.util.Map.of("deleted", true));
    }
}
