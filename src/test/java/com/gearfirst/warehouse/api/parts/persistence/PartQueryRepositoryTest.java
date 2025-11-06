package com.gearfirst.warehouse.api.parts.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import com.gearfirst.warehouse.api.parts.persistence.entity.CarModelEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCarModelEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCategoryEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PartQueryRepositoryTest {

    @Autowired
    EntityManager em;

    PartQueryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PartQueryRepositoryImpl(new JPAQueryFactory(em));
        seed();
    }

    private void seed() {
        // Categories
        PartCategoryEntity catFilter = PartCategoryEntity.builder().name("Filter").description("Oil/Air").enabled(true).build();
        PartCategoryEntity catBrake = PartCategoryEntity.builder().name("Brake").description("Pad").enabled(true).build();
        em.persist(catFilter);
        em.persist(catBrake);

        // Car models
        CarModelEntity avante = CarModelEntity.builder().name("Avante").enabled(true).build();
        CarModelEntity sonata = CarModelEntity.builder().name("Sonata").enabled(true).build();
        em.persist(avante);
        em.persist(sonata);

        // Parts
        PartEntity p1 = PartEntity.builder()
                .code("P-1001")
                .name("Engine Oil Filter")
                .price(12000)
                .categoryId(catFilter.getId())
                .imageUrl("/img/p-1001.png")
                .enabled(true)
                .safetyStockQty(0)
                .build();
        PartEntity p2 = PartEntity.builder()
                .code("P-2001")
                .name("Brake Pad Front")
                .price(56000)
                .categoryId(catBrake.getId())
                .imageUrl("/img/p-2001.png")
                .enabled(true)
                .safetyStockQty(0)
                .build();
        em.persist(p1);
        em.persist(p2);

        // Part-CarModel links
        em.persist(PartCarModelEntity.builder().partId(p1.getId()).carModelId(avante.getId()).enabled(true).build());
        em.persist(PartCarModelEntity.builder().partId(p1.getId()).carModelId(sonata.getId()).enabled(true).build());
        em.persist(PartCarModelEntity.builder().partId(p2.getId()).carModelId(sonata.getId()).enabled(true).build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("search by q and categoryName and carModelName with whitelist sort and paging")
    void search_filters_and_sort_and_paging() {
        // given
        PartSearchCond cond = PartSearchCond.builder()
                .q("Filter") // matches p1 by name
                .categoryName("Filt") // category contains
                .carModelName("Avan") // matches Avante linked to p1
                .enabled(true)
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"), Sort.Order.desc("updatedAt")));

        // when
        Page<com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem> page = repository.search(cond, pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        var item = page.getContent().get(0);
        assertThat(item.getCode()).isEqualTo("P-1001");
        assertThat(item.getCategoryName()).isEqualTo("Filter");
        assertThat(item.getCarModelNames()).containsExactlyInAnyOrder("Avante", "Sonata");
    }

    @Test
    @DisplayName("invalid sort keys are ignored and baseline fallback applies (code ASC, id DESC)")
    void invalid_sort_fallback() {
        // given
        PartSearchCond cond = PartSearchCond.builder().q("P-").build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("unknownKey")));

        // when
        Page<com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem> page = repository.search(cond, pageable);

        // then
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).isNotEmpty();
    }
}
