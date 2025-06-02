package hello.shoppingmall.order.service;

import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.member.service.MemberService;
import hello.shoppingmall.order.dto.OrderDetailResponse;
import hello.shoppingmall.order.dto.OrderRequest;
import hello.shoppingmall.order.dto.OrderResponse;
import hello.shoppingmall.order.dto.OrderStatisticsResponse;
import hello.shoppingmall.order.entity.Order;
import hello.shoppingmall.order.entity.OrderItem;
import hello.shoppingmall.order.entity.OrderStats;
import hello.shoppingmall.order.entity.OrderStatus;
import hello.shoppingmall.order.repository.OrderRepository;
import hello.shoppingmall.order.repository.OrderStatsRepository;
import hello.shoppingmall.product.dto.ProductRequest;
import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatsRepository orderStatsRepository;
    private final ProductService productService;
    public Page<OrderResponse> findOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toOrderResponse);
    }

    public OrderDetailResponse findOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toOrderDetailResponse(order);
    }

    public Page<OrderResponse> findOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::toOrderResponse);
    }

    public Page<OrderResponse> findOrdersByPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.findByOrderDateBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), pageable)
                .map(this::toOrderResponse);
    }

    public Page<OrderResponse> searchOrders(
            LocalDateTime startDate,
            OrderStatus status,
            int minAmount,
            Pageable pageable) {
        return orderRepository.findOrdersByComplexCondition(startDate, status, minAmount, pageable)
                .map(this::toOrderResponse);
    }

    public OrderDetailResponse findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toOrderDetailResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberEmail(order.getMember().getEmail())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount((long) order.getTotalAmount())
                .totalItems(order.getOrderItems().size())
                .build();
    }

    @Scheduled(cron = "0 */3 * * * *")
    @Transactional
    public void refreshOrderStatistics() {
        log.info("Starting order statistics refresh at {}", LocalDateTime.now());
        try {
            // DB에서 직접 통계 데이터 집계 및 저장
            orderStatsRepository.refreshOrderStats();
            log.info("Order statistics refreshed successfully");
        } catch (Exception e) {
            log.error("Failed to refresh order statistics", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderStatisticsResponse> getOrderStatistics(Long minAmount, Pageable pageable) {
        Page<OrderStats> statsPage = orderStatsRepository.findByTotalAmountGreaterThanEqual(
                minAmount != null ? minAmount : 0L,
                pageable
        );

        return statsPage.map(this::toOrderStatisticsResponse);
    }
    @Transactional
    public void order(OrderRequest orderRequest){
        List<OrderItem> orderItemList = new ArrayList<>();
        int totalQuantity=0;
        int totalAmount =0;

        for(ProductRequest productRequest: orderRequest.getProductList()){
            Product product = productService.findProductBydId(productRequest.getProductId());

            // 수량 체크
            product.subtractStock(productRequest.getQuantity());

            orderItemList.add(OrderItem.builder()
                    .product(product)
                    .quantity(productRequest.getQuantity())
                    .price(product.getPrice())
                    .build());
            totalQuantity += productRequest.getQuantity();
            totalAmount += productRequest.getQuantity() * product.getPrice();
        }

        Order order = Order.builder()
                .member(orderRequest.getMember())
                .orderNumber(UUID.randomUUID().toString())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PROCESSING)
                .totalAmount(totalAmount)
                .totalItems(totalQuantity)
                .build();

        // order와 item의 연관관계를 위해
        for (OrderItem item : orderItemList){
            item.addOrder(order);
        }
        orderRepository.save(order);


    }


    private OrderStatisticsResponse toOrderStatisticsResponse(OrderStats stats) {
        return OrderStatisticsResponse.builder()
                .memberEmail(stats.getEmail())
                .totalOrders((long) stats.getOrderCount())
                .totalAmount(stats.getTotalAmount())
                .averageAmount(stats.getAvgAmount())
                .build();
    }
    private OrderDetailResponse toOrderDetailResponse(Order order) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberEmail(order.getMember().getEmail())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount((long) order.getTotalAmount())
                .items(order.getOrderItems().stream()
                        .map(item -> OrderDetailResponse.OrderItemResponse.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price((long) item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
} 