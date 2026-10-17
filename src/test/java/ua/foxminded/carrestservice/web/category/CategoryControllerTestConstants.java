package ua.foxminded.carrestservice.web.category;

import lombok.experimental.UtilityClass;

@UtilityClass
class CategoryControllerTestConstants {

    final String BASE_URL = "/api/v1/categories";

    final Long SEDAN_ID = 101L;
    final Long SUV_ID = 102L;
    final Long UNKNOWN_ID = 999999L;

    final String SEDAN_NAME = "Sedan";
    final String SUV_NAME = "SUV";
}
