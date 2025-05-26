package hello.shoppingmall.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_stats")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStats {
    @Id
    private Long memberId;
    
    private String email;
    private int orderCount;
    private long totalAmount;
    private double avgAmount;
    private LocalDateTime lastOrderDate;
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 