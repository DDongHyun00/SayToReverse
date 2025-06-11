package org.example.saytoreverse.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.saytoreverse.config.jwt.JwtTokenProvider;
import org.example.saytoreverse.domain.Refresh;
import org.example.saytoreverse.domain.User;
import org.example.saytoreverse.dto.LoginRequestDto;
import org.example.saytoreverse.dto.SignupRequestDto;
import org.example.saytoreverse.repository.RefreshRepository;
import org.example.saytoreverse.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;


    // 회원가입
    @Override
    @Transactional
    public void signup(SignupRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .role(org.example.saytoreverse.domain.Role.USER)
                .build();

        userRepository.save(user);
    }

    // 로그인
    @Override
    @Transactional
    public void login(LoginRequestDto requestDto, HttpServletResponse response) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh 토큰 DB 저장 (user별로 1개만 존재하게)
        Optional<Refresh> existing = refreshRepository.findByUser(user);
        if (existing.isPresent()) {
            Refresh refresh = existing.get();
            refresh.setToken(refreshToken);
            refreshRepository.save(refresh);
        } else {
            Refresh refresh = Refresh.builder()
                    .user(user)
                    .token(refreshToken)
                    .build();
            refreshRepository.save(refresh);
        }

        // 쿠키에 토큰 저장
        addTokenToCookie("AccessToken", accessToken, response);
        addTokenToCookie("RefreshToken", refreshToken, response);
    }

    // 쿠키 생성 메서드
    private void addTokenToCookie(String name, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/");    // 쿠키의 경로 지정
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(cookie);
    }


    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refreshToken을 꺼내기
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if(refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            // DB에서 RefreshToken 삭제
            refreshRepository.deleteByUser(user);
        }

        // 쿠키 삭제 (Access, Refresh 둘 다 삭제)
        expireCookie("AccessToken",response);
        expireCookie("RefreshToken",response);
    }

    // 쿠키 만료 처리 메서드
    private void expireCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name,null);
        cookie.setPath("/");  // 쿠키가 있던 원래 경로 그대로 설정
        cookie.setMaxAge(0);  // 브라우저에 "이 쿠키 바로 삭제해"라고 명령 (-1은 세션 쿠키, >0은 몇 초 후 만료, 0은 즉시 삭제)
        cookie.setHttpOnly(true); // 쿠키 생성때 HttpOnly가 되어있었다면, 같게 유지 해야 함.
        response.addCookie(cookie); // 이 쿠키를 응답에 실어서 브라우저에게 보냄
    }

    // AccessToken 재발급
    @Override
//    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 RefreshToken 꺼내기
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        // 없으면 종료
        if(refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)){
            throw new IllegalArgumentException("RefreshToken이 유효하지 않습니다.");
        }

        // 토큰에 있는 userId 꺼냄
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // DB에 저장된 RefreshToken과 일치하는지 확인
        Refresh refresh = refreshRepository.findByUser(user)
                .orElseThrow(()-> new IllegalArgumentException("DB에 토큰 없음"));

        if(!refresh.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("토큰 불일치");
        }

        // 새 AccessTOken 생성 -> 쿠키로 재전송
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
        addTokenToCookie("AccessToken", newAccessToken, response);
    }
}
