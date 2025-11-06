package com.gearfirst.warehouse.common;

import static org.assertj.core.api.Assertions.assertThat;
import static com.gearfirst.warehouse.api.parts.persistence.entity.QPartEntity.partEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuerydslSmokeTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Q-class generation & JPAQueryFactory basic select works")
    void qtypes_and_jpaqueryfactory_smoke() {
        // given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        // when
        Long count = queryFactory
                .select(partEntity.id.count())
                .from(partEntity)
                .fetchOne();

        // then
        // If Q-classes are missing or JPAQueryFactory misconfigured, compilation/runtime will fail.
        // Here we only assert the query executed and returned a value (may be 0 on empty DB).
        assertThat(count).isNotNull();
    }
}
