package hello.shoppingmall.order.repository;

import hello.shoppingmall.order.entity.ImprovedOrder;
import hello.shoppingmall.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ImprovedOrderRepository extends JpaRepository<ImprovedOrder, Long> {
    
    @Query(value = """
            SELECT o FROM ImprovedOrder o
            WHERE o.orderDate >= :startDate 
            AND o.status = :status 
            AND o.totalAmount >= :minAmount 
            ORDER BY o.orderDate DESC
            """)
    @EntityGraph(attributePaths = "member")  // JOIN FETCH 대신 EntityGraph 사용
    Page<ImprovedOrder> findOrdersByComplexCondition(
            @Param("startDate") LocalDateTime startDate,
            @Param("status") OrderStatus status,
            @Param("minAmount") int minAmount,
            Pageable pageable);

    Optional<ImprovedOrder> findByOrderNumber(String orderNumber);
} 