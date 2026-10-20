package ua.foxminded.carrestservice.service.category;

import lombok.experimental.UtilityClass;

@UtilityClass
class CategoryServiceTestConstants {

    final Long SEDAN_ID = 101L;
    final Long HATCHBACK_ID = 102L;
    final Long COUPE_ID = 103L;
    final Long TRUCK_ID = 104L;
    final Long UNKNOWN_ID = 999999L;
    
    final Long INITIAL_CATEGORIES_COUNT = 4L;
    final Long AMOUNT_ASSOCIATIONS_IN_DB = 1L;
    final Long MODELS_BEFORE = 2L;
    
    final Integer TOTAL_PAGES = 2;

    final String SEDAN_NAME = "Sedan";
    final String SEDAN_NAME_UPDATED = "Family Sedan";
    final String HATCHBACK_NAME = "Hatchback";
    final String COUPE_NAME = "Coupe";
    final String TRUCK_NAME = "Truck";
    final String CROSSOVER_NAME = "Crossover";
}
