package com.gearfirst.warehouse.api.parts;

import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.CreateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.PartCarModelDetail;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.UpdateMappingRequest;
import com.gearfirst.warehouse.api.parts.service.PartCarModelService;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "PCM PartCarModel", description = "Part–CarModel 매핑 API")
public class PcmController {

    private final PartCarModelService pcmService;

    @Operation(summary = "특정 부품을 사용하는 차량 모델 목록", description = "부품 ID로 차량 모델(CarModel) 목록을 조회합니다. 페이지/사이즈/정렬은 컨트롤러에서 래핑합니다.")
    @GetMapping("/parts/{partId}/car-models")
    public ResponseEntity<ApiResponse<PageEnvelope<CarModelSummary>>> listCarModelsByPart(
            @PathVariable Long partId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        List<CarModelSummary> all = pcmService.listCarModelsByPart(partId, name);

        Comparator<CarModelSummary> comp = Comparator.comparing(CarModelSummary::name, String.CASE_INSENSITIVE_ORDER);
        List<CarModelSummary> sorted = all.stream().sorted(comp).collect(Collectors.toList());

        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        PageEnvelope<CarModelSummary> envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_PCM_CARMODEL_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "차량 모델에 적용 가능한 부품 목록", description = "차량 모델 ID로 부품(Part) 목록을 조회합니다. 페이지/사이즈/정렬은 컨트롤러에서 래핑합니다.")
    @GetMapping("/car-models/{carModelId}/parts")
    public ResponseEntity<ApiResponse<PageEnvelope<PartSummaryResponse>>> listPartsByCarModel(
            @PathVariable Long carModelId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        List<PartSummaryResponse> all = pcmService.listPartsByCarModel(carModelId, code, name, categoryId);

        Comparator<PartSummaryResponse> comp = Comparator
                .comparing(PartSummaryResponse::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(PartSummaryResponse::code, String.CASE_INSENSITIVE_ORDER);
        List<PartSummaryResponse> sorted = all.stream().sorted(comp).collect(Collectors.toList());

        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        PageEnvelope<PartSummaryResponse> envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_PCM_PART_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "부품-차량 모델 매핑 생성", description = "(partId, carModelId) 조합 생성. 비활성 매핑 존재 시 재활성화")
    @PostMapping("/parts/{partId}/car-models")
    public ResponseEntity<ApiResponse<PartCarModelDetail>> createMapping(
            @PathVariable Long partId,
            @RequestBody @Valid CreateMappingRequest req
    ) {
        var detail = pcmService.createMapping(partId, req);
        return ApiResponse.success(SuccessStatus.SEND_PCM_CREATE_SUCCESS, detail);
    }

    @Operation(summary = "부품-차량 모델 매핑 수정", description = "note/enabled 변경")
    @PatchMapping("/parts/{partId}/car-models/{carModelId}")
    public ResponseEntity<ApiResponse<PartCarModelDetail>> updateMapping(
            @PathVariable Long partId,
            @PathVariable Long carModelId,
            @RequestBody @Valid UpdateMappingRequest req
    ) {
        var detail = pcmService.updateMapping(partId, carModelId, req);
        return ApiResponse.success(SuccessStatus.SEND_PCM_UPDATE_SUCCESS, detail);
    }

    @Operation(summary = "부품-차량 모델 매핑 삭제(soft)", description = "enabled=false 처리")
    @DeleteMapping("/parts/{partId}/car-models/{carModelId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> deleteMapping(
            @PathVariable Long partId,
            @PathVariable Long carModelId
    ) {
        pcmService.deleteMapping(partId, carModelId);
        return ApiResponse.success(SuccessStatus.SEND_PCM_DELETE_SUCCESS, java.util.Map.of("deleted", true));
    }
}
