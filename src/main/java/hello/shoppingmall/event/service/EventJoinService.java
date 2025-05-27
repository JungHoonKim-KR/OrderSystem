package hello.shoppingmall.event.service;

import hello.shoppingmall.event.entity.Event;
import hello.shoppingmall.event.repository.EventRepository;
import hello.shoppingmall.event.entity.EventParticipant;
import hello.shoppingmall.event.repository.EventParticipantRepository;
import hello.shoppingmall.global.error.exception.EntityNotFoundException;
import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventJoinService {
    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinEvent(Long eventId, Long memberId) {
        // 1. 이벤트와 회원 정보 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 2. 이벤트 참가자 수 증가 (내부적으로 참가 가능 여부 검증)
        event.increaseParticipants();
        eventRepository.saveAndFlush(event);

        // 3. 참가자 정보 저장
        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .member(member)
                .build();

        participantRepository.save(participant);
    }
} 