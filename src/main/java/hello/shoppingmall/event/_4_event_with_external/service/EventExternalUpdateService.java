package hello.shoppingmall.event._4_event_with_external.service;

import hello.shoppingmall.event._3_event_with_lock.entity.EventWithLock;
import hello.shoppingmall.event._3_event_with_lock.entity.EventWithLockParticipant;
import hello.shoppingmall.event._3_event_with_lock.repository.EventWithLockRepository;
import hello.shoppingmall.event._4_event_with_external.repository.EventWithLockParticipantRepository;
import hello.shoppingmall.global.error.exception.EntityNotFoundException;
import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventExternalUpdateService {
    private final EventWithLockRepository eventRepository;
    private final EventWithLockParticipantRepository participantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public EventWithLockParticipant joinEventWithTransaction(Long eventId, Long memberId) {
        // 1. 이벤트와 회원 정보 조회
        EventWithLock event = eventRepository.findByIdWithOptimisticLock(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 2. 이벤트 참가자 수 증가 (내부적으로 참가 가능 여부 검증)
        event.increaseParticipants();
        eventRepository.save(event);

        // 3. 참가자 정보 저장
        EventWithLockParticipant participant = EventWithLockParticipant.builder()
                .event(event)
                .member(member)
                .build();

        return participantRepository.save(participant);
    }

    @Transactional
    public void updateExternalId(EventWithLockParticipant participant, String externalId) {
        participant.updateExternalId(externalId);
        participantRepository.save(participant);
    }
} 