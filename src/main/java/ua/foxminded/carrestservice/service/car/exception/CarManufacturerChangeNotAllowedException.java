package ua.foxminded.carrestservice.service.car.exception;

public class CarManufacturerChangeNotAllowedException extends RuntimeException {

    public CarManufacturerChangeNotAllowedException(
            Long carId,
            Long currentManufacturerId,
            Long requestedManufacturerId) {

        super(
                "Cannot change car manufacturer: carId=%d, currentManufacturerId=%d, requestedManufacturerId=%d"
                        .formatted(
                                carId,
                                currentManufacturerId,
                                requestedManufacturerId));
    }
}
