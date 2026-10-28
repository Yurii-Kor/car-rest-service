package ua.foxminded.carrestservice.web.car;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;
import ua.foxminded.carrestservice.service.car.dto.CarSearchRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

public interface CarApi {

    @Operation(
            summary = "Create a car",
            description = "Creates a car for an existing model "
                    + "and categories. The model-year combination "
                    + "must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Car created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created car",
                            schema = @Schema(type = "string")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = IdResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Car model or category not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Model-year combination already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<IdResponse> create(CarCreateRequest request);

    @Operation(
            summary = "Get a car by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Car found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CarDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Car not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    CarDetails findById(Long id);

    @Operation(
            summary = "Search cars",
            description = "Returns a paginated list of cars "
                    + "filtered by manufacturer, model, "
                    + "manufacturing year range, and categories."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cars retrieved",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameters",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    Page<CarDetails> findAll(
            @ParameterObject CarSearchRequest request,
            @ParameterObject Pageable pageable);

    @Operation(
            summary = "Update a car",
            description = "Partially updates a car. "
                    + "At least one field must be provided. "
                    + "Changing the manufacturer is not allowed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Car updated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid update request",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Car, model, or category not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicting model, year, or manufacturer",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void update(Long id, CarUpdateRequest request);

    @Operation(
            summary = "Delete a car"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Car deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Car not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void delete(Long id);
}
