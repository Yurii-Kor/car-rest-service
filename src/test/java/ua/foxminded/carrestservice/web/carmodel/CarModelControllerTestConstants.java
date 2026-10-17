package ua.foxminded.carrestservice.web.carmodel;

import lombok.experimental.UtilityClass;

@UtilityClass
class CarModelControllerTestConstants {

    final String MODELS_URL = "/api/v1/models";
    final String MANUFACTURER_MODELS_URL = "/api/v1/manufacturers/{manufacturerId}/models";
    final String MANUFACTURER_MODELS_OPTIONS_URL = MANUFACTURER_MODELS_URL + "/options";

    final Long TOYOTA_ID = 101L;
    final Long UNKNOWN_MANUFACTURER_ID = 999998L;

    final Long COROLLA_ID = 201L;
    final Long CAMRY_ID = 202L;
    final Long UNKNOWN_MODEL_ID = 999999L;

    final String TOYOTA_NAME = "Toyota";
    final String COROLLA_NAME = "Corolla";
    final String CAMRY_NAME = "Camry";
}
