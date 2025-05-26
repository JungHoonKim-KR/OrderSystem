package hello.shoppingmall.order.repository.dto;

public interface OrderStatisticsProjection {
    String getMemberEmail();
    Long getTotalOrders();
    Long getTotalAmount();
    Double getAverageAmount();
}
