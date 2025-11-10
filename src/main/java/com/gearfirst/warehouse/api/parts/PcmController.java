package com.gearfirst.warehouse.api.parts;

import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary;
import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelListItem;
import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CreateCarModelRequest;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.CreateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.PartCarModelDetail;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.UpdateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.persistence.CarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartCarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.entity.CarModelEntity;
import com.gearfirst.warehouse.api.parts.service.PartCarModelService;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "PCM PartCarModel", description = "Part–CarModel 매핑 API")
public class PcmController {

    private final PartCarModelService pcmService;
    private final CarModelJpaRepository carModelRepo;
    private final PartCarModelJpaRepository pcmRepo;

    // TODO: Move listing logic into CarModelQueryService
    //  Controller → Service → Repository
    //  Controller : only handle binding/sort parsing
    @Operation(
            summary = "차량 모델 목록",
            description = "차량 모델(CarModel) 목록을 조회합니다.\n"
                    + "- q: name 부분 일치\n"
                    + "- enabled: true/false(미전달 시 전체)\n"
                    + "- 정렬 화이트리스트: name, createdAt, updatedAt (무효 키는 name ASC, id DESC로 폴백)\n"
                    + "\n예시:\n"
                    + "GET /api/v1/car-models?q=avan&sort=name,asc&page=0&size=10\n"
                    + "GET /api/v1/car-models?q=son&enabled=true&sort=createdAt,desc&sort=name,asc"
    )
    @Parameters({
            @Parameter(name = "q", description = "모델명 부분 일치(대소문자 무시)", example = "avan"),
            @Parameter(name = "enabled", description = "활성 모델만(true) 또는 비활성 포함(false)", example = "true"),
            @Parameter(name = "page", description = "페이지(0..)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기(1..100)", example = "20"),
            @Parameter(name = "sort", description = "정렬(화이트리스트: name,createdAt,updatedAt)", example = "name,asc")
    })
    @GetMapping("/car-models")
    public ResponseEntity<CommonApiResponse<PageEnvelope<CarModelListItem>>> listCarModels(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        String name = (q == null ? "" : q.trim());
        Pageable pageable = PageRequest.of(p, s, parseCarModelSort(sort));
        Page<CarModelEntity> pg;
        if (enabled == null) {
            pg = carModelRepo.findByNameContainingIgnoreCase(name, pageable);
        } else {
            pg = carModelRepo.findByEnabledAndNameContainingIgnoreCase(enabled, name, pageable);
        }
        List<CarModelListItem> items = pg.getContent().stream()
                .map(e -> new CarModelListItem(e.getId(), e.getName(), e.isEnabled()))
                .toList();
        return CommonApiResponse.success(
                SuccessStatus.SEND_PCM_CARMODEL_LIST_SUCCESS,
                PageEnvelope.of(items, pg.getNumber(), pg.getSize(), pg.getTotalElements())
        );
    }

    @Operation(summary = "차량 모델 생성", description = "이름 중복 시 409를 반환합니다. enabled 미전달 시 기본값 true")
    @PostMapping("/car-models")
    public ResponseEntity<CommonApiResponse<CarModelListItem>> createCarModel(
            @RequestBody @Valid CreateCarModelRequest req
    ) {
        String name = req.name() == null ? "" : req.name().trim();
        if (name.isEmpty()) {
            // Bean Validation에서 차단되지만, 방어적 처리
            throw new IllegalArgumentException("name must not be blank");
        }
        if (carModelRepo.existsByNameIgnoreCase(name)) {
            throw new ConflictException(ErrorStatus.CONFLICT_RESOURCE_ALREADY_EXISTS);
        }
        boolean enabled = (req.enabled() == null) ? true : req.enabled();
        CarModelEntity saved = carModelRepo.save(CarModelEntity.builder()
                .name(name)
                .enabled(enabled)
                .build());
        CarModelListItem dto = new CarModelListItem(saved.getId(), saved.getName(), saved.isEnabled());
        return CommonApiResponse.success(SuccessStatus.SEND_CARMODEL_CREATE_SUCCESS, dto);
    }

