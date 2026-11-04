package ua.foxminded.carrestservice.web.car;

import java.time.Year;

import lombok.experimental.UtilityClass;

@UtilityClass
class CarControllerTestConstants {

    final String BASE_URL = "/api/v1/cars";

    final Long CAR_ID = 301L;
    final Long SECOND_CAR_ID = 302L;
    final Long UNKNOWN_CAR_ID = 999999L;

    final Long MANUFACTURER_ID = 101L;
    final Long OTHER_MANUFACTURER_ID = 102L;

    final Long MODEL_ID = 201L;
    final Long SECOND_MODEL_ID = 202L;
    final Long UNKNOWN_MODEL_ID = 999998L;

    final Long SEDAN_ID = 401L;
    final Long SUV_ID = 402L;

    final Year CAR_YEAR = Year.of(2020);
    final Year UPDATED_YEAR = Year.of(2021);

    final Long DB_EXISTING_CAR_ID = 101L;
    final Long DB_SECOND_CAR_ID = 102L;
    final Long DB_CAR_WITHOUT_CATEGORIES_ID = 103L;
    final Long DB_BMW_CAR_ID = 104L;

    final Long DB_TOYOTA_ID = 1L;
    final Long DB_COROLLA_ID = 11L;
    final Long DB_CAMRY_ID = 12L;

    final Long DB_SEDAN_ID = 31L;
    final Long DB_HYBRID_ID = 32L;
    final Long DB_SUV_ID = 33L;

    final Long DB_UNKNOWN_ID = 9999L;

    final Long INITIAL_CAR_COUNT = 4L;
}
