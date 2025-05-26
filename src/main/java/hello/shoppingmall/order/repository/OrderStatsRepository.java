package hello.shoppingmall.order.repository;

import hello.shoppingmall.order.entity.OrderStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatsRepository extends JpaRepository<OrderStats, Long> {
    Page<OrderStats> findByTotalAmountGreaterThanEqual(long minAmount, Pageable pageable);
    
    @Modifying
    @Query(value = """
        INSERT INTO order_stats (member_id, email, order_count, total_amount, avg_amount, last_order_date, updated_at)
        SELECT 
            m.id,
            m.email,
            COUNT(o.id),
            SUM(o.total_amount),
            AVG(o.total_amount),
            MAX(o.order_date),
            NOW()
        FROM orders o
        JOIN members m ON o.member_id = m.id
        GROUP BY m.id, m.email
        ON DUPLICATE KEY UPDATE
            order_count = VALUES(order_count),
            total_amount = VALUES(total_amount),
            avg_amount = VALUES(avg_amount),
            last_order_date = VALUES(last_order_date),
            updated_at = NOW()
        """, nativeQuery = true)
    void refreshOrderStats();
} 