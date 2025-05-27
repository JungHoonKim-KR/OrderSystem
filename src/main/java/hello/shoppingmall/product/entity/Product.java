package hello.shoppingmall.product.entity;

import hello.shoppingmall.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
    
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Builder
    public Product(String name, int price, int stockQuantity, ProductCategory category) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public void subtractStock(int stockQuantity){
        if(this.stockQuantity <stockQuantity)
            throw new RuntimeException("재고가 없습니다.");
        this.stockQuantity -= stockQuantity;
    }
} 