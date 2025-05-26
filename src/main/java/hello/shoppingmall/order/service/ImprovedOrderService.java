package hello.shoppingmall.order.service;

import hello.shoppingmall.order.dto.ImprovedOrderDetailResponse;
import hello.shoppingmall.order.dto.ImprovedOrderResponse;
import hello.shoppingmall.order.dto.OrderStatisticsResponse;
import hello.shoppingmall.order.entity.ImprovedOrder;
import hello.shoppingmall.order.entity.OrderStats;
import hello.shoppingmall.order.entity.OrderStatus;
import hello.shoppingmall.order.repository.ImprovedOrderRepository;
import hello.shoppingmall.order.repository.OrderStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImprovedOrderService {

    private final ImprovedOrderRepository improvedOrderRepository;
    private final OrderStatsRepository orderStatsRepository;

    @Transactional(readOnly = true)
    public Page<OrderStatisticsResponse> getOrderStatistics(Long minAmount, Pageable pageable) {
        Page<OrderStats> statsPage = orderStatsRepository.findByTotalAmountGreaterThanEqual(
                minAmount != null ? minAmount : 0L,
                pageable
        );

        return statsPage.map(this::toOrderStatisticsResponse);
    }

    // 집계 정산 데이터를 요구하는 즉시 계산해서 바로 보여주는 것은 성능이 저하됨
    // 그래서 원하는 데이터를 OrderStats라는 객체로 따로 만들어서 특정 시간마다 미리 집계함
    // 그럼 요구할 때 바로 꺼내 쓸 수 있음
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

    private OrderStatisticsResponse toOrderStatisticsResponse(OrderStats stats) {
        return OrderStatisticsResponse.builder()
                .memberEmail(stats.getEmail())
                .totalOrders((long) stats.getOrderCount())
                .totalAmount(stats.getTotalAmount())
                .averageAmount(stats.getAvgAmount())
                .build();
    }

    public Page<ImprovedOrderResponse> searchOrders(
            LocalDateTime startDate,
            OrderStatus status,
            int minAmount,
            Pageable pageable) {
        return improvedOrderRepository.findOrdersByComplexCondition(startDate, status, minAmount, pageable)
                .map(this::toOrderResponse);
    }

    private ImprovedOrderResponse toOrderResponse(ImprovedOrder order) {
        return ImprovedOrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberEmail(order.getMember().getEmail())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .build();
    }

    public ImprovedOrderDetailResponse findByOrderNumber(String orderNumber) {
        ImprovedOrder order = improvedOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toOrderDetailResponse(order);
    }

    private ImprovedOrderDetailResponse toOrderDetailResponse(ImprovedOrder order) {
        return ImprovedOrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberEmail(order.getMember().getEmail())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .build();
    }
} 