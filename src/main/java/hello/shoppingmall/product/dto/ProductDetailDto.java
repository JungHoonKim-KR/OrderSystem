package hello.shoppingmall.product.dto;

import hello.shoppingmall.product.entity.Product;
import lombok.Data;

import java.util.List;

@Data

public class ProductDetailDto {
    private Product product;
    private List<Product> relatedProductList;

    public ProductDetailDto(Product product, List<Product> relatedProductList) {
        this.product = product;
        this.relatedProductList = relatedProductList;
    }
}
