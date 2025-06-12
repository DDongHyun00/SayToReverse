package org.example.saytoreverse.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthUser {
    @Id
    private Long id; // User와 동일한 ID 사용

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(nullable = false, unique = true)
    private String socialId; // ex: "kakao_12345678"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = true)
    private String nickname; // 소셜 닉네임

    @Column(nullable = true)
    private String profileImageUrl;
}
