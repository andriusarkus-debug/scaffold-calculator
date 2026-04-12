package com.scaffold.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;



// @Builder (Lombok) leidžia kurti objektus taip: User.builder().username("jonas").build()
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

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

    @Builder.Default // Nurodo numatytąją reikšmę Builder šablonui
    @Column(nullable = false)
    private boolean active = true; // Vartotojo paskyros aktyvumo požymis — administratorius gali išjungti

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatiškai nustato sukūrimo laiką prieš pirmąjį išsaugojimą duomenų bazėje
    @PrePersist // Anotacija — metodas vykdomas prieš išsaugant objektą
    protected void onCreate() { // Metodas, nustatantis sukūrimo laiką
        this.createdAt = LocalDateTime.now(); // Nustatome dabartinį laiką kaip sukūrimo laiką
    }
}
