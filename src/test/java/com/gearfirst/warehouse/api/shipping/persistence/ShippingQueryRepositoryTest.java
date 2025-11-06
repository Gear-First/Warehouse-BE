package com.gearfirst.warehouse.api.shipping.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummary;
import com.gearfirst.warehouse.api.shipping.dto.ShippingSearchCond;
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
class ShippingQueryRepositoryTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("ShippingQueryRepository.search smoke: executes with defaults and returns non-null page")
    void search_smoke_executes() {
        // given
        JPAQueryFactory qf = new JPAQueryFactory(em);
        ShippingQueryRepository repo = new ShippingQueryRepositoryImpl(qf);
        ShippingSearchCond cond = ShippingSearchCond.builder()
                .status("not-done")
                .date(null)
                .dateFrom(null)
                .dateTo(null)
                .warehouseCode(null)
                .shippingNo(null)
                .branchName(null)
                .build();

        // when
        Page<ShippingNoteSummary> page =
                repo.search(cond, PageRequest.of(0, 20));

        // then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotNull();
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
    }
}
