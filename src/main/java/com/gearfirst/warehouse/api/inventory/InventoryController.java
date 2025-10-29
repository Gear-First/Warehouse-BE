package com.gearfirst.warehouse.api.inventory;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "재고 현황(On-hand) 목록", description = "창고/부품 키워드로 On-hand 목록을 조회합니다. 필터: warehouseId, keyword (부분 일치). 페이지/사이즈 기본값: page=0, size=20. 주의: Inventory의 Create/Update/Delete 엔드포인트는 일반 운영에서 사용하지 않습니다.")
    @GetMapping("/onhand")
    public ResponseEntity<ApiResponse<PageEnvelope<OnHandSummary>>> listOnHand(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        var envelope = service.listOnHand(warehouseId, keyword, page, size);
        return ApiResponse.success(SuccessStatus.SEND_INVENTORY_ONHAND_LIST_SUCCESS, envelope);
    }
}
