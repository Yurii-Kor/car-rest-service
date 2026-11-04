package ua.foxminded.carrestservice.web.manufacturer;

import lombok.experimental.UtilityClass;

@UtilityClass
class ManufacturerControllerTestConstants {

    final String BASE_URL = "/api/v1/manufacturers";

    final Long TOYOTA_ID = 101L;
    final Long BMW_ID = 102L;
    final Long UNUSED_ID = 103L;
    final Long PROTOTYPE_ID = 104L;
    final Long UNKNOWN_ID = 999999L;

    final String TOYOTA_NAME = "Toyota";
    final String BMW_NAME = "BMW";
    final String LAND_ROVER_NAME = "Land Rover";
    final String BMW_MOTORS_NAME = "BMW Motors";

    final String TOYOTA_UPDATED_NAME = "Toyota Motors";
    final String UNUSED_NAME = "Unused Motors";
    final String PROTOTYPE_NAME = "Prototype Motors";

    final int INITIAL_MANUFACTURERS_COUNT = 4;
}
