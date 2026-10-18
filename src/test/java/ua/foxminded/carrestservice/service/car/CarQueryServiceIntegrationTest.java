
package ua.foxminded.carrestservice.service.car;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static ua.foxminded.carrestservice.service.car.CarServiceTestConstants.*;

import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;
import ua.foxminded.carrestservice.service.car.dto.CarSearchRequest;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgreSQLTestConfig.class)
@SqlMergeMode(MERGE)
@Sql(
        scripts = "/sql/car/car-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarQueryServiceIntegrationTest {

    private static final Pageable DEFAULT_PAGEABLE =
            PageRequest.of(0, 12, Sort.by("id"));

    private static final CarSearchRequest NO_FILTERS =
            new CarSearchRequest(null, null, null, null, null);

    @Autowired
    private CarQueryService carQueryService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("findById: existing car -> returns complete CarDetails")
    void findById_existingCar_returnsDetails() {
        var result = carQueryService.findById(CAR_ID);

        assertThat(result).isEqualTo(new CarDetails(
                CAR_ID,
                YEAR_2020,
                COROLLA_ID,
                TOYOTA_ID,
                Set.of(SEDAN_ID, HYBRID_ID)));
    }

    @Test
    @DisplayName("findById: car without categories -> returns empty category set")
    void findById_carWithoutCategories_returnsEmptySet() {
        var result = carQueryService.findById(
                CAR_WITHOUT_CATEGORIES_ID);

        assertThat(result.id())
                .isEqualTo(CAR_WITHOUT_CATEGORIES_ID);

        assertThat(result.categoryIds()).isEmpty();
    }

    @Test
    @DisplayName("findById: unknown car -> throws EntityNotFoundException")
    void findById_unknownCar_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> carQueryService.findById(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(
                        "Car not found: id=%d".formatted(UNKNOWN_ID));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("searchCases")
    @DisplayName("findAll: search filters -> returns matching cars")
    void findAll_searchFilters_returnsMatchingCars(
            String scenario,
            CarSearchRequest request,
            List<Long> expectedIds) {

        var page = carQueryService.findAll(
                request, DEFAULT_PAGEABLE);

        assertThat(page.getContent())
                .extracting(CarDetails::id)
                .containsExactlyElementsOf(expectedIds);

        assertThat(page.getTotalElements())
                .isEqualTo(expectedIds.size());
    }

    static Stream<Arguments> searchCases() {
        return Stream.of(
                Arguments.of(
                        "no filters",
                        NO_FILTERS,
                        List.of(
                                CAR_ID,
                                SECOND_CAR_ID,
                                CAR_WITHOUT_CATEGORIES_ID,
                                BMW_CAR_ID)),

                Arguments.of(
                        "manufacturer only",
                        request(TOYOTA_ID, null, null, null, null),
                        List.of(
                                CAR_ID,
                                SECOND_CAR_ID,
                                CAR_WITHOUT_CATEGORIES_ID)),

                Arguments.of(
                        "model only",
                        request(null, COROLLA_ID, null, null, null),
                        List.of(CAR_ID, CAR_WITHOUT_CATEGORIES_ID)),

                Arguments.of(
                        "matching manufacturer and model",
                        request(
                                TOYOTA_ID, COROLLA_ID,
                                null, null, null),
                        List.of(CAR_ID, CAR_WITHOUT_CATEGORIES_ID)),

                Arguments.of(
                        "conflicting manufacturer and model",
                        request(
                                BMW_ID, COROLLA_ID,
                                null, null, null),
                        List.of()),

                Arguments.of(
                        "unknown manufacturer",
                        request(UNKNOWN_ID, null, null, null, null),
                        List.of()),

                Arguments.of(
                        "unknown model",
                        request(null, UNKNOWN_ID, null, null, null),
                        List.of()),

                Arguments.of(
                        "year from only",
                        request(
                                null, null,
                                Year.of(2021), null, null),
                        List.of(SECOND_CAR_ID, CAR_WITHOUT_CATEGORIES_ID)),

                Arguments.of(
                        "year to only",
                        request(
                                null, null,
                                null, YEAR_2020, null),
                        List.of(CAR_ID, BMW_CAR_ID)),

                Arguments.of(
                        "year range",
                        request(
                                null, null,
                                YEAR_2020, Year.of(2021), null),
                        List.of(CAR_ID, SECOND_CAR_ID, BMW_CAR_ID)),

                Arguments.of(
                        "same year boundaries",
                        request(
                                null, null,
                                YEAR_2020, YEAR_2020, null),
                        List.of(CAR_ID, BMW_CAR_ID)),

                Arguments.of(
                        "single category",
                        request(
                                null, null,
                                null, null, Set.of(SEDAN_ID)),
                        List.of(CAR_ID, SECOND_CAR_ID)),

                Arguments.of(
                        "all selected categories",
                        request(
                                null, null,
                                null, null,
                                Set.of(SEDAN_ID, HYBRID_ID)),
                        List.of(CAR_ID)),

                Arguments.of(
                        "categories without common cars",
                        request(
                                null, null,
                                null, null,
                                Set.of(SEDAN_ID, SUV_ID)),
                        List.of()),

                Arguments.of(
                        "unknown category",
                        request(
                                null, null,
                                null, null, Set.of(UNKNOWN_ID)),
                        List.of()),

                Arguments.of(
                        "empty category set",
                        request(
                                null, null,
                                null, null, Set.of()),
                        List.of(
                                CAR_ID,
                                SECOND_CAR_ID,
                                CAR_WITHOUT_CATEGORIES_ID,
                                BMW_CAR_ID)),

                Arguments.of(
                        "all filter blocks combined",
                        request(
                                TOYOTA_ID,
                                COROLLA_ID,
                                YEAR_2020,
                                YEAR_2022,
                                Set.of(SEDAN_ID, HYBRID_ID)),
                        List.of(CAR_ID)));
    }

    @Test
    @DisplayName("findAll: paginated search -> returns correct pages and totals")
    void findAll_paginatedSearch_returnsCorrectPagesAndTotals() {
        var firstPage = carQueryService.findAll(
                NO_FILTERS,
                PageRequest.of(0, 2, Sort.by("id")));

        var secondPage = carQueryService.findAll(
                NO_FILTERS,
                PageRequest.of(1, 2, Sort.by("id")));

        assertThat(firstPage.getContent())
                .extracting(CarDetails::id)
                .containsExactly(CAR_ID, SECOND_CAR_ID);

        assertThat(secondPage.getContent())
                .extracting(CarDetails::id)
                .containsExactly(
                        CAR_WITHOUT_CATEGORIES_ID,
                        BMW_CAR_ID);

        assertThat(firstPage.getContent().get(0).categoryIds())
                .containsExactlyInAnyOrder(SEDAN_ID, HYBRID_ID);

        assertThat(firstPage.getContent().get(1).categoryIds())
                .containsExactly(SEDAN_ID);

        assertThat(secondPage.getContent().get(0).categoryIds())
                .isEmpty();

        assertThat(secondPage.getContent().get(1).categoryIds())
                .containsExactly(SUV_ID);

        assertThat(secondPage.getContent().get(1).manufacturerId())
                .isEqualTo(BMW_ID);

        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("findAll: page beyond available cars -> returns empty page")
    void findAll_pageBeyondRange_returnsEmptyPage() {
        var pageable = PageRequest.of(3, 2, Sort.by("id"));

        var page = carQueryService.findAll(NO_FILTERS, pageable);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("findAll: category filter with pagination -> counts matching cars")
    void findAll_categoryFilterWithPagination_countsMatchingCars() {
        var request = request(
                null, null, null, null, Set.of(SEDAN_ID));

        var firstPage = carQueryService.findAll(
                request,
                PageRequest.of(0, 1, Sort.by("id")));

        var secondPage = carQueryService.findAll(
                request,
                PageRequest.of(1, 1, Sort.by("id")));

        assertThat(firstPage.getContent())
                .extracting(CarDetails::id)
                .containsExactly(CAR_ID);

        assertThat(secondPage.getContent())
                .extracting(CarDetails::id)
                .containsExactly(SECOND_CAR_ID);

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAll: year descending sort -> returns expected order")
    void findAll_yearDescendingSort_returnsExpectedOrder() {
        var sort = Sort.by(
                Sort.Order.desc("year"),
                Sort.Order.asc("id"));

        var page = carQueryService.findAll(
                NO_FILTERS,
                PageRequest.of(0, 12, sort));

        assertThat(page.getContent())
                .extracting(CarDetails::id)
                .containsExactly(
                        CAR_WITHOUT_CATEGORIES_ID,
                        SECOND_CAR_ID,
                        CAR_ID,
                        BMW_CAR_ID);
    }

    @Test
    @DisplayName("findAll: matching car -> maps complete CarDetails")
    void findAll_matchingCar_mapsCompleteDetails() {
        var request = request(
                TOYOTA_ID,
                COROLLA_ID,
                YEAR_2020,
                YEAR_2020,
                Set.of(SEDAN_ID, HYBRID_ID));

        var page = carQueryService.findAll(
                request, DEFAULT_PAGEABLE);

        assertThat(page.getContent())
                .singleElement()
                .isEqualTo(new CarDetails(
                        CAR_ID,
                        YEAR_2020,
                        COROLLA_ID,
                        TOYOTA_ID,
                        Set.of(SEDAN_ID, HYBRID_ID)));
    }

    @Test
    @DisplayName("findAll: car without categories -> returns empty category set")
    void findAll_carWithoutCategories_returnsEmptyCategorySet() {
        var request = request(
                null,
                COROLLA_ID,
                YEAR_2022,
                YEAR_2022,
                null);

        var page = carQueryService.findAll(
                request, DEFAULT_PAGEABLE);

        assertThat(page.getContent())
                .singleElement()
                .satisfies(details -> {
                    assertThat(details.id())
                            .isEqualTo(CAR_WITHOUT_CATEGORIES_ID);

                    assertThat(details.categoryIds()).isEmpty();
                });
    }
    
    @Test
    @DisplayName("findAll: entity graph -> avoids N+1 queries")
    void findAll_entityGraph_avoidsNPlusOne() {
        var statistics = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();

        statistics.setStatisticsEnabled(true);
        statistics.clear();

        var page = carQueryService.findAll(
                NO_FILTERS,
                PageRequest.of(0, 2, Sort.by("id")));

        assertThat(page.getContent()).hasSize(2);

        assertThat(statistics.getPrepareStatementCount())
                .isLessThanOrEqualTo(2);
    }

    private static CarSearchRequest request(
            Long manufacturerId,
            Long modelId,
            Year yearFrom,
            Year yearTo,
            Set<Long> categoryIds) {

        return new CarSearchRequest(
                manufacturerId,
                modelId,
                yearFrom,
                yearTo,
                categoryIds);
    }
}
