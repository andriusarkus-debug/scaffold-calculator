package com.scaffold.security;

import com.scaffold.entity.User;
import com.scaffold.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security naudoja šią klasę vartotojui surasti prisijungimo metu.
// formLogin() automatiškai iškviečia loadUserByUsername() su įvestu vardu.
// User entitetas implementuoja UserDetails — grąžiname jį tiesiai, be papildomo konvertavimo.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Vartotojas nerastas: " + username));
    }
}
