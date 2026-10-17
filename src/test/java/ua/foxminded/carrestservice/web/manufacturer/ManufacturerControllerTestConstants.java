package ua.foxminded.carrestservice.web.manufacturer;

import lombok.experimental.UtilityClass;

@UtilityClass
class ManufacturerControllerTestConstants {

    final String BASE_URL = "/api/v1/manufacturers";

    final Long TOYOTA_ID = 1L;
    final Long BMW_ID = 2L;
    final Long UNKNOWN_ID = 999999L;

    final String TOYOTA_NAME = "Toyota";
    final String BMW_NAME = "BMW";
}
