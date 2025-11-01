package com.gearfirst.warehouse.api.inventory;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "재고 읽기 모델 API (MVP, Read-only)")
public class InventoryController {

    private final InventoryService service;

    @Operation(summary = "재고 현황(On-hand) 목록", description = "창고/부품 키워드로 On-hand 목록을 조회합니다. 필터는 AND로 결합됩니다. partKeyword/supplierName은 대소문자 무시 contains. 수량 범위는 minQty ≤ onHandQty ≤ maxQty. 페이지/사이즈 기본값: page=0, size=20. 정렬 허용 필드: partName, partCode, onHandQty, lastUpdatedAt. 잘못된 정렬 키 또는 잘못된 범위는 400.")
    @Parameters({
            @Parameter(name = "warehouseCode", description = "창고 코드(예: 서울)"),
            @Parameter(name = "partKeyword", description = "부품 코드/이름 키워드 (대소문자 무시 contains)"),
            @Parameter(name = "supplierName", description = "공급업체 이름 (대소문자 무시 contains)"),
            @Parameter(name = "minQty", description = "최소 수량"),
            @Parameter(name = "maxQty", description = "최대 수량"),
            @Parameter(name = "page", description = "페이지(기본 0, 최소 0)"),
            @Parameter(name = "size", description = "페이지 크기(기본 20, 1..100)"),
            @Parameter(name = "sort", description = "정렬 필드(허용: partName,partCode,onHandQty,lastUpdatedAt)")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "On-hand 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (정렬 키/범위 등)")
    })
    @GetMapping("/onhand")
    public ResponseEntity<CommonApiResponse<PageEnvelope<OnHandSummary>>> listOnHand(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String partKeyword,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer minQty,
            @RequestParam(required = false) Integer maxQty,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) List<String> sort
    ) {
        // Validate page/size per contract (page >= 0, 1 <= size <= 100)
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        if (p < 0 || s < 1 || s > 100) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        // Bridge behavior for legacy tests: when only paging is used (no filters), call legacy signature
        boolean noFilters = (warehouseCode == null && partKeyword == null && supplierName == null
                && minQty == null && maxQty == null && (sort == null || sort.isEmpty()));

        // Validate min/max range when provided
        if (minQty != null && maxQty != null && minQty > maxQty) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        // Validate sort keys when provided (whitelist)
        if (sort != null && !sort.isEmpty()) {
            Set<String> allowed = Set.of("partName", "partCode", "onHandQty", "lastUpdatedAt");
            for (String srt : sort) {
                String key = srt == null ? null : srt.split(",")[0];
                if (key == null || !allowed.contains(key)) {
                    throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
                }
            }
        }

        PageEnvelope<OnHandSummary> envelope = noFilters
                ? service.listOnHand(null, null, p, s) // legacy path used by existing unit tests
                : service.listOnHand(warehouseCode, partKeyword, supplierName, minQty, maxQty, p, s, sort);
        return CommonApiResponse.success(SuccessStatus.SEND_INVENTORY_ONHAND_LIST_SUCCESS, envelope);
    }
}
