package org.example.saytoreverse.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.saytoreverse.dto.LoginRequestDto;
import org.example.saytoreverse.dto.SignupRequestDto;

import java.security.Signature;

public interface AuthService {

    void signup(SignupRequestDto requestDto);

    void login(LoginRequestDto requestDto, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    void reissue(HttpServletRequest request, HttpServletResponse response);

}
