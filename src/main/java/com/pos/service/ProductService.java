package com.pos.service;

import com.pos.dto.GetProductsResponse;
import com.pos.dto.ProductDto;
import com.pos.entity.Product;
import com.pos.entity.Tenant;
import com.pos.entity.User;
import com.pos.repository.ProductRepository;
import com.pos.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> getAllProducts() {
        return   productRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

//        return Map.of("products",products);
    }

    public ProductDto getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDto(product);
    }

    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElse( null);
        if(product != null){
            return convertToDto(product);
        }

        return null;
    }

    public List<ProductDto> getProductsByCategory(String categoryId) {
        return productRepository.findByActiveTrueAndCategoryId(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String searchTerm) {
        return productRepository.searchProducts(searchTerm).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU already exists");
        }

        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(String id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existingProduct.getSku().equals(productDto.getSku()) && 
            productRepository.existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU already exists");
        }

        updateProductFromDto(existingProduct, productDto);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void updateStock(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }

    private ProductDto convertToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .minQuantity(product.getMinQuantity())
                .maxQuantity(product.getMaxQuantity())
                .costPrice(product.getCostPrice())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product convertToEntity(ProductDto dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Tenant tenant = user.getTenant();
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .price(dto.getPrice())
                .tenant(tenant)
                .quantity(dto.getQuantity())
                .minQuantity(dto.getMinQuantity())
                .maxQuantity(dto.getMaxQuantity())
                .costPrice(dto.getCostPrice())
                .imageUrl(dto.getImageUrl())
                .category(categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found")))
                .active(true)
                .build();
    }

    private void updateProductFromDto(Product product, ProductDto dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setMinQuantity(dto.getMinQuantity());
        product.setMaxQuantity(dto.getMaxQuantity());
        product.setCostPrice(dto.getCostPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.getActive());
        
        if (!product.getCategory().getId().equals(dto.getCategoryId())) {
            product.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }
    }
}
