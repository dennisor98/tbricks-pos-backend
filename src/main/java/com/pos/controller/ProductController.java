package com.pos.controller;

import com.pos.dto.GetProductsResponse;
import com.pos.dto.ProductDto;
import com.pos.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Multi-tenant product management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Retrieve all active products for the current tenant. Products are filtered by tenant context."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class),
                examples = @ExampleObject(
                    name = "Products List",
                    value = "[{\"id\":1,\"name\":\"Laptop Pro 15\\\"\",\"description\":\"High-performance laptop with 15-inch display\",\"sku\":\"LAP001\",\"price\":999.99,\"quantity\":25,\"minQuantity\":5,\"maxQuantity\":100,\"costPrice\":750.00,\"imageUrl\":\"https://example.com/laptop.jpg\",\"active\":true,\"categoryId\":1,\"categoryName\":\"Electronics\"}]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER','SUPER_ADMIN')")
    public ResponseEntity<GetProductsResponse> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        GetProductsResponse response =  new GetProductsResponse();
        response.setProducts(products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieve a specific product by its ID. Only returns products beStringing to the current tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class),
                examples = @ExampleObject(
                    name = "Product Details",
                    value = "{\"id\":1,\"name\":\"Laptop Pro 15\\\"\",\"description\":\"High-performance laptop with 15-inch display\",\"sku\":\"LAP001\",\"price\":999.99,\"quantity\":25,\"minQuantity\":5,\"maxQuantity\":100,\"costPrice\":750.00,\"imageUrl\":\"https://example.com/laptop.jpg\",\"active\":true,\"categoryId\":1,\"categoryName\":\"Electronics\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER','SUPER_ADMIN')")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable String id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(
        summary = "Get product by SKU",
        description = "Retrieve a specific product by its SKU. Only returns products beStringing to the current tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER','SUPER_ADMIN')")
    public ResponseEntity<Object> getProductBySku(
            @Parameter(description = "Product SKU", required = true, example = "LAP001")
            @PathVariable String sku) {
        ProductDto product = productService.getProductBySku(sku);
        if(product == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Product with SKU not found"));
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieve all active products in a specific category for the current tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class)
            )
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER','SUPER_ADMIN')")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable String categoryId) {
        List<ProductDto> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Search products by name or SKU within the current tenant. Search is case-insensitive and matches partial strings."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class)
            )
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER','SUPER_ADMIN')")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @Parameter(description = "Search term for product name or SKU", required = true, example = "Laptop")
            @RequestParam String term) {
        List<ProductDto> products = productService.searchProducts(term);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    @Operation(
        summary = "Get low stock products",
        description = "Retrieve all products with stock quantity at or below minimum threshold for the current tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Low stock products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','SUPER_ADMIN')")
    public ResponseEntity<List<ProductDto>> getLowStockProducts() {
        List<ProductDto> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(
        summary = "Create product",
        description = "Create a new product for the current tenant. SKU must be unique within the tenant.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Product details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class),
                examples = @ExampleObject(
                    name = "Create Product",
                    value = "{\"name\":\"Wireless Keyboard\",\"description\":\"Ergonomic wireless keyboard\",\"sku\":\"KEY001\",\"price\":49.99,\"quantity\":30,\"minQuantity\":5,\"maxQuantity\":200,\"costPrice\":25.00,\"imageUrl\":\"https://example.com/keyboard.jpg\",\"categoryId\":1}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed or SKU already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<ProductDto> createProduct(
            @Parameter(description = "Product details", required = true)
            @Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update product",
        description = "Update an existing product for the current tenant. Cannot change SKU to an existing one.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated product details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class),
                examples = @ExampleObject(
                    name = "Update Product",
                    value = "{\"name\":\"Laptop Pro 15\\\" Updated\",\"description\":\"High-performance laptop with 15-inch display - Updated model\",\"price\":1099.99,\"quantity\":20,\"minQuantity\":5,\"maxQuantity\":100,\"costPrice\":800.00}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable String id,
            @Parameter(description = "Updated product details", required = true)
            @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete product",
        description = "Deactivate a product (soft delete). The product remains in the system but is marked as inactive."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product deactivated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock")
    @Operation(
        summary = "Update product stock",
        description = "Update product stock quantity. Can be used for inventory adjustments, stock additions, or corrections."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Stock updated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid stock quantity"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateStock(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable String id,
            @Parameter(description = "New stock quantity", required = true, example = "50")
            @RequestParam Integer quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok().build();
    }
}
