package com.pos.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetProductsResponse {
    List<ProductDto> products;
}
