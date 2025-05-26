package hello.shoppingmall.event._1_event.service;

import hello.shoppingmall.event._1_event.entity.Event;
import hello.shoppingmall.event._1_event.repository.EventRepository;
import hello.shoppingmall.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service

@RequiredArgsConstructor
@Slf4j
public class SingleEventService {
    private final EventRepository eventRepository;


    public void increaseParticipants(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));
        event.increaseParticipants();
        eventRepository.save(event);
    }
} 