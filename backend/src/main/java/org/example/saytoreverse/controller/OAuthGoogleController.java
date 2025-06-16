package org.example.saytoreverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.saytoreverse.external.Google.GoogleTokenService;
import org.example.saytoreverse.service.oauth.OAuthService;
import org.example.saytoreverse.service.oauth.OAuthServiceImplGoogle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OAuthGoogleController {

    private final OAuthServiceImplGoogle oauthServiceImplGoogle;
    private final GoogleTokenService googleTokenService;
    @Qualifier("OAuthServiceImplGoogle")
    private final OAuthService googleOAuthService;
    @Value("${GOOGLE_AUTH_URL}")
    private String GOOGLE_AUTH_URL;

    public OAuthGoogleController(
            OAuthServiceImplGoogle oauthServiceImplGoogle, GoogleTokenService googleTokenService,
            @Qualifier("OAuthServiceImplGoogle") OAuthService googleOAuthService
    ) {
        this.oauthServiceImplGoogle = oauthServiceImplGoogle;
        this.googleTokenService = googleTokenService;
        this.googleOAuthService = googleOAuthService;
    }
    /**
     * 프론트에서 구글 인가 코드 수신 → access token 요청 → 로그인 처리
     */
    @GetMapping("/oauth/google/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        log.info("[구글 로그인 콜백] 인가 코드 수신: {}", code);
        String accessToken = googleTokenService.requestAccessToken(code);
        googleOAuthService.login(accessToken, response);

        response.sendRedirect("http://localhost:5173/main");
    }

    /**
     * 🔹 1. 프론트 → [백엔드] /oauth/google 요청 → 구글 로그인 창으로 리디렉트
     */
    @GetMapping("/oauth/google")
    public void redirectToGoogle(HttpServletResponse response) throws Exception {
        log.info("[구글 로그인 시도] Google OAuth Redirect 시작");
        response.sendRedirect(GOOGLE_AUTH_URL);  //
    }

    @PostMapping("/oauth/google/logout")
    public ResponseEntity<Void> googleLogout(HttpServletRequest request, HttpServletResponse response) {
        oauthServiceImplGoogle.logout(request, response);
        return ResponseEntity.ok().build();
    }
}
