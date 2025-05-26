package hello.shoppingmall.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberSignupRequest {
    private String email;
    private String password;
    private String nickname;
} 