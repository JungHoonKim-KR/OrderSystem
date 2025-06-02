package hello.shoppingmall.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.shoppingmall.global.DbCallChecker;
import hello.shoppingmall.global.redis.LockProvider;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import hello.shoppingmall.product.respository.ProductRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductCacheService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final DbCallChecker dbCallChecker = new DbCallChecker("product");
    private final LockProvider lockProvider;
    private final ProductService productService;

    private static final String NULL_VALUE = "__NULL__";
    private static final int BASE_TTL = 1000;
    private static final int JITTER_RANGE = 500;
    private static final int MAX_RETRY_COUNT = 40;
    private static final long RETRY_DELAY_MS = 5;
    private static final long RETRY_DELAY_JITTER_MS = 15;
    private static final long LOCK_TIMEOUT_MS = 400;

    public Optional<Product> findProductByIdWithCache(Long productId) throws JsonProcessingException {
        String key = String.format("product:%d", productId);
        Optional<Product> cachedProduct = getProductFromCacheOnly(key);
        if (cachedProduct!=null) {
            return cachedProduct;
        }

        Product product = productRepository.findByIdWithPessimisticLock(productId).orElse(null);
        dbCallChecker.incrementDbSelectCount();

        long ttl = BASE_TTL + ThreadLocalRandom.current().nextLong(JITTER_RANGE);
        // DB에 없는 경우
        if (product == null) {
            redisTemplate.opsForValue().set(key, NULL_VALUE, ttl, TimeUnit.MICROSECONDS);
            return Optional.empty();
        }
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(product), ttl, TimeUnit.MICROSECONDS);
        return Optional.of(product);
    }

    public Optional<Product> findProductByIdWithCacheLock(Long productId) throws JsonProcessingException, InterruptedException {
        String key = String.format("product:%d", productId);
        String lockKey = lockProvider.makeLockKey("product",productId);

        Optional<Product> cachedResult = getProductFromCacheOnly(key);

        // cachedResult.isEmpty() : 데이터가 아예 없음
        // cachedResult : 값이 있음
        if(cachedResult!=null){
            return cachedResult;
        }

        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {

            boolean tryLock = lockProvider.tryLock(lockKey, LOCK_TIMEOUT_MS);
            // 락 흭득 실패 시 재시도
            if (!tryLock) {
                log.warn(">> 경쟁 발생 !! - key={}, retry={}", lockKey, retry);
                long jitter = ThreadLocalRandom.current().nextLong(RETRY_DELAY_JITTER_MS);
                sleep(RETRY_DELAY_MS + jitter);
                continue;
            }
            try {
                cachedResult = getProductFromCacheOnly(key);

                if(cachedResult != null){
                    return cachedResult;
                }
                meterRegistry.counter("cache.miss", "service", "product").increment();

                Product product = productRepository.findByIdWithPessimisticLock(productId).orElse(null);
                dbCallChecker.incrementDbSelectCount();

                // DB에 없는 경우
                if (product == null) {
                    redisTemplate.opsForValue().set(key, NULL_VALUE);
                    return Optional.empty();
                }
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(product));
                return Optional.of(product);
            }finally {
                lockProvider.unlock(lockKey);
            }
        }

        // MAX_RETRY_COUNT 회 모두 락 획득에 실패하면 예외 처리
        throw new IllegalStateException("Failed to acquire lock after " + MAX_RETRY_COUNT + " - key=" + lockKey);
    }

    public List<Product> findRelatedProductsWithCache(Long productId, ProductCategory category) throws JsonProcessingException {
        String key = String.format("related_product-%s : %d", category.toString(), productId);
        List<Product> cachedResult = getRelatedProductsListFromCacheOnly(key);

       if(cachedResult !=null){
           return cachedResult;
       }

        List<Product> relatedProducts = productService.findRelatedProducts(productId, category);
        redisTemplate.opsForValue().set(key,objectMapper.writeValueAsString(relatedProducts));
        return relatedProducts;
    }

    private Optional<Product> getProductFromCacheOnly(String key) throws JsonProcessingException {
        String cachedResult = redisTemplate.opsForValue().get(key);

        // DB에 데이터가 없음
        if (cachedResult != null && cachedResult.equals(NULL_VALUE)) {
            meterRegistry.counter("cache.hit", "service", "product").increment();
            return Optional.empty(); // 부정 캐싱: 상품 없음
        }

        if (cachedResult != null) {
            meterRegistry.counter("cache.hit", "service", "product").increment();
            return Optional.of(objectMapper.readValue(cachedResult, Product.class));
        }
        meterRegistry.counter("cache.miss", "service", "product").increment();
        return null;
    }
    private List<Product> getRelatedProductsListFromCacheOnly(String key) throws JsonProcessingException {
        String cachedJson = redisTemplate.opsForValue().get(key);
        if (cachedJson == null) {
            meterRegistry.counter("related_products.cache.miss").increment();
            return null;
        }
        if (NULL_VALUE.equals(cachedJson)) {
            meterRegistry.counter("related_products.cache.hit.null").increment();
            return Collections.emptyList();
        }
        meterRegistry.counter("related_products.cache.hit").increment();
        return objectMapper.readValue(cachedJson, new TypeReference<List<Product>>() {});
    }

    private void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public void logDbCall() {
        dbCallChecker.logDbCall();
    }

    public void reset() {
        dbCallChecker.reset();
    }
}
