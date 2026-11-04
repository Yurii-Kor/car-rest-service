package ua.foxminded.carrestservice.service.carmodel;

import lombok.experimental.UtilityClass;

@UtilityClass
class CarModelServiceTestConstants {

    final Long TOYOTA_ID = 101L;
    final Long HONDA_ID = 102L;
    final Long EMPTY_MANUFACTURER_ID = 104L;
    final Long UNKNOWN_MANUFACTURER_ID = 999998L;

    final Long COROLLA_ID = 201L;
    final Long CAMRY_ID = 202L;
    final Long CIVIC_ID = 203L;
    final Long UNKNOWN_MODEL_ID = 999999L;
    
    final Long INITIAL_CAR_MODELS_COUNT = 5L;
    final Long AMOUNT_TOYOTA_MODELS = 2L;
    final Integer EXPECTED_TOTAL_PAGES = 2;

    final String COROLLA_NAME = "Corolla";
    final String CAMRY_NAME = "Camry";
    final String CAMRY_UPDATED = "Camry Hybrid";
    final String CIVIC_NAME = "Civic";
    final String YARVIS_NAME = "Yaris";
}
