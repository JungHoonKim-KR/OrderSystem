package hello.shoppingmall.event.repository;

import hello.shoppingmall.event.entity.EventWithMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventWithLockParticipantRepository extends JpaRepository<EventWithMember, Long> {
    // 특정 시간 이전에 생성된 외부 ID 미할당 참가자 조회
    List<EventWithMember> findByExternalIdIsNullAndCreatedAtBefore(
            LocalDateTime dateTime
    );
} 