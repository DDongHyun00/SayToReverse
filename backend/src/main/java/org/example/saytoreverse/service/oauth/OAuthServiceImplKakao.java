package org.example.saytoreverse.service.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.saytoreverse.domain.OAuthUser;
import org.example.saytoreverse.domain.Role;
import org.example.saytoreverse.domain.SocialType;
import org.example.saytoreverse.domain.User;
import org.example.saytoreverse.dto.kakao.KakaoUserDto;
import org.example.saytoreverse.repository.OAuthUserRepository;
import org.example.saytoreverse.repository.UserRepository;
import org.example.saytoreverse.config.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuthServiceImplKakao implements OAuthService {
    private final UserRepository userRepository;
    private final OAuthUserRepository oauthUserRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${KAKAO_USER_INFO_URL}")
    private String KAKAO_USER_INFO_URL;

    // ============ 로그인 로직 (인터페이스 메서드 구현) ============ //
    /**
     * 소셜 로그인 진입점
     * - 프론트에서 받은 카카오 accessToken으로 사용자 정보 조회
     * - 우리 DB에 해당 소셜 유저가 존재하는지 확인
     * - 없으면 회원가입(User + OAuthUser)
     * - JWT 발급 → 쿠키로 전송
     */


    @Override
    public void login(String kakaoAccessToken, HttpServletResponse response) throws Exception{

        // accessToken으로 카카오 유저 정보 조회
        KakaoUserDto kakaoUser = getKakaoUserInfo(kakaoAccessToken);


        // 우리 DB에 이미 등록된 OAuthUser인지 조회
        OAuthUser oAuthUser = oauthUserRepository.findBySocialIdAndSocialType(
                kakaoUser.getId(), SocialType.KAKAO
        ).orElseGet(() -> {
            // 등록 안 된 경우 -> User + OAuthUser 자동 회원가입 처리

            // User 생성
            User newUser = User.builder()
                    .email(kakaoUser.getEmail()) // nullable
                    .password(null)              // 소셜 유저는 password 없음
                    .name(kakaoUser.getNickname()) // 임시 name으로 nickname 사용
                    .nickname(kakaoUser.getNickname())
                    .phone("010-0000-0000")      // 임시 번호 (프론트에서 수집 가능)
                    .role(Role.USER)
                    .build();

            userRepository.save(newUser);

            // OAuthUser 생성
            OAuthUser newOauthUser = OAuthUser.builder()
                    .user(newUser)
                    .socialId(kakaoUser.getId()) // 카카오 고유 ID
                    .socialType(SocialType.KAKAO)
                    .nickname(kakaoUser.getNickname())
                    .profileImageUrl(kakaoUser.getProfileImageUrl())
                    .build();

            return oauthUserRepository.save(newOauthUser);

        });

    }


    // 어떤 소셜 플랫폼인지 구분해주는 메서드(인터페이스용)
    @Override
    public SocialType getSocialType() {
        return SocialType.KAKAO;
    }


    // access token으로 카카오 사용자 정보 요청 → DTO로 변환
    private KakaoUserDto getKakaoUserInfo(String kakaoAccessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken); // Authentication : Bearer 토큰
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 이게 뭔지 모르겠음

        HttpEntity<?> entity = new HttpEntity<>(headers); // 얘도 모름

        ResponseEntity<String> response = new RestTemplate().exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return new KakaoUserDto(jsonNode);
    }




    // ============ JWT 토큰 쿠키에 담기 ============ //

    /**
     * JWT 토큰을 HttpOnly + Secure 쿠키로 전달
     */

    private void setTokenCookie(HttpServletResponse response, String name, String token) {
        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간
        response.addCookie(cookie);
    }


}
