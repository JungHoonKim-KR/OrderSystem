package hello.shoppingmall.order.dto;

import hello.shoppingmall.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {
    private Long id;
    private String orderNumber;
    private String memberEmail;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private Long totalAmount;
    private List<OrderItemResponse> items;

    @Getter
    @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private int quantity;
        private Long price;
    }
} 