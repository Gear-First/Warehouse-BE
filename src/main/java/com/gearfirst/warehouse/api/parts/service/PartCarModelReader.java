package com.gearfirst.warehouse.api.parts.service;

/**
 * Part–CarModel mapping 존재 확인용 Port interface without introducing PCM entities yet. Default implementation returns
 * zero; tests 나 PCM repository 구현체에서 override 해서 사용.
 */
public interface PartCarModelReader {
    long countByPartId(Long partId);
}
