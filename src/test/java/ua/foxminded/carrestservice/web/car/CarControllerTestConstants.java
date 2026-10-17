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
}
