package hello.shoppingmall.member.controller;

import hello.shoppingmall.member.dto.MemberLoginRequest;
import hello.shoppingmall.member.dto.MemberResponse;
import hello.shoppingmall.member.dto.MemberSignupRequest;
import hello.shoppingmall.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chapter2/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody MemberSignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody MemberLoginRequest request) {
        String token = memberService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentMember(@AuthenticationPrincipal User user) {
        String email = user.getUsername();
        log.info("Current user email from UserDetails: {}", email);
        
        MemberResponse memberResponse = memberService.getCurrentMember(email);
        return ResponseEntity.ok(memberResponse);
    }
} 