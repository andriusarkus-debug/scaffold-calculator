package com.scaffold.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

// @Builder (Lombok) leidžia kurti objektus taip: User.builder().username("jonas").build()
// Nenaudojame @Data — @Data generuoja equals/hashCode iš visų laukų (įskaitant passwordHash),
// o entitetams saugiau id-pagrindu. Taip pat @ToString be passwordHash .
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = "passwordHash")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING) // Vaidmenį saugome kaip tekstą duomenų bazėje (pvz. "ROLE_USER")
    @Column(nullable = false)
    private Role role;

    @Builder.Default //  Lombok'as po @Builder ignoruotų inicializavimą (= true). @Builder.Default priverčia naudoti šį default'ą.
    @Column(nullable = false)
    private boolean active = true; // Vartotojo paskyros aktyvumo požymis — administratorius gali išjungti

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatiškai nustato sukūrimo laiką prieš pirmąjį išsaugojimą duomenų bazėje
    @PrePersist // Anotacija — metodas vykdomas prieš išsaugant objektą
    protected void onCreate() { // Metodas, nustatantis sukūrimo laiką, protected- Tai framework hook — niekas iš išorės neturėtų jo kviesti. protected signalizuoja konvenciją. Hibernate per refleksiją vis tiek pasieks.
        this.createdAt = LocalDateTime.now(); // Nustatome dabartinį laiką kaip sukūrimo laiką
    }

    // UserDetails metodai — Spring Security naudoja prisijungimo ir autorizacijos metu
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public boolean isEnabled() { return active; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // equals/hashCode pagal `id` — saugu JPA entitetams.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
