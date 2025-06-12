package org.example.saytoreverse.service.oauth;

import jakarta.servlet.http.HttpServletResponse;
import org.example.saytoreverse.domain.SocialType;

public interface OAuthService {
    void login(String accessToken, HttpServletResponse response) throws Exception;
    SocialType getSocialType();

}
