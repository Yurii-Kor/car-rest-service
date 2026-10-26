package ua.foxminded.carrestservice.web.carmodel;

import java.util.List;

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
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;
import ua.foxminded.carrestservice.web.carmodel.dto.CarModelResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

public interface CarModelApi {

    @Operation(
            summary = "Create a car model",
            description = "Creates a model for an existing manufacturer. "
                    + "Model names must be unique within a manufacturer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Car model created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created car model",
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
                    description = "Manufacturer not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Car model already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<IdResponse> create(CarModelCreateRequest request);

    @Operation(summary = "Get a car model by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Car model found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CarModelResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Car model not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    CarModelResponse findById(Long id);

    @Operation(
            summary = "Get car models by manufacturer",
            description = "Returns a paginated list of models "
                    + "belonging to an existing manufacturer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Car models retrieved",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid manufacturer ID or pagination parameters",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Manufacturer not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    Page<CarModelResponse> findAllByManufacturerId(
            Long manufacturerId, Pageable pageable);

    @Operation(
            summary = "Get car model options",
            description = "Returns all models of an existing manufacturer "
                    + "sorted by name, without pagination."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Car model options retrieved",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or missing manufacturer ID",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Manufacturer not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    List<CarModelResponse> findAllForSelection(Long manufacturerId);

    @Operation(
            summary = "Update a car model",
            description = "Updates the model name while preserving "
                    + "its manufacturer. The new name must be unique "
                    + "within that manufacturer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Car model updated",
                    content = @Content
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
                    description = "Car model not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Car model name already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void update(Long id, CarModelUpdateRequest request);

    @Operation(summary = "Delete a car model")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Car model deleted",
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
                    description = "Car model not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Car model is used by existing cars",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void delete(Long id);
}
