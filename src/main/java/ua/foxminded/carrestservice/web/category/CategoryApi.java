package ua.foxminded.carrestservice.web.category;

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
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;
import ua.foxminded.carrestservice.web.category.dto.CategoryResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

public interface CategoryApi {

    @Operation(
            summary = "Create a category",
            description = "Creates a new category. "
                    + "Category names must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Category created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created category",
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
                    description = "Category already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<IdResponse> create(CategoryRequest request);

    @Operation(summary = "Get a category by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    CategoryResponse findById(Long id);

    @Operation(
            summary = "Get categories",
            description = "Returns a paginated list of categories."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved",
            useReturnTypeSchema = true
    )
    Page<CategoryResponse> findAll(Pageable pageable);

    @Operation(
            summary = "Get category options",
            description = "Returns all categories sorted by name, "
                    + "without pagination."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Category options retrieved",
            useReturnTypeSchema = true
    )
    List<CategoryResponse> findAllForSelection();

    @Operation(
            summary = "Update a category",
            description = "Updates the category name. "
                    + "The new name must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Category updated",
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
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category name already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void update(Long id, CategoryRequest request);

    @Operation(
            summary = "Delete a category",
            description = "Deletes a category if it is not used "
                    + "by existing cars."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Category deleted",
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
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category is used by existing cars",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(
                                    implementation = ProblemDetail.class)
                    )
            )
    })
    void delete(Long id);
}
