package hello.shoppingmall.product.dto;

import hello.shoppingmall.product.entity.ProductCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private ProductCategory category;
} 