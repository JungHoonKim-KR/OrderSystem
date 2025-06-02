package hello.shoppingmall.product.fixture;

import hello.shoppingmall.product.dto.ProductRequest;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class ProductFixture {

    public static Product createProduct(String name, int price, int quantity, ProductCategory category){
        return Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(quantity)
                .category(category)
                .build();
    }

    public static ProductRequest createProductRequest(Long productId){
        // 1~3 랜덤
        int quantity = new Random().nextInt(3)+1;
        log.info("주문 생성");
        log.info("상품 번호:{} 상품 수량:{}", productId, quantity);
        return new ProductRequest(productId, quantity);
    }

    public static List<Product> createProducts(int count){
        // 1~3 랜덤
        List<Product> products = new ArrayList<>();
        Random random = new Random();
        for(int i =0; i<count; i++){
            String randomName = generateRandomString(random.nextInt(10));
            products.add(Product.builder()
                    .name(randomName)
                    .price(random.nextInt())
                    .stockQuantity(200)
                    .category(ProductCategory.BOOKS)
                    .build());

        }
        return products;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
