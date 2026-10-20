package ua.foxminded.carrestservice.web.category;

import lombok.experimental.UtilityClass;

@UtilityClass
class CategoryControllerTestConstants {

    final String BASE_URL = "/api/v1/categories";

    final Long SEDAN_ID = 101L;
    final Long HATCHBACK_ID = 102L;
    final Long COUPE_ID = 103L;
    final Long TRUCK_ID = 104L;
    final Long SUV_ID = 105L;
    final Long UNKNOWN_ID = 999999L;

    final String SEDAN_NAME = "Sedan";
    final String HATCHBACK_NAME = "Hatchback";
    final String COUPE_NAME = "Coupe";
    final String TRUCK_NAME = "Truck";
    final String SUV_NAME = "SUV";
    final String SPORTS_CAR_NAME = "Sports Car";

    final int INITIAL_CATEGORIES_COUNT = 4;
}
