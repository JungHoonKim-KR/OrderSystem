package hello.shoppingmall.event.facade;

import hello.shoppingmall.event.entity.EventWithLock;
import hello.shoppingmall.event.repository.EventWithLockRepository;
import hello.shoppingmall.event.external.ExternalEventApi;
import hello.shoppingmall.event.external.KakaoTalkMessageApi;
import hello.shoppingmall.event.external.model.ExternalEventResponse;
import hello.shoppingmall.event.service.EventExternalUpdateService;
import hello.shoppingmall.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImprovedEventJoinWithExternalApiFacade {
    private static final String TEST_PHONE_NUMBER = "01012341234";

    private final EventExternalUpdateService eventExternalUpdateService;
    private final ExternalEventApi externalEventApi;
    private final KakaoTalkMessageApi kakaoTalkMessageApi;
    private final EventWithLockRepository eventRepository;

    public void joinEvent(Long eventId, Long memberId) {
        // 0. 이벤트 정보 조회 (읽기 전용)
        EventWithLock event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        // 1. DB 트랜잭션 먼저 처리
        eventExternalUpdateService.joinEventWithTransaction(eventId, memberId);

        // 2. DB 트랜잭션 성공 후 외부 API 호출
        ExternalEventResponse response = externalEventApi.registerParticipant(
                eventId, memberId, event.getName()
        );

        if (!response.isSuccess()) {
            // 외부 API 실패 시 보상 트랜잭션 또는 알림 처리 필요
            log.error("외부 API 호출 실패. 이벤트: {}, 회원: {}", eventId, memberId);
        }

        // 3. 카카오톡 알림 발송 (선택적)
        try {
            kakaoTalkMessageApi.sendEventJoinMessage(TEST_PHONE_NUMBER, event.getName());
        } catch (Exception e) {
            log.error("알림 발송 실패", e);
            // 알림 실패는 핵심 비즈니스 로직에 영향을 주지 않음
        }
    }
} 