package com.gearfirst.warehouse.api.parts;

import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategoryDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategorySummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CreateCategoryRequest;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.UpdateCategoryRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.CreatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.UpdatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem;
import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import com.gearfirst.warehouse.api.parts.service.PartCategoryService;
import com.gearfirst.warehouse.api.parts.service.PartQueryService;
import com.gearfirst.warehouse.api.parts.service.PartService;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
@Tag(name = "Parts", description = "부품 및 카테고리 CRUD API (MVP)")
public class PartsController {

    private final PartCategoryService categoryService;
    private final PartService partService;
    private final PartQueryService partQueryService;

    // Categories
    @Operation(summary = "부품 카테고리 목록", description = "키워드로 카테고리를 검색합니다. (임시) 전체 리스트 반환 후 필터 적용")
    @GetMapping("/categories")
    public ResponseEntity<CommonApiResponse<List<CategorySummaryResponse>>> listCategories(
            @RequestParam(required = false) String keyword) {
        String kw = (keyword == null) ? "" : keyword;
        return CommonApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_LIST_SUCCESS, categoryService.list(kw));
    }

    @Operation(summary = "부품 카테고리 상세", description = "카테고리 ID로 상세를 조회합니다.")
    @GetMapping("/categories/{id}")
    public ResponseEntity<CommonApiResponse<CategoryDetailResponse>> getCategory(@PathVariable Long id) {
        return CommonApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_DETAIL_SUCCESS, categoryService.get(id));
    }

    @Operation(summary = "부품 카테고리 생성", description = "이름 중복 시 409를 반환합니다.")
    @PostMapping("/categories")
    public ResponseEntity<CommonApiResponse<CategoryDetailResponse>> createCategory(
            @RequestBody @Valid CreateCategoryRequest req) {
        return CommonApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_CREATE_SUCCESS, categoryService.create(req));
    }

    @Operation(summary = "부품 카테고리 수정", description = "이름 변경 시 중복 검사합니다.")
    @PatchMapping("/categories/{id}")
    public ResponseEntity<CommonApiResponse<CategoryDetailResponse>> updateCategory(@PathVariable Long id,
                                                                                    @RequestBody @Valid UpdateCategoryRequest req) {
        return CommonApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_UPDATE_SUCCESS, categoryService.update(id, req));
    }

    @Operation(summary = "부품 카테고리 삭제(soft)", description = "참조 부품이 있으면 409로 차단합니다.")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<CommonApiResponse<Map<String, Boolean>>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return CommonApiResponse.success(SuccessStatus.SEND_PART_CATEGORY_DELETE_SUCCESS, java.util.Map.of("deleted", true));
    }

    // Parts
    @Operation(summary = "부품 목록", description = "code/name/categoryId로 필터하고 서버사이드 페이지네이션/정렬로 조회합니다. 기본 정렬: name,asc → code,asc")
    @GetMapping
    public ResponseEntity<CommonApiResponse<PageEnvelope<PartSummaryResponse>>> listParts(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        PageEnvelope<PartSummaryResponse> envelope = partService.list(code, name, categoryId, p, s, sort);
        return CommonApiResponse.success(SuccessStatus.SEND_PART_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "부품 통합 조회", description = "Querydsl 기반 통합 검색: q(code | name | category.name | carModel.name), categoryId|categoryName, carModelId|carModelName, enabled. 정렬 화이트리스트(code,name,price,createdAt,updatedAt)")
    @GetMapping("/integrated")
    public ResponseEntity<CommonApiResponse<PageEnvelope<PartIntegratedItem>>> searchIntegrated(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long partId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Long carModelId,
            @RequestParam(required = false) String carModelName,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(p, s, parseSort(sort));
        var cond = PartSearchCond.builder()
                .q(q)
                .partId(partId)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .carModelId(carModelId)
                .carModelName(carModelName)
                .enabled(enabled)
                .build();
        var envelope = partQueryService.searchIntegrated(cond, pageable);
        return CommonApiResponse.success(SuccessStatus.SEND_PART_LIST_SUCCESS, envelope);
    }

    private Sort parseSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }
        try {
            List<Sort.Order> orders = sortParams.stream()
                    .map(s -> {
                        String[] arr = s.split(",");
                        String prop = arr[0].trim();
                        String dir = arr.length > 1 ? arr[1].trim().toLowerCase() : "asc";
                        Sort.Order o = "desc".equals(dir)
                                ? Sort.Order.desc(prop)
                                : Sort.Order.asc(prop);
                        return o.ignoreCase();
                    })
                    .toList();
            return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        } catch (Exception e) {
            return Sort.unsorted();
        }
    }

    @Operation(summary = "부품 상세", description = "부품 ID로 상세를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<PartDetailResponse>> getPart(@PathVariable Long id) {
        return CommonApiResponse.success(SuccessStatus.SEND_PART_DETAIL_SUCCESS, partService.get(id));
    }

    @Operation(summary = "부품 생성", description = "code 중복 시 409를 반환합니다.")
    @PostMapping
    public ResponseEntity<CommonApiResponse<PartDetailResponse>> createPart(@RequestBody @Valid CreatePartRequest req) {
        return CommonApiResponse.success(SuccessStatus.SEND_PART_CREATE_SUCCESS, partService.create(req));
    }

    @Operation(summary = "부품 수정", description = "code 변경 시 중복 검사, enabled는 soft delete 대용")
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<PartDetailResponse>> updatePart(@PathVariable Long id,
                                                                            @RequestBody @Valid UpdatePartRequest req) {
        return CommonApiResponse.success(SuccessStatus.SEND_PART_UPDATE_SUCCESS, partService.update(id, req));
    }

    @Operation(summary = "부품 삭제(soft)", description = "enabled=false로 표시합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Map<String, Boolean>>> deletePart(@PathVariable Long id) {
        partService.delete(id);
        return CommonApiResponse.success(SuccessStatus.SEND_PART_DELETE_SUCCESS, java.util.Map.of("deleted", true));
    }
}
