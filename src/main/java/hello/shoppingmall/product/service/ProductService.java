package hello.shoppingmall.product.service;


import hello.shoppingmall.global.error.exception.EntityNotFoundException;
import hello.shoppingmall.product.dto.ProductResponse;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import hello.shoppingmall.product.respository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product findProductBydId(Long productId){
        return productRepository.findByIdWithPessimisticLock(productId).orElseThrow(()->new EntityNotFoundException("상품을 찾을 수 없습니다."));
    }

    public Page<ProductResponse> findProductsByCategory(ProductCategory category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(this::toProductResponse);
    }

    @Transactional
    public Product save(Product product){
        return productRepository.save(product);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .build();
    }
} 