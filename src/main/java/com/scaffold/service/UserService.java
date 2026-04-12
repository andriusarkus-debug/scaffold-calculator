package com.scaffold.service;

import com.scaffold.dto.RegisterRequest;
import com.scaffold.entity.Role;
import com.scaffold.entity.User;
import com.scaffold.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// JWT importai — palikti ateities REST API / mobile app naudojimui
// import com.scaffold.dto.AuthResponse;
// import com.scaffold.dto.LoginRequest;
// import com.scaffold.security.JwtUtil;
// import org.springframework.security.authentication.BadCredentialsException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // JWT utility — paliktas ateities mobile API naudojimui
    // private final JwtUtil jwtUtil;

    // Registruoja naują vartotoją duomenų bazėje.
    // Prisijungimą tvarko Spring Security automatiškai per formLogin().
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Toks vartotojo vardas jau užimtas");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Šis el. paštas jau naudojamas");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        userRepository.save(user);

        // JWT token generavimas — paliktas ateities mobile API naudojimui
        // String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        // return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas: " + username));
    }

    // JWT prisijungimas — paliktas ateities REST API / mobile app naudojimui
    /*
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Neteisingas vardas arba slaptažodis"));

        if (!user.isActive()) {
            throw new RuntimeException("Paskyra išjungta. Susisiekite su administratoriumi.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Neteisingas vardas arba slaptažodis");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
    */
}
