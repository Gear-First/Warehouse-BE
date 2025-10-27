package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartDtos.CreatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.UpdatePartRequest;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
class PartServiceImplTest {

    @Autowired
    private PartService partService;

    @Autowired
    private PartCategoryService categoryService;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        var cat = categoryService.create(new com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CreateCategoryRequest("Filter", "Oil/Air"));
        categoryId = cat.id();
    }

    @Test
    @DisplayName("create: 카테고리 미존재 시 404")
    void create_notFound_category() {
        assertThrows(NotFoundException.class, () -> partService.create(new CreatePartRequest("P-1001", "오일필터", 12000, 9999L, null)));
    }

    @Test
    @DisplayName("create: 정상 생성 후 상세 조회 성공")
    void create_then_get_success() {
        var detail = partService.create(new CreatePartRequest("P-1001", "오일필터", 12000, categoryId, null));
        assertNotNull(detail.id());
        var loaded = partService.get(detail.id());
        assertEquals("P-1001", loaded.code());
        assertEquals("Filter", loaded.category().name());
    }

    @Test
    @DisplayName("create: 코드 중복 시 409")
    void create_conflict_duplicateCode() {
        partService.create(new CreatePartRequest("P-1002", "에어필터", 8000, categoryId, null));
        assertThrows(ConflictException.class, () -> partService.create(new CreatePartRequest("P-1002", "오일필터", 12000, categoryId, null)));
    }

    @Test
    @DisplayName("update: 코드 변경 시 중복이면 409")
    void update_conflict_duplicateCodeOnChange() {
        var a = partService.create(new CreatePartRequest("P-A", "A", 1000, categoryId, null));
        var b = partService.create(new CreatePartRequest("P-B", "B", 2000, categoryId, null));
        assertThrows(ConflictException.class, () -> partService.update(b.id(), new UpdatePartRequest("P-A", "B2", 2100, categoryId, null, true)));
    }

    @Test
    @DisplayName("delete: soft delete(enabled=false) 적용")
    void delete_soft() {
        var p = partService.create(new CreatePartRequest("P-DEL", "삭제테스트", 1000, categoryId, null));
        partService.delete(p.id());
        var loaded = partService.get(p.id());
        assertFalse(loaded.enabled());
    }
}
