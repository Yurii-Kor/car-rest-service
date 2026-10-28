package ua.foxminded.carrestservice.web.manufacturer;

import java.util.List;

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
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;
import ua.foxminded.carrestservice.web.manufacturer.dto.ManufacturerResponse;

public interface ManufacturerApi {

    @Operation(
            summary = "Create a manufacturer",
            description = "Creates a new manufacturer. "
                    + "Manufacturer names must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Manufacturer created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created manufacturer",
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
                    responseCode = "409",
                    description = "Manufacturer already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<IdResponse> create(ManufacturerRequest request);

    @Operation(summary = "Get a manufacturer by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Manufacturer found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ManufacturerResponse.class)
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
    ManufacturerResponse findById(Long id);

    @Operation(
            summary = "Get manufacturers",
            description = "Returns a paginated list of manufacturers."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Manufacturers retrieved",
            useReturnTypeSchema = true
    )
    Page<ManufacturerResponse> findAll(@ParameterObject Pageable pageable);

    @Operation(
            summary = "Get manufacturer options",
            description = "Returns all manufacturers sorted by name, "
                    + "without pagination."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Manufacturer options retrieved",
            useReturnTypeSchema = true
    )
    List<ManufacturerResponse> findAllForSelection();

    @Operation(
            summary = "Update a manufacturer",
            description = "Updates the manufacturer name. "
                    + "The new name must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Manufacturer updated",
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
                    description = "Manufacturer not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Manufacturer name already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void update(Long id, ManufacturerRequest request);

    @Operation(
            summary = "Delete a manufacturer",
            description = "Deletes a manufacturer if it is not referenced "
                    + "by existing data."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Manufacturer deleted",
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
                    description = "Manufacturer not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Manufacturer cannot be deleted "
                            + "because of existing references",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void delete(Long id);
}
