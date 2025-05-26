package hello.shoppingmall.member.dto;

import hello.shoppingmall.member.entity.Member;
import lombok.Data;

@Data
public class MemberResponse {
    private Long id;
    private String email;
    private String nickname;
    private String role;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.role = member.getRole().name();
    }
} 