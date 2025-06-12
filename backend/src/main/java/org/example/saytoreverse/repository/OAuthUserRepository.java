package org.example.saytoreverse.repository;

import org.example.saytoreverse.domain.OAuthUser;
import org.example.saytoreverse.domain.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthUserRepository extends JpaRepository<OAuthUser, Long> {
    Optional<OAuthUser> findBySocialIdAndSocialType(String socialId, SocialType socialType);
}
