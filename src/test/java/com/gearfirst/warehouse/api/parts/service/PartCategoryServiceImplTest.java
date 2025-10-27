package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CreateCategoryRequest;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.UpdateCategoryRequest;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
class PartCategoryServiceImplTest {

    @Autowired
    private PartCategoryService service;

    @Test
    @DisplayName("create: 카테고리 생성 후 조회까지 성공")
    void create_then_get_success() {
        var created = service.create(new CreateCategoryRequest("Filter", "Oil/Air filters"));
        assertNotNull(created.id());
        var loaded = service.get(created.id());
        assertEquals("Filter", loaded.name());
    }

    @Test
    @DisplayName("create: 중복 이름이면 409 Conflict")
    void create_conflict_onDuplicateName() {
        service.create(new CreateCategoryRequest("Brake", "브레이크"));
        assertThrows(ConflictException.class, () -> service.create(new CreateCategoryRequest("Brake", "브레이크2")));
    }

    @Test
    @DisplayName("update: 존재하지 않으면 404")
    void update_notFound() {
        assertThrows(NotFoundException.class, () -> service.update(9999L, new UpdateCategoryRequest("X", "Y")));
    }
}
