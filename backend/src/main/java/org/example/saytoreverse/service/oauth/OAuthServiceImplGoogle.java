package org.example.saytoreverse.service.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.saytoreverse.config.jwt.JwtTokenProvider;
import org.example.saytoreverse.domain.*;
import org.example.saytoreverse.dto.Google.GoogleUserDto;
import org.example.saytoreverse.repository.OAuthUserRepository;
import org.example.saytoreverse.repository.UserRepository;
import org.example.saytoreverse.service.RefreshTokenService;
import org.example.saytoreverse.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
@Service("OAuthServiceImplGoogle")  // ★ 반드시 이름 명시!
public class OAuthServiceImplGoogle implements OAuthService {

    private final UserRepository userRepository;
    private final OAuthUserRepository oauthUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    @Value("${GOOGLE_USER_INFO_URL}")
    private String GOOGLE_USER_INFO_URL;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void login(String googleAccessToken, HttpServletResponse response) throws Exception {
        log.info("[Google Login] accessToken: {}", googleAccessToken);

        // 1. accessToken으로 구글 유저 정보 조회
        GoogleUserDto googleUser = getGoogleUserInfo(googleAccessToken);

        // 2. DB에 OAuthUser 존재 확인
        OAuthUser oAuthUser = oauthUserRepository
                .findBySocialIdAndSocialType(googleUser.getId(), SocialType.GOOGLE)
                .orElseGet(() -> {
                    // 2-1. 없으면 User + OAuthUser 등록
                    User newUser = User.builder()
                            .email(googleUser.getEmail())
                            .password(null)
                            .name(googleUser.getName())
                            .build();
                    userRepository.save(newUser);

                    OAuthUser newOAuthUser = OAuthUser.builder()
                            .socialId(googleUser.getId())
                            .socialType(SocialType.GOOGLE)
                            .user(newUser)
                            .build();
                    return oauthUserRepository.save(newOAuthUser);
                });

        User user = oAuthUser.getUser();
        // 3. JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Redis에 Refresh Token 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken, refreshTokenExpiration);

        // 쿠키로 전달
        setTokenCookie(response, "AccessToken", accessToken);
        setTokenCookie(response, "RefreshToken", refreshToken);
        log.info("[Google Access/Refresj 토큰 발급 성공] : AccessToken="+accessToken + " refreshToken="+refreshToken);
        log.info("[Google Login] 성공적으로 로그인 처리됨");
    }

    private void setTokenCookie(HttpServletResponse response, String name, String token) {
        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간
        response.addCookie(cookie);
    }
    // 쿠키 삭제용
    private void expireCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    // 기존에 있는 쿠키 추가 메서드 재사용
    private void addTokenToCookie(String name, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간
        response.addCookie(cookie);
    }


    /**
     * accessToken으로 구글 사용자 정보 조회
     */
    private GoogleUserDto getGoogleUserInfo(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                String.class
        );

        log.info("[GoogleUserInfo] 응답: {}", response.getBody());

        JsonNode json = new ObjectMapper().readTree(response.getBody());
        return new GoogleUserDto(json);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // AccessToken 블랙리스트 등록
        String accessToken = extractAccessTokenFromCookie(request);

        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Date expiration = jwtTokenProvider.getExpiration(accessToken);
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();
            if (ttlMillis > 0) {
                tokenBlacklistService.addToBlacklist(accessToken, ttlMillis);
            }
        }

        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            // Redis에서 RefreshToken 삭제
            refreshTokenService.deleteRefreshToken(userId);
        }

        // 쿠키 만료
        expireCookie("AccessToken", response);
        expireCookie("RefreshToken", response);

    }
    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookie -> cookie.getName().equals("AccessToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }


    @Override
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 RefreshToken입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // Redis에 저장된 RefreshToken과 일치하는지 확인
        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        // AccessToken 재발급 → 쿠키로 전달
        addTokenToCookie("AccessToken", newAccessToken, response);
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.GOOGLE;
    }
}
