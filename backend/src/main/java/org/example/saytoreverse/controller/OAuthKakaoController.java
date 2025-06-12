package org.example.saytoreverse.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.saytoreverse.external.kakao.KakaoTokenService;
import org.example.saytoreverse.service.oauth.OAuthService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
* 인가 코드(code) → accessToken 요청 → 로그인 처리
* 프론트에서 카카오 로그인이 완료되면 아래와 같은 URL로 우리 서버에 code를 넘겨줌: GET /oauth/kakao/callback?code=abcdefg12345
* 그걸 받아서: 카카오 accessToken 요청 - accessToken으로 사용자 정보 조회 - 자동 회원가입 & JWT 발급 -쿠키에 토큰 저장
*/

@RestController
@RequiredArgsConstructor
public class OAuthKakaoController {

    private final KakaoTokenService kakaoTokenService;
    private final OAuthService kakaoOAuthService;

    /**
     * 카카오 로그인 콜백 URL
     * 프론트에서 인가 코드(code)를 받아서 → accessToken 요청 후 → 로그인 처리
     */

    @GetMapping("/oauth/kakao/callback")
    public String kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        // 인가코드로 accessToken 요청
        String accessToken = kakaoTokenService.requestAccessToken(code);

        // accessToken으로 사용자 정보 조희 + 자동 로그인 처리
        kakaoOAuthService.login(accessToken, response);
        return "카카오 로그인 성공";
    }
}
