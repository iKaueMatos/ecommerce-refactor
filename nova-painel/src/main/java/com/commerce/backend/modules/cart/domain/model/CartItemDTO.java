package com.commerce.backend.modules.cart.domain.model;

import com.commerce.backend.modules.product.domain.model.ColorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private String url;
    private String name;
    private Float price;
    private Integer amount;
    private String thumb;
    private Integer stock;
    private ColorDTO color;
}
