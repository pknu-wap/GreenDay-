package practice.login.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import practice.login.domain.Member;
import practice.login.domain.dto.JoinRequest;
import practice.login.domain.dto.LoginRequest;
import practice.login.service.MemberService;

import java.util.Collection;
import java.util.Iterator;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class OauthLoginController {

    private final MemberService memberService;

    // 로그인 페이지를 반환하는 핸들러 메서드
    @GetMapping(value = {"", "/"})
    public String loginPage(Model model) {
        // 모델에 소셜 로그인을 나타내는 속성들을 추가함
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");
        // 로그인 요청 객체를 생성하여 모델에 추가함
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // 소셜 로그인 성공 핸들러 메서드
    @PostMapping("/oauth/success")
    public ResponseEntity<String> oauthLoginSuccess() {
        // 성공 메시지를 생성함
        String message = "네이버 소셜로그인이 성공하였습니다.";
        return ResponseEntity.ok(message); // 성공 메시지를 반환함
    /*@PostMapping("/login")
        public ResponseEntity<String> login(@Valid @ModelAttribute LoginRequest loginRequest, BindingResult bindingResult) {
            // 만약 바인딩 에러가 있다면 에러 메시지 반환
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body("로그인 정보가 올바르지 않습니다.");
            }

            // 로그인 처리 로직 작성 (예: 사용자 인증)

            // 로그인이 성공했을 경우 성공 메시지 반환
            return ResponseEntity.ok("로그인이 성공했습니다.");
            return ResponseEntity.ok(message); // 성공 메시지를 반환합니다.
        }*/
    }
}