package hello.shoppingmall.order.dto;

import hello.shoppingmall.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ImprovedOrderDetailResponse {
    private Long id;
    private String orderNumber;
    private String memberEmail;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private long totalAmount;
    private int totalItems;
    private List<OrderDetailResponse.OrderItemResponse> orderItems;
} 