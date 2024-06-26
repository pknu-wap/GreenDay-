package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import com.example.domain.entity.Member;
import com.example.service.MemberService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class OauthLoginController {

    private final MemberService memberService;

    public OauthLoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    // JWT 구성 속성
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @PostMapping("/user-info")
    public ResponseEntity<UserInfoResponse> getUserInfo(@RequestBody TokenRequest tokenRequest) {
        System.out.println("Received token request: " + tokenRequest); // 로그 추가
        Map<String, String> tokens = getAccessToken(tokenRequest);

        String accessToken = tokens.get("access_token");
        String refreshToken = tokens.get("refresh_token");
        String tokenType = tokens.get("token_type");
        String expiresIn = tokens.get("expires_in");

        if (accessToken == null || refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        String naverEmail = memberService.getNaverEmail(accessToken);
        String memberName = memberService.getName(accessToken);
        String provider = "naver";
        String providerId = tokenRequest.getToken(); // 필요 시 적절한 값으로 변경

        // 액세스 토큰을 가져옴
        System.out.println("Email: " + naverEmail); // 이메일 출력
        System.out.println("Name: " + memberName); // 회원 이름 출력

        // 이미 존재하는 회원인지 확인
        Member existingMember = memberService.findByEmail(naverEmail);
        if (existingMember != null) {
            UserInfoResponse userInfoResponse = new UserInfoResponse(existingMember.getEmail(), existingMember.getName(), accessToken, refreshToken, generateJwtToken(existingMember));
            return ResponseEntity.ok(userInfoResponse);
        }

        // 존재하지 않는 회원인 경우 새로운 회원으로 등록
        Member savedMember = memberService.saveMember(naverEmail, naverEmail, memberName, provider, providerId);

        // JWT 토큰 생성
        String jwtToken = generateJwtToken(savedMember);

        // JWT 토큰 저장 및 반환
        UserInfoResponse userInfoResponse = new UserInfoResponse(savedMember.getEmail(), savedMember.getName(), jwtToken, accessToken, refreshToken);
        return ResponseEntity.ok(userInfoResponse);
    }

    private String generateJwtToken(Member member) {
        return Jwts.builder()
                .setSubject((member.getEmail()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                // 필요 시 추가 클레임 추가
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private Map<String, String> getAccessToken(TokenRequest tokenRequest) {
        String clientId = "o72MtePRXsbwlztUtJoj";
        String clientSecret = "syAjjCYexm";
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders httpHeaders = new HttpHeaders();
        MultiValueMap<String,String> body= new LinkedMultiValueMap<>();
        body.add("grant_type","authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret",clientSecret);
        body.add("code",tokenRequest.getCode());
        body.add("state",tokenRequest.getState());
        HttpEntity<?> request = new HttpEntity<>(body,httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> response = restTemplate.postForObject(tokenUrl, request, Map.class);

        return response;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<UserInfoResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String clientId = "o72MtePRXsbwlztUtJoj";
        String clientSecret = "syAjjCYexm";
        String refreshToken = refreshTokenRequest.getRefreshToken();

        //리프레시 토큰을 사용하여 새로운 엑세스 토큰을 받아오는 로직
        //네이버의 OAuth 2.0 리프레시 토큰 엔드포인트를 호출하여 새로운 엑세스 토큰을 받아오는 코드로 채워짐
        String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=refresh_token" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&refresh_token=" + refreshToken;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> response = restTemplate.postForObject(tokenUrl, null, Map.class);

        String newAccessToken = response.get("access_token");
        String newRefreshToken = response.get("refresh_token");

        // 이 부분에서 Member 객체를 가져와야 함
        Member member = memberService.findByEmail(refreshTokenRequest.getEmail());

        UserInfoResponse userInfoResponse = new UserInfoResponse(refreshTokenRequest.getEmail(), refreshTokenRequest.getName(), newAccessToken, newRefreshToken, generateJwtToken(member));
        return ResponseEntity.ok(userInfoResponse);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class UserInfoResponse {
        private String email;
        private String name;
        private String accessToken;
        private String refreshToken;
        private String jwtToken; // JWT 토큰 추가
    }

    @Data
    private static class TokenRequest {
        private String code;
        private String state;
        // getToken() 메서드 추가
        public String getToken() {
            return this.code;
        }
    }

    @Data
    private static class RefreshTokenRequest {
        private String email;
        private String name;
        private String refreshToken;
    }
}
