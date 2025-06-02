package hello.shoppingmall.product.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import hello.shoppingmall.global.error.CustomException;
import hello.shoppingmall.global.error.ErrorCode;
import hello.shoppingmall.product.dto.ProductDetailDto;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import hello.shoppingmall.product.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedProductAndRelatedProductsFacade {
    private final ProductCacheService productCacheService;

    @Transactional
    public ProductDetailDto getProductDetail(Long productId, ProductCategory category) throws JsonProcessingException, InterruptedException {

        if (productId == null || productId <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "상품 ID는 필수이며 양수여야 합니다.");
        }
        if (category == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카테고리는 필수입니다.");
        }

        Optional<Product> productByIdWithCacheLock = productCacheService.findProductByIdWithCacheLock(productId);
        if(productByIdWithCacheLock.isEmpty()){
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "없는 상품입니다.");
        }
        List<Product> relatedProductsWithCache = productCacheService.findRelatedProductsWithCache(productId, category);

        return new ProductDetailDto(productByIdWithCacheLock.get(), relatedProductsWithCache);
    }
}
