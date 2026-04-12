package com.scaffold.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// JWT imports — palikti ateities REST API / mobile app naudojimui
// import com.scaffold.security.JwtAuthFilter;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // JWT filter — paliktas ateities mobile API naudojimui
    // private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF įjungtas — Thymeleaf automatiškai įterpia CSRF token į formas
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers("/history/all").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                    .anyRequest().authenticated()
            )
            // Prisijungimo forma — Spring Security automatiškai patikrina vardą ir slaptažodį
            .formLogin(form -> form
                    .loginPage("/login")               // mūsų prisijungimo puslapis
                    .loginProcessingUrl("/login")      // forma siunčia POST į šį URL
                    .defaultSuccessUrl("/calculator", true)
                    .failureUrl("/login?error")
                    .permitAll()
            )
            // Atsijungimas
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .userDetailsService(userDetailsService);

        // JWT stateless konfigūracija — palikta ateities REST API naudojimui
        // .csrf(csrf -> csrf.disable())
        // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
