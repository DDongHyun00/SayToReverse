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
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(cookie);
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // TODO
    }

    @Override
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        // TODO
    }
}
