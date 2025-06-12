package org.example.saytoreverse.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)        // 소셜로그인 유저의 경우 null이 들어갈 수 있음.
    private String email;

    @Column(nullable = true)                       // 소셜로그인 유저의 경우 null이 들어갈 수 있음.
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String nickname;

    // 소셜 로그인 유저일 경우 연결
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OAuthUser oauthUser;
}
