package hello.shoppingmall.order.entity;

import hello.shoppingmall.global.BaseTimeEntity;
import hello.shoppingmall.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "improved_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImprovedOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    private int totalAmount;

    private int totalItems;  // 반정규화된 필드 추가

    @OneToMany(mappedBy = "improvedOrder", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Builder
    public ImprovedOrder(Member member, String orderNumber, OrderStatus status, 
                        LocalDateTime orderDate, int totalAmount, int totalItems) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.status = status;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
    }
} 