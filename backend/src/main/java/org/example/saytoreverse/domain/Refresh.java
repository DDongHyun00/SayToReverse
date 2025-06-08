package org.example.saytoreverse.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refresh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 한 유저당 하나의 리프레시 토큰만 존재하도록
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 토큰 문자열
    @Column(nullable = false, unique = true)
    private String token;

    // 생성일
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 수정일 (토큰 재발급 시 갱신됨)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
