package org.example.saytoreverse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.saytoreverse.domain.User;

@Getter
public class UserResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String role;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole().name(); // enum → 문자열
    }
}
