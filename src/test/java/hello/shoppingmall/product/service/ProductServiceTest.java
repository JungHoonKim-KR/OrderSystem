package hello.shoppingmall.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hello.shoppingmall.config.IntegrationTest;
import hello.shoppingmall.product.dto.ProductDetailDto;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import hello.shoppingmall.product.facade.CachedProductAndRelatedProductsFacade;
import hello.shoppingmall.product.fixture.ProductFixture;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@IntegrationTest
@Slf4j
public class ProductServiceTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private CachedProductAndRelatedProductsFacade cachedProductAndRelatedProducts;
    @Autowired
    private ProductCacheService productCacheService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MeterRegistry meterRegistry;


    private static final String INSERT_PRODUCT_QUERY = """
            INSERT INTO products(name, price, stock_quantity, category)
            VALUES (?, ?, ?, ?)
            """;

    private final int count = 100_000;
    private List<Long> ids = LongStream.rangeClosed(1, count)
            .boxed()
            .toList();
    public static final int LOOP_COUNT = 300;
    public static final int REPEATED_COUNT = 20;
    public static final int WARM_UP_COUNT = 10;

    @BeforeEach
    void setUp() {
        List<Product> products = ProductFixture.createProducts(count);
//        productRepository.deleteAllInBatch();

        jdbcTemplate.batchUpdate(INSERT_PRODUCT_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product product = products.get(i);
                ps.setString(1, product.getName());
                ps.setInt(2, product.getPrice());
                ps.setInt(3, product.getStockQuantity());
                ps.setString(4, String.valueOf(product.getCategory()));
            }

            @Override
            public int getBatchSize() {
                return products.size();
            }
        });
//        List<String> keys = ids.stream().map(String::valueOf).toList();
//        redisTemplate.delete(keys);
    }


    @DisplayName("Jitter")
    @Test
    void testJitter() throws InterruptedException {
        ExecutorService executors = Executors.newFixedThreadPool(300);
        CountDownLatch latch = new CountDownLatch(ids.toArray().length);

        for (long id : ids) {
            executors.submit(() -> {
                try {
                    productCacheService.findProductByIdWithCache(id);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        productCacheService.logDbCall();
        productCacheService.reset();

    }

    @DisplayName("Hot Key")
    @Test
    void testHotKeyWithLock() throws InterruptedException {
        ExecutorService executors = Executors.newFixedThreadPool(300);
        CountDownLatch readyLatch = new CountDownLatch(LOOP_COUNT);
        CountDownLatch fireLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(LOOP_COUNT);

        for (int i = 0; i < LOOP_COUNT; i++) {
            executors.submit(() -> {
                try {
                    readyLatch.countDown();
                    fireLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    productCacheService.findProductByIdWithCacheLock(1L);
                }catch (JsonProcessingException | InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        // 모든 스레드 준비 후 동시에 발사
        readyLatch.await();
        fireLatch.countDown();

        doneLatch.await();

        productCacheService.logDbCall();
        productCacheService.reset();
    }

    @DisplayName("연관 상품 조회 테스트")
    @Test
    void getProductDetailWhenNoRelatedProductsIntegration() throws JsonProcessingException, InterruptedException {
        // Given
        Long productId = 89999L;
        ProductCategory category = ProductCategory.BOOKS;

        // When
        ProductDetailDto productDetail = cachedProductAndRelatedProducts.getProductDetail(productId, category);

        // Then
        assertThat(productDetail.getProduct().getId()).isEqualTo(productId);
        assertThat(productDetail.getProduct().getCategory()).isEqualTo(category);

        assertThat(productDetail.getRelatedProductList()).isNotNull();
        assertThat(productDetail.getRelatedProductList()).isNotEmpty(); // 연관 상품 리스트가 비어있는지 확인
        assertThat(productDetail.getRelatedProductList().size()).isEqualTo(3); // 크기가 0인지 확인

        // 선택 사항: Redis에 NULL_LIST_VALUE가 저장되었는지 확인
        String key=String.format("related_product-%s : %d", category.toString(), productId);
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("__NULL_LIST__");
    }




    protected void measureAndRecordTime(String operationType, int currentCount, Runnable operation) {
        // given
        Timer timer = meterRegistry.timer(operationType);

        // when
        if (currentCount <= WARM_UP_COUNT) {
            operation.run();
        } else {
            timer.record(operation);
        }

        // then
        if (currentCount == REPEATED_COUNT) {
            double mean = timer.mean(TimeUnit.MILLISECONDS);
            log.info(">>> {} - 평균 소요 시간 (warm-up 제외)={}ms", operationType, String.format("%.2f", mean));
        }
    }

    private void printCacheStats(String serviceName) {
        double hit = meterRegistry.counter("cache.hit", "service", serviceName).count();
        double miss = meterRegistry.counter("cache.miss", "service", serviceName).count();
        double total = hit + miss;
        double hitRate = calculateHitRatio(hit, total);

        double dbSelect = meterRegistry.counter("db.select", "service", serviceName).count();

        log.info(">>> 캐시 매트릭[{}] 캐시 히트: {}, 미스: {}, 전체 요청: {}, 히트율: {}%", serviceName, (int) hit, (int) miss, (int) total, String.format("%.2f", hitRate));
        log.info(">>> DB 매트릭[{}] 조회 요청: {}", serviceName, dbSelect);
    }

    private static double calculateHitRatio(double hit, double total) {
        if (total == 0) {
            return 0;
        }
        return (hit / total) * 100;
    }
}
