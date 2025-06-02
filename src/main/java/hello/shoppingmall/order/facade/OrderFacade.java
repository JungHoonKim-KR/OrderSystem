package hello.shoppingmall.order.facade;

import hello.shoppingmall.global.MailService;
import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.member.service.MemberService;
import hello.shoppingmall.order.dto.OrderRequest;
import hello.shoppingmall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {
    private final OrderService orderService;
    private final MailService mailService;
    private final MemberService memberService;
    private String Email = "kjh1232100@naver.com";

    public void order(OrderRequest requests, String email){
        Member member = memberService.findMemberByEmail(email);
        orderService.order(requests);

        mailService.sendSimpleMail(Email, "주문 완료", "고객님의 주문이 성공적으로 처리됐습니다.");

    }
}
