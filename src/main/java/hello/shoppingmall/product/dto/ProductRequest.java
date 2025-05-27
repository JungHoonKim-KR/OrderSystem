package hello.shoppingmall.product.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private Long productId;
    private int quantity;

    public ProductRequest(long productId, int quantity){
        this.productId = productId;
        this.quantity = quantity;
    }
}
