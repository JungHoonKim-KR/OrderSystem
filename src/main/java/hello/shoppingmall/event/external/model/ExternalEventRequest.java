package hello.shoppingmall.event.external.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExternalEventRequest {
    private Long eventId;
    private Long memberId;
    private String eventName;
} 