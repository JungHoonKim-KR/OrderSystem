package hello.shoppingmall.event.service;

import hello.shoppingmall.event.entity.Event;
import hello.shoppingmall.event.entity.EventWithMember;
import hello.shoppingmall.event.repository.EventWithLockRepository;
import hello.shoppingmall.event.repository.EventWithLockParticipantRepository;
import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventWithLockService {
    private final EventWithLockRepository eventRepository;
    private final EventWithLockParticipantRepository participantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinEventOptimistic(Long eventId, Long memberId) {
        Event event = eventRepository.findByIdWithOptimisticLock(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventRepository.saveAndFlush(event);

        EventWithMember participant = EventWithMember.builder()
                .event(event)
                .member(member)
                .build();
        participantRepository.save(participant);
    }

    @Transactional
    public void joinEventPessimistic(Long eventId, Long memberId) {
        Event event = eventRepository.findByIdWithPessimisticLock(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();

        EventWithMember participant = EventWithMember.builder()
                .event(event)
                .member(member)
                .build();

        participantRepository.save(participant);
    }

    // TODO: Named Lock 을 위해 아래 주석을 해제해야함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void joinEventWithNamedLock(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventRepository.saveAndFlush(event);

        EventWithMember participant = EventWithMember.builder()
                .event(event)
                .member(member)
                .build();
        participantRepository.save(participant);
    }
} 