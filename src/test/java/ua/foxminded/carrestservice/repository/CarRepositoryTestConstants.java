package ua.foxminded.carrestservice.repository;

import java.time.Year;

import lombok.experimental.UtilityClass;

@UtilityClass
class CarRepositoryTestConstants {

    final Long COROLLA_2020_ID = 101L;
    final Long CAMRY_2021_ID = 102L;
    final Long COROLLA_2022_ID = 103L;
    final Long X5_2020_ID = 104L;

    final Long TOYOTA_ID = 1L;
    final Long BMW_ID = 2L;

    final Long COROLLA_ID = 11L;
    final Long CAMRY_ID = 12L;
    final Long X5_ID = 21L;

    final Long SEDAN_ID = 31L;
    final Long HYBRID_ID = 32L;
    final Long SUV_ID = 33L;

    final Long UNKNOWN_ID = 9999L;

    final Year YEAR_2020 = Year.of(2020);
    final Year YEAR_2021 = Year.of(2021);
    final Year YEAR_2022 = Year.of(2022);
    final Year YEAR_2023 = Year.of(2023);

    final long TOTAL_CARS = 4;
}
