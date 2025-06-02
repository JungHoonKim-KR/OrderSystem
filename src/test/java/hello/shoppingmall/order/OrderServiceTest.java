package hello.shoppingmall.order;

import hello.shoppingmall.config.IntegrationTest;
import hello.shoppingmall.global.MailService;
import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.member.entity.Role;
import hello.shoppingmall.member.repository.MemberRepository;
import hello.shoppingmall.order.dto.OrderRequest;
import hello.shoppingmall.order.service.OrderService;
import hello.shoppingmall.product.dto.ProductRequest;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import hello.shoppingmall.product.fixture.ProductFixture;
import hello.shoppingmall.product.respository.ProductRepository;
import hello.shoppingmall.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@IntegrationTest
public class OrderServiceTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MailService mailService;

    private List<Product> testProductList;
    private List<Member> testMemberList;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        testProductList = new ArrayList<>();
        testMemberList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testProductList.add(productService.save(ProductFixture.createProduct("test" + i, 5000 + i * 1000, 20, ProductCategory.BOOKS)));
        }

        for (int i = 0; i < 500; i++) {
            testMemberList.add(memberRepository.save(Member.builder().nickname("testMan").email(UUID.randomUUID().toString()).password("test").role(Role.USER).build()));
        }
    }


    @Test
    @DisplayName("요청자가 많을 때 비관적 락 테스트")
    void Test() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(testMemberList.size());


        // 상품은 1~3개 랜덤
        List<Member> memberList = memberRepository.findAll();

        for (Member member : memberList) {
            executorService.submit(() -> {
                try {
                    orderService.order(new OrderRequest(member,
                            List.of(ProductFixture.createProductRequest(1L),
                                    ProductFixture.createProductRequest(2L))));
                } catch (Exception e) {
                    log.error("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        long executionTIme = System.currentTimeMillis() - startTime;
        Product p1 = productRepository.findById(1L)
                .orElseThrow();
        Product p2 = productRepository.findById(2L)
                .orElseThrow();
        log.info("===테스트 결과===");
        log.info("실행 시간 {}ms", executionTIme);
        log.info("1번 상품 재고: " + p1.getStockQuantity());
        log.info("2번 상품 재고: " + p2.getStockQuantity());
    }

    @Test
    @DisplayName("주문 완료 후 이메일 발송 테스트")
    void sendEmailTest() {
        Member testMember = Member.builder()
                .nickname("test")
                .email("kjh1232100@naver.com")
                .password("test")
                .role(Role.USER)
                .build();
        memberRepository.save(testMember);
        orderService.order(new OrderRequest(testMember, List.of(
                ProductFixture.createProductRequest(1L),
                ProductFixture.createProductRequest(2L)
        )));

        mailService.sendSimpleMail(testMember.getEmail(), "주문 완료", String.format("상품 %s의 주문이 완료됐습니다.", 1L));

    }

}
