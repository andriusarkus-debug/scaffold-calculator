package com.scaffold.service;

import com.scaffold.dto.UserDto;
import com.scaffold.entity.Role;
import com.scaffold.entity.User;
import com.scaffold.exception.UserNotFoundException;
import com.scaffold.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Skaitymo metodai bus read-only pagal nutylėjimą
public class AdminService {

    private final UserRepository userRepository;

    // Grąžina visų vartotojų sąrašą (be slaptažodžių)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // Pakeičia vartotojo vaidmenį (pvz. ROLE_USER → ROLE_MANAGER)
    @Transactional // Rašymo metodas — perrašo klasės lygio read-only
    public UserDto updateRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setRole(newRole);
        return toDto(userRepository.save(user));
    }

    // Aktyvuoja arba deaktyvuoja vartotojo paskyrą
    @Transactional // Rašymo metodas — perrašo klasės lygio read-only
    public UserDto updateActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setActive(active);
        return toDto(userRepository.save(user));
    }

    // Paverčia User objektą į saugų DTO (be slaptažodžio maišos)
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
