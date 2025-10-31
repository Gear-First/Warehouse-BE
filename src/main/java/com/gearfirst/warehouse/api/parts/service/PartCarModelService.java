package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.CreateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.PartCarModelDetail;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.UpdateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import java.util.List;

public interface PartCarModelService {
    List<CarModelSummary> listCarModelsByPart(Long partId, String name);

    List<PartSummaryResponse> listPartsByCarModel(Long carModelId, String code, String name, Long categoryId);

    PartCarModelDetail createMapping(Long partId, CreateMappingRequest request);

    PartCarModelDetail updateMapping(Long partId, Long carModelId, UpdateMappingRequest request);

    void deleteMapping(Long partId, Long carModelId);
}