    private Sort parseCarModelSort(List<String> sortParams) {
        // whitelist: name, createdAt, updatedAt
        Sort defaultSort = Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.desc("id"));
        if (sortParams == null || sortParams.isEmpty()) return defaultSort;
        try {
            List<Sort.Order> orders = sortParams.stream().map(s -> {
                String[] arr = s.split(",");
                String prop = arr[0].trim();
                String dir = arr.length > 1 ? arr[1].trim().toLowerCase() : "asc";
                Sort.Order order = "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
                return order.ignoreCase();
            }).toList();
            // apply whitelist
            List<String> allowed = List.of("name", "createdAt", "updatedAt");
            List<Sort.Order> filtered = orders.stream()
                    .filter(o -> allowed.contains(o.getProperty()))
                    .toList();
            return filtered.isEmpty() ? defaultSort : Sort.by(filtered);
        } catch (Exception e) {
            return defaultSort;
        }
    }

    @Operation(summary = "특정 부품을 사용하는 차량 모델 목록", description = "부품 ID로 차량 모델(CarModel) 목록을 조회합니다. 페이지/사이즈/정렬은 컨트롤러에서 래핑합니다.")
    @GetMapping("/parts/{partId}/car-models")
    public ResponseEntity<CommonApiResponse<PageEnvelope<CarModelSummary>>> listCarModelsByPart(
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
        return CommonApiResponse.success(SuccessStatus.SEND_PCM_CARMODEL_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "차량 모델에 적용 가능한 부품 목록", description = "차량 모델 ID로 부품(Part) 목록을 조회합니다. 페이지/사이즈/정렬은 컨트롤러에서 래핑합니다.")
    @GetMapping("/car-models/{carModelId}/parts")
    public ResponseEntity<CommonApiResponse<PageEnvelope<PartSummaryResponse>>> listPartsByCarModel(
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
        return CommonApiResponse.success(SuccessStatus.SEND_PCM_PART_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "부품-차량 모델 매핑 생성", description = "(partId, carModelId) 조합 생성. 비활성 매핑 존재 시 재활성화")
    @PostMapping("/parts/{partId}/car-models")
    public ResponseEntity<CommonApiResponse<PartCarModelDetail>> createMapping(
            @PathVariable Long partId,
            @RequestBody @Valid CreateMappingRequest req
    ) {
        var detail = pcmService.createMapping(partId, req);
        return CommonApiResponse.success(SuccessStatus.SEND_PCM_CREATE_SUCCESS, detail);
    }

    @Operation(summary = "부품-차량 모델 매핑 수정", description = "note/enabled 변경")
    @PatchMapping("/parts/{partId}/car-models/{carModelId}")
    public ResponseEntity<CommonApiResponse<PartCarModelDetail>> updateMapping(
            @PathVariable Long partId,
            @PathVariable Long carModelId,
            @RequestBody @Valid UpdateMappingRequest req
    ) {
        var detail = pcmService.updateMapping(partId, carModelId, req);
        return CommonApiResponse.success(SuccessStatus.SEND_PCM_UPDATE_SUCCESS, detail);
    }

    @Operation(summary = "부품-차량 모델 매핑 삭제(soft)", description = "enabled=false 처리")
    @DeleteMapping("/parts/{partId}/car-models/{carModelId}")
    public ResponseEntity<CommonApiResponse<Map<String, Boolean>>> deleteMapping(
            @PathVariable Long partId,
            @PathVariable Long carModelId
    ) {
        pcmService.deleteMapping(partId, carModelId);
        return CommonApiResponse.success(SuccessStatus.SEND_PCM_DELETE_SUCCESS, Map.of("deleted", true));
    }

    @Operation(summary = "차량 모델 활성 상태 토글", description = "차량 모델이 활성중에는 매핑에 참조 중이면 409를 반환, 성공 시 enabled=false로 비활성화 처리합니다. 비활성화 상태에서는 활성화 시킵니다.")
    @PatchMapping("/car-models/{id}/enable")
    public ResponseEntity<CommonApiResponse<Map<String, Boolean>>> toggleCarModelEnable(
            @PathVariable Long id
    ) {
        var cm = carModelRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("CarModel not found: " + id));
        long refCount = pcmRepo.countByCarModelIdAndEnabledTrue(id);
        if (refCount > 0) {
            throw new ConflictException(ErrorStatus.CARMODEL_HAS_MAPPINGS);
        }
        if (cm.isEnabled()) {
            cm.setEnabled(false);
            carModelRepo.save(cm);
        } else {
            cm.setEnabled(true);
            carModelRepo.save(cm);
        }
        return CommonApiResponse.success(SuccessStatus.SEND_CARMODEL_ENABLE_TOGGLED_SUCCESS,
                Map.of("toggled", true));
    }
}
