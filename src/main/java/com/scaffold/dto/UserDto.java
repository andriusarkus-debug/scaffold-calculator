package com.scaffold.dto;

import com.scaffold.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Saugus vartotojo atsakymas — be slaptažodžio maišos
// Naudojamas admin API atsakymuose vietoje pilno User objekto
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
