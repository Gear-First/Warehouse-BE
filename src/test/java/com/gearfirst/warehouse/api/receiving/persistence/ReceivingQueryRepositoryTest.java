package com.gearfirst.warehouse.api.receiving.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingSearchCond;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceivingQueryRepositoryTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("ReceivingQueryRepository.search smoke: executes with defaults and returns non-null page")
    void search_smoke_executes() {
        // given
        JPAQueryFactory qf = new JPAQueryFactory(em);
        ReceivingQueryRepository repo = new ReceivingQueryRepositoryImpl(qf);
        ReceivingSearchCond cond = ReceivingSearchCond.builder()
                .status("not-done")
                .date(null)
                .dateFrom(null)
                .dateTo(null)
                .warehouseCode(null)
                .receivingNo(null)
                .supplierName(null)
                .build();

        // when
        Page<com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummary> page =
                repo.search(cond, PageRequest.of(0, 20));

        // then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotNull();
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
    }
}
